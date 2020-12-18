package org.jsonplayback.player.hibernate.entities;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

import org.jsonplayback.player.LazyProperty;
import org.jsonplayback.player.implementation.BlobBase64Serializer;
import org.jsonplayback.player.implementation.ByteArrayBase64Serializer;
import org.jsonplayback.player.implementation.ClobStringSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name="MASTER_A")
public class MasterAEnt {
	@Id()
	@Column(name="MTRA_ID", columnDefinition="INTEGER")
	private Integer id;
	@Column(name="MTRA_VCHAR_A", columnDefinition="VARCHAR(200)")
	private String vcharA;
	@Column(name="MTRA_VCHAR_B", columnDefinition="VARCHAR(200)")
	private String vcharB;
	@Column(name="MTRA_DATE_A", columnDefinition="DATE")
	private Date dateA;
	@Column(name="MTRA_DATETIME_A", columnDefinition="TIMESTAMP")
	private Date datetimeA;
	@JsonSerialize(using=ByteArrayBase64Serializer.class)
	@Column(name="MTRA_BLOB_A", columnDefinition="BLOB")
	private byte[] blobA;
	@JsonSerialize(using=BlobBase64Serializer.class)
	@Column(name="MTRA_BLOB_B", columnDefinition="BLOB")
	private Blob blobB;
	@Version
	@Column(name="MTRA_HB_VERSION", columnDefinition="INTEGER")
	private Integer hbVersion;
	@OneToMany(cascade={CascadeType.ALL}, orphanRemoval=true, fetch=FetchType.LAZY) 
	@JoinColumns({
		@JoinColumn(name="DTLA_MTRA_ID", columnDefinition="INTEGER")
	})
	@OrderBy("DTLA_SUB_ID")
	private Set<DetailAEnt> detailAEntCol;
	@LazyProperty(nonLazyMaxSize=1024)
	@JsonSerialize(using=ByteArrayBase64Serializer.class)
	@Column(name="MTRA_BLOB_LAZY_A", columnDefinition="BLOB")
	private byte[] blobLazyA;
	@JsonSerialize(using=BlobBase64Serializer.class)
	@LazyProperty(nonLazyMaxSize=1024)
	@Column(name="MTRA_BLOB_LAZY_B", columnDefinition="BLOB")
	private Blob blobLazyB;
	@LazyProperty(nonLazyMaxSize=1024)
	@Column(name="MTRA_CLOB_LAZY_A", columnDefinition="CLOB")
	private String clobLazyA;
	@JsonSerialize(using=ClobStringSerializer.class)
	@LazyProperty(nonLazyMaxSize=1024)
	@Column(name="MTRA_CLOB_LAZY_B", columnDefinition="CLOB")
	private Clob clobLazyB;	
	
	public byte[] getBlobLazyA() {
		return blobLazyA;
	}
	public void setBlobLazyA(byte[] blobLazyA) {
		this.blobLazyA = blobLazyA;
	}
	public Blob getBlobLazyB() {
		return blobLazyB;
	}
	public void setBlobLazyB(Blob blobLazyB) {
		this.blobLazyB = blobLazyB;
	}
	public String getClobLazyA() {
		return clobLazyA;
	}
	public void setClobLazyA(String clobLazyA) {
		this.clobLazyA = clobLazyA;
	}
	public Clob getClobLazyB() {
		return clobLazyB;
	}
	public void setClobLazyB(Clob clobLazyB) {
		this.clobLazyB = clobLazyB;
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
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
