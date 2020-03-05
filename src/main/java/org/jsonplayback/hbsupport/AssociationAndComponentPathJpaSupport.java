package org.jsonplayback.hbsupport;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute.CollectionType;

import org.jsonplayback.player.hibernate.AssociationAndComponentPath;

public class AssociationAndComponentPathHb6Support extends AssociationAndComponentPath {
	private EmbeddableType<?>[] compositeTypePath;
	private CollectionType collType;
	private EmbeddableType<?> compType;
	private EntityType<?> relEntity;
	public EmbeddableType<?>[] getCompositeTypePath() {
		return compositeTypePath;
	}
	public void setCompositeTypePath(EmbeddableType<?>[] compositeTypePath) {
		this.compositeTypePath = compositeTypePath;
	}
	public CollectionType getCollType() {
		return collType;
	}
	public void setCollType(CollectionType collType) {
		this.collType = collType;
	}
	public EmbeddableType<?> getCompType() {
		return compType;
	}
	public void setCompType(EmbeddableType<?> compType) {
		this.compType = compType;
	}
	public EntityType<?> getRelEntity() {
		return relEntity;
	}
	public void setRelEntity(EntityType<?> relEntity) {
		this.relEntity = relEntity;
	}
}
