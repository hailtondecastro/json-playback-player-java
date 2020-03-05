package org.jsonplayback.player.implementation;

public class AssociationAndComponentTrackInfo {
	private Object entityOwner;
	private AssociationAndComponentPath entityAndComponentPath;
//	private EntityAndComponentPathKey componentTypeKey;
//	private CompositeType[] compositeTypePath;
//	private String[] compositePrpPath;
//	private EntityType relEntity;
	
	public AssociationAndComponentTrackInfo() {
	}
	
	public Object getEntityOwner() {
		return entityOwner;
	}
	public void setEntityOwner(Object entityOwner) {
		this.entityOwner = entityOwner;
	}

	public AssociationAndComponentPath getEntityAndComponentPath() {
		return entityAndComponentPath;
	}

	public void setEntityAndComponentPath(AssociationAndComponentPath entityAndComponentPath) {
		this.entityAndComponentPath = entityAndComponentPath;
	}
	
	
//	public EntityAndComponentPathKey getComponentTypeKey() {
//		return componentTypeKey;
//	}
//	public void setComponentTypeKey(EntityAndComponentPathKey componentTypeKey) {
//		this.componentTypeKey = componentTypeKey;
//	}
//	public CompositeType[] getCompositeTypePath() {
//		return compositeTypePath;
//	}
//	public void setCompositeTypePath(CompositeType[] compositeTypePath) {
//		this.compositeTypePath = compositeTypePath;
//	}
//	public String[] getCompositePrpPath() {
//		return compositePrpPath;
//	}
//	public void setCompositePrpPath(String[] compositePrpPath) {
//		this.compositePrpPath = compositePrpPath;
//	}
//	public EntityType getRelEntity() {
//		return relEntity;
//	}
//	public void setRelEntity(EntityType relEntity) {
//		this.relEntity = relEntity;
//	}
//	
//	public CompositeType getLastCompositeType() {
//		if (this.compositeTypePath.length > 0) {
//			return this.compositeTypePath[this.compositeTypePath.length - 1];
//		} else {
//			return null;
//		}
//	}
}
