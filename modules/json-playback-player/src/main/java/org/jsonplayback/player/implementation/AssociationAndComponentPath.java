package org.jsonplayback.player.implementation;

public abstract class AssociationAndComponentPath implements Cloneable {
	private AssociationAndComponentPathKey aacKey;
	private String[] compositePrpPath;

	public AssociationAndComponentPathKey getAacKey() {
		return aacKey;
	}
	public void setAacKey(AssociationAndComponentPathKey aacKey) {
		this.aacKey = aacKey;
	}
	
	public String[] getCompositePrpPath() {
		return compositePrpPath;
	}
	public void setCompositePrpPath(String[] compositePrpPath) {
		this.compositePrpPath = compositePrpPath;
	}
}
