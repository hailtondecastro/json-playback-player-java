package org.jsonplayback.player.hibernate.entities;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class DetailACompId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="DUMMY_COL_DetailACompId_01")
	private MasterAEnt masterA;
	private Integer subId;
	public MasterAEnt getMasterA() {
		return masterA;
	}
	public void setMasterA(MasterAEnt masterA) {
		this.masterA = masterA;
	}	
	public Integer getSubId() {
		return subId;
	}
	public void setSubId(Integer subId) {
		this.subId = subId;
	}
	@Override
	public int hashCode() {
		return this.masterA.getId() + this.subId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DetailACompId) {
			DetailACompId detailACompIdObj = (DetailACompId) obj;
			return this.masterA.getId().equals(detailACompIdObj.getMasterA().getId()) 
					&& this.subId.equals(detailACompIdObj.getSubId());	
		} else {
			return false;
		}
	}
}
