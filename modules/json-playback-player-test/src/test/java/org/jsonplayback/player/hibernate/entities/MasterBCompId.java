package org.jsonplayback.player.hibernate.entities;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class MasterBCompId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer idA;
	private Integer idB;
	public Integer getIdA() {
		return idA;
	}
	public void setIdA(Integer idA) {
		this.idA = idA;
	}
	public Integer getIdB() {
		return idB;
	}
	public void setIdB(Integer idB) {
		this.idB = idB;
	}

	
	@Override
	public int hashCode() {
		return this.getIdA() + this.getIdB();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MasterBCompId) {
			MasterBCompId masterBCompIdObj = (MasterBCompId) obj;
			return this.idA.equals(masterBCompIdObj.getIdA()) 
					&& this.idB.equals(masterBCompIdObj.getIdB());	
		} else {
			return false;
		}
	}
}
