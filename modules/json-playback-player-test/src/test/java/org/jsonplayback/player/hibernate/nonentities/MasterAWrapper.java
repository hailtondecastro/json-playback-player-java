package org.jsonplayback.player.hibernate.nonentities;

import java.util.Collection;
import java.util.List;

import org.jsonplayback.player.hibernate.entities.DetailAEnt;
import org.jsonplayback.player.hibernate.entities.MasterAEnt;

public class MasterAWrapper {
	private MasterAEnt masterA;
	private List<DetailAWrapper> detailAWrapperList;
	private Collection<DetailAEnt> detailAEntCol;
	
	public Collection<DetailAEnt> getDetailAEntCol() {
		return detailAEntCol;
	}

	public void setDetailAEntCol(Collection<DetailAEnt> detailAEntCol) {
		this.detailAEntCol = detailAEntCol;
	}

	public List<DetailAWrapper> getDetailAWrapperList() {
		return detailAWrapperList;
	}

	public void setDetailAWrapperList(List<DetailAWrapper> detailAWrapperList) {
		this.detailAWrapperList = detailAWrapperList;
	}

	public MasterAEnt getMasterA() {
		return masterA;
	}

	public void setMasterA(MasterAEnt masterA) {
		this.masterA = masterA;
	}
	
		
}
