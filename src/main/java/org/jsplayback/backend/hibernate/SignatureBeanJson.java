package org.jsplayback.backend.hibernate;

import org.jsplayback.backend.SignatureBean;

/**
 * Usado para Serializar {@link SignatureBean} pra json
 * @author Hailton de Castro
 *
 */
class SignatureBeanJson {
	private String clazzName;
	private String entityName;
	private Boolean isColl;
	private Boolean isAssoc;
	private Boolean isComp;
	/**
	 * Se nulo eh a propria entidade, caso contrario eh o lazy de uma entidade.
	 */
	private String propertyName;
	private String[] rawKeyValues;
	private String[] rawKeyTypeNames;
	public String getClazzName() {
		return clazzName;
	}
	public void setClazzName(String clazzName) {
		this.clazzName = clazzName;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public String[] getRawKeyValues() {
		return rawKeyValues;
	}
	public void setRawKeyValues(String[] rawKeyValues) {
		this.rawKeyValues = rawKeyValues;
	}
	public String[] getRawKeyTypeNames() {
		return rawKeyTypeNames;
	}
	public void setRawKeyTypeNames(String[] rawKeyTypeNames) {
		this.rawKeyTypeNames = rawKeyTypeNames;
	}
	public Boolean getIsColl() {
		return isColl;
	}
	public void setIsColl(Boolean isColl) {
		this.isColl = isColl;
	}
	public Boolean getIsAssoc() {
		return isAssoc;
	}
	public void setIsAssoc(Boolean isAssoc) {
		this.isAssoc = isAssoc;
	}
	public Boolean getIsComp() {
		return isComp;
	}
	public void setIsComp(Boolean isComp) {
		this.isComp = isComp;
	}
}
/*gerando conflito*/