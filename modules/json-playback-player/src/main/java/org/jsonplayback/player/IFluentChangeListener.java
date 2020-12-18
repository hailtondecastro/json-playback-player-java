package org.jsonplayback.player;

import java.util.function.Consumer;
import java.util.function.Function;

public interface IFluentChangeListener<E> {
	IFluentChangeListener<E> onBeforeForClass(String listenerName, Consumer<ChangeActionEventArgs<E>> forClassCallback);
	IFluentChangeListener<E> onBeforeForProperty(String listenerName, Function<E, ?> prpFunction, Consumer<ChangeActionEventArgs<E>> forPropertyCallback);
	
	IFluentChangeListener<E> onAfterForClass(String listenerName, Consumer<ChangeActionEventArgs<E>> forClassCallback);
	IFluentChangeListener<E> onAfterForProperty(String listenerName, Function<E, ?> prpFunction, Consumer<ChangeActionEventArgs<E>> forPropertyCallback);
	
	IReplayable complete();
}
