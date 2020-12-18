package org.jsonplayback.player;

import java.util.Collection;

public class ChangeActionEventArgs<E> {
	private E ownerValue;
	private Object settedValue;
	private String propertyName;
	private Collection changedCollection;
	private TapeActionType actionType;
	
	@SuppressWarnings("rawtypes")
	public ChangeActionEventArgs(E ownerValue, Object settedValue, String property, Collection changedCollection,
			TapeActionType actionType) {
		super();
		this.ownerValue = ownerValue;
		this.settedValue = settedValue;
		this.propertyName = property;
		this.changedCollection = changedCollection;
		this.actionType = actionType;
	}
	
	public E getOwnerValue() {
		return ownerValue;
	}
	public void setOwnerValue(E ownerValue) {
		this.ownerValue = ownerValue;
	}
	public Object getSettedValue() {
		return settedValue;
	}
	public void setSettedValue(Object settedValue) {
		this.settedValue = settedValue;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String property) {
		this.propertyName = property;
	}
	public Collection getChangedCollection() {
		return changedCollection;
	}
	public void setChangedCollection(Collection changedCollection) {
		this.changedCollection = changedCollection;
	}
	public TapeActionType getActionType() {
		return actionType;
	}
	public void setActionType(TapeActionType actionType) {
		this.actionType = actionType;
	}
	
	
}
