package org.jsonplayback.player.hibernate.entities;

import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Embeddable
public class MasterBCompComp {
	@OneToMany(cascade={}, orphanRemoval=true, fetch=FetchType.LAZY) 
	@OrderBy("DTLA_SUB_ID")
	@JoinColumns({
		@JoinColumn(name="DUMMY_COL_MasterBCompComp_01", columnDefinition="INTEGER"),
		@JoinColumn(name="DUMMY_COL_MasterBCompComp_02", columnDefinition="INTEGER"),
	})
	private Set<DetailAEnt> detailAEntCol;

	public Set<DetailAEnt> getDetailAEntCol() {
		return detailAEntCol;
	}

	public void setDetailAEntCol(Set<DetailAEnt> detailAEntCol) {
		this.detailAEntCol = detailAEntCol;
	}
}
