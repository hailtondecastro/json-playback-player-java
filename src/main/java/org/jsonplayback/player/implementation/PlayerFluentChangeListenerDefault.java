package org.jsonplayback.player.hibernate;

import java.util.function.Consumer;
import java.util.function.Function;

import org.jsonplayback.player.IChangeActionListener;
import org.jsonplayback.player.IFluentChangeListener;
import org.jsonplayback.player.IReplayable;
import org.jsonplayback.player.ChangeActionEventArgs;

public class PlayerFluentChangeListenerDefault<E> implements IFluentChangeListener<E> {

	private IReplayable replayable;

	public PlayerFluentChangeListenerDefault(IReplayable replayable, Class<E> classTarget) {
		this.replayable = replayable;
		this.classTarget = classTarget;
	}

	private Class<E> classTarget;

	@SuppressWarnings("unused")
	static class ChangeActionListener<T> implements IChangeActionListener {

		private String name;
		private Consumer<ChangeActionEventArgs<Object>> beforeCallback;
		private Consumer<ChangeActionEventArgs<Object>> afterCallback;

		public ChangeActionListener(String name, Class<T> classTarget,
				Consumer<ChangeActionEventArgs<Object>> beforeCallback,
				Consumer<ChangeActionEventArgs<Object>> afterCallback) {
			super();
			this.name = name;
			this.beforeCallback = beforeCallback;
			this.afterCallback = afterCallback;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public void onBeforeChange(ChangeActionEventArgs eventArgs) {
			if (this.beforeCallback != null) {
				this.beforeCallback.accept(eventArgs);
			}
		}

		@Override
		public void onAfterChange(ChangeActionEventArgs eventArgs) {
			if (this.afterCallback != null) {
				this.afterCallback.accept(eventArgs);
			}
		}
	}

	@Override
	public IFluentChangeListener<E> onBeforeForClass(String name,
			Consumer<ChangeActionEventArgs<E>> forAllPropertiesCallback) {

		if (this.classTarget != null) {
			this.replayable.addChangeActionListenerForClass(this.classTarget,
					new ChangeActionListener(name, this.classTarget, forAllPropertiesCallback, null));
		} else {
			this.replayable.addChangeActionListener(
					new ChangeActionListener(name, this.classTarget, forAllPropertiesCallback, null));
		}

		return this;
	}

	@Override
	public IFluentChangeListener<E> onBeforeForProperty(String name, Function<E, ?> prpFunction,
			Consumer<ChangeActionEventArgs<E>> forPropertyCallback) {
		this.replayable.addChangeActionListenerForProperty(this.classTarget, prpFunction,
				new ChangeActionListener(name, this.classTarget, forPropertyCallback, null));

		return this;
	}

	@Override
	public IFluentChangeListener<E> onAfterForClass(String name,
			Consumer<ChangeActionEventArgs<E>> forAllPropertiesCallback) {
		if (this.classTarget != null) {
			this.replayable.addChangeActionListenerForClass((Class<E>) this.classTarget,
					new ChangeActionListener(name, this.classTarget, null, forAllPropertiesCallback));
		} else {
			this.replayable.addChangeActionListener(
					new ChangeActionListener(name, this.classTarget, null, forAllPropertiesCallback));
		}

		return this;
	}

	@Override
	public IFluentChangeListener<E> onAfterForProperty(String name, Function<E, ?> prpFunction,
			Consumer<ChangeActionEventArgs<E>> forPropertyCallback) {
		this.replayable.addChangeActionListenerForProperty(this.classTarget, prpFunction,
				new ChangeActionListener(name, this.classTarget, null, forPropertyCallback));

		return this;
	}

	@Override
	public IReplayable complete() {
		// TODO Auto-generated method stub
		return this.replayable;
	}
}
