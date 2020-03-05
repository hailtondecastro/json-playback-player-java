package org.jsonplayback.player.hibernate.entities;

import java.sql.Blob;
import java.util.Date;
import java.util.Set;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

import org.jsonplayback.player.implementation.BlobBase64Serializer;
import org.jsonplayback.player.implementation.ByteArrayBase64Serializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name="MASTER_B")
public class MasterBEnt {
	@EmbeddedId()
	@AttributeOverrides({
		@AttributeOverride(name="idA", column=@Column(name="MTRB_ID_A", columnDefinition="INTEGER")),
		@AttributeOverride(name="idB", column=@Column(name="MTRB_ID_B", columnDefinition="INTEGER"))
	})
	private MasterBCompId compId;
	@Column(name="MTRB_VCHAR_A", columnDefinition="VARCHAR(200)")
	private String vcharA;
	@Column(name="MTRB_VCHAR_B", columnDefinition="VARCHAR(2000)")
	private String vcharB;
	@Column(name="MTRB_DATE_A", columnDefinition="DATE")
	private Date dateA;
	@Column(name="MTRB_DATETIME_A", columnDefinition="TIMESTAMP")
	private Date datetimeA;
	@JsonSerialize(using=ByteArrayBase64Serializer.class)
	@Column(name="MTRB_BLOB_A", columnDefinition="BLOB")
	private byte[] blobA;
	@JsonSerialize(using=BlobBase64Serializer.class)
	@Column(name="MTRB_BLOB_B", columnDefinition="BLOB")
	private Blob blobB;
	@Version
	@Column(name="MTRB_HB_VERSION", columnDefinition="INTEGER")
	private Integer hbVersion;
	@OneToMany(cascade={CascadeType.ALL}, orphanRemoval=true, fetch=FetchType.LAZY) 
	@JoinColumns({
		@JoinColumn(name="DTLA_MTRB_ID_A_COMPONENT", columnDefinition="INTEGER"),
		@JoinColumn(name="DTLA_MTRB_ID_B_COMPONENT", columnDefinition="INTEGER"),
	})
	@OrderBy("DTLA_SUB_ID")
	private Set<DetailAEnt> detailAEntCol;
	@Embedded
	@AssociationOverrides({
		@AssociationOverride(
				name="detailAEntColB", 
				joinColumns={
						@JoinColumn(name="DTLA_MTRB_ID_A_COMPONENT", columnDefinition="INTEGER"),
						@JoinColumn(name="DTLA_MTRB_ID_B_COMPONENT", columnDefinition="INTEGER")
				}),
		@AssociationOverride(
				name="masterBCompComp.detailAEntCol", 
				joinColumns={
						@JoinColumn(name="DTLA_MTRB_ID_A_COMPONENT", columnDefinition="INTEGER"),
						@JoinColumn(name="DTLA_MTRB_ID_B_COMPONENT", columnDefinition="INTEGER")
				}),
	})
	private MasterBComp masterBComp;
	
	public MasterBComp getMasterBComp() {
		return masterBComp;
	}
	public void setMasterBComp(MasterBComp masterBComp) {
		this.masterBComp = masterBComp;
	}
	public Integer getHbVersion() {
		return hbVersion;
	}
	public void setHbVersion(Integer hbVersion) {
		this.hbVersion = hbVersion;
	}
	public Set<DetailAEnt> getDetailAEntCol() {
		return detailAEntCol;
	}
	public void setDetailAEntCol(Set<DetailAEnt> detailAEntCol) {
		this.detailAEntCol = detailAEntCol;
	}
	public MasterBCompId getCompId() {
		return compId;
	}
	public void setCompId(MasterBCompId compId) {
		this.compId = compId;
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
	public Date getDateA() {
		return dateA;
	}
	public void setDateA(Date dateA) {
		this.dateA = dateA;
	}
	public Date getDatetimeA() {
		return datetimeA;
	}
	public void setDatetimeA(Date datetimeA) {
		this.datetimeA = datetimeA;
	}
	public byte[] getBlobA() {
		return blobA;
	}
	public void setBlobA(byte[] blobA) {
		this.blobA = blobA;
	}
	public Blob getBlobB() {
		return blobB;
	}
	public void setBlobB(Blob blobB) {
		this.blobB = blobB;
	}
}
