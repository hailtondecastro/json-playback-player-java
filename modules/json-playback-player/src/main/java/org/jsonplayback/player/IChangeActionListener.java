package org.jsonplayback.player;

public interface IChangeActionListener {
	String getName();
	void onBeforeChange(ChangeActionEventArgs eventArgs);
	void onAfterChange(ChangeActionEventArgs eventArgs);
}
/*gerando conflito*/