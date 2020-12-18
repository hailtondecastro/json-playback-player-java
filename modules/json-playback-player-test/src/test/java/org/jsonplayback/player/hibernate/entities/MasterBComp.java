package org.jsonplayback.player.hibernate.entities;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Embeddable
public class MasterBComp {
	@OneToMany(cascade={}, fetch=FetchType.LAZY) 
	@OrderBy("DTLA_SUB_ID")
	@JoinColumns({
		@JoinColumn(name="DUMMY_COL_MasterBComp_01", columnDefinition="INTEGER"),
		@JoinColumn(name="DUMMY_COL_MasterBComp_02", columnDefinition="INTEGER"),
	})
	private Set<DetailAEnt> detailAEntCol;
	@Embedded
	private MasterBCompComp masterBCompComp;

	public Set<DetailAEnt> getDetailAEntCol() {
		return detailAEntCol;
	}

	public void setDetailAEntCol(Set<DetailAEnt> detailAEntCol) {
		this.detailAEntCol = detailAEntCol;
	}

	public MasterBCompComp getMasterBCompComp() {
		return masterBCompComp;
	}

	public void setMasterBCompComp(MasterBCompComp masterBCompComp) {
		this.masterBCompComp = masterBCompComp;
	}
}
