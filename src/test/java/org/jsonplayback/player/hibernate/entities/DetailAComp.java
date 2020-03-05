package org.jsonplayback.player.hibernate.entities;

import java.sql.Blob;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

import org.jsonplayback.player.implementation.BlobBase64Serializer;
import org.jsonplayback.player.implementation.ByteArrayBase64Serializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Embeddable
public class DetailAComp {
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="DUMMY_COL_DetailAComp_01"),
		@JoinColumn(name="DUMMY_COL_DetailAComp_02")
	})
	private MasterBEnt masterB;
	@Embedded
	private DetailACompComp detailACompComp;
	private String vcharA;
	private String vcharB;
	@JsonSerialize(using=ByteArrayBase64Serializer.class)
	private byte[] blobA;
	@JsonSerialize(using=BlobBase64Serializer.class)
	private Blob blobB;
	
	public DetailACompComp getDetailACompComp() {
		return detailACompComp;
	}
	public void setDetailACompComp(DetailACompComp detailACompComp) {
		this.detailACompComp = detailACompComp;
	}
	public Blob getBlobB() {
		return blobB;
	}
	public void setBlobB(Blob blobB) {
		this.blobB = blobB;
	}
	public MasterBEnt getMasterB() {
		return masterB;
	}
	public void setMasterB(MasterBEnt masterB) {
		this.masterB = masterB;
	}
	public String getVcharA() {
		return vcharA;
	}
	public void setVcharA(String vcharA) {
		this.vcharA = vcharA;
	}
	public String getVcharB() {
		return vcharB;
	}
	public void setVcharB(String vcharB) {
		this.vcharB = vcharB;
	}
	public byte[] getBlobA() {
		return blobA;
	}
	public void setBlobA(byte[] blobA) {
		this.blobA = blobA;
	}
	
	
}
