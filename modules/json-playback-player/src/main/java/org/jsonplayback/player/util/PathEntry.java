package org.jsonplayback.player.util;

public class PathEntry {
    private Class<?> directOwnerType;
    private String directFieldName;
    private Class<?> directFieldType;
	public Class<?> getDirectOwnerType() {
		return directOwnerType;
	}
	public void setDirectOwnerType(Class<?> directOwnerType) {
		this.directOwnerType = directOwnerType;
	}
	public String getDirectFieldName() {
		return directFieldName;
	}
	public void setDirectFieldName(String directFieldName) {
		this.directFieldName = directFieldName;
	}
	public Class<?> getDirectFieldType() {
		return directFieldType;
	}
	public void setDirectFieldType(Class<?> directFieldType) {
		this.directFieldType = directFieldType;
	}
    
}
