package org.jsonplayback.jpa;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;

import org.jsonplayback.player.implementation.AssociationAndComponentPath;

public class AssociationAndComponentPathJpaSupport extends AssociationAndComponentPath {
	private EmbeddableType<?>[] compositeTypePath;
	private PluralAttribute<?, ?, ?> pluralAttribuite;
	private EmbeddableType<?> embeddableType;
	private EntityType<?> relEntity;
	public EmbeddableType<?>[] getCompositeTypePath() {
		return compositeTypePath;
	}
	public void setCompositeTypePath(EmbeddableType<?>[] compositeTypePath) {
		this.compositeTypePath = compositeTypePath;
	}
	public PluralAttribute getPluralAttribuite() {
		return pluralAttribuite;
	}
	public void setPluralAttribuite(PluralAttribute<?, ?, ?> pluralAttribute) {
		this.pluralAttribuite = pluralAttribute;
	}
	public EmbeddableType<?> getEmbeddableType() {
		return embeddableType;
	}
	public void setEmbeddableType(EmbeddableType<?> embeddableType) {
		this.embeddableType = embeddableType;
	}
	public EntityType<?> getRelEntity() {
		return relEntity;
	}
	public void setRelEntity(EntityType<?> relEntity) {
		this.relEntity = relEntity;
	}
}
