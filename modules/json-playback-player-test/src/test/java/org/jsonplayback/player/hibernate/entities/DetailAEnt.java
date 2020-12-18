package org.jsonplayback.player.hibernate.entities;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="DETAIL_A")
public class DetailAEnt {
	@EmbeddedId()
	@AssociationOverride(name="masterA", joinColumns={@JoinColumn(name="DTLA_MTRA_ID", columnDefinition="INTEGER")})
	@AttributeOverride(name="subId", column=@Column(name="DTLA_SUB_ID", columnDefinition="INTEGER"))
	private DetailACompId compId;
	@Column(name="DTLA_VCHAR_A", columnDefinition="VARCHAR(200)")
	private String vcharA;
	@Column(name="DTLA_VCHAR_B", columnDefinition="VARCHAR(200)")
	private String vcharB;
	@Column(name="DTLA_HB_VERSION",columnDefinition="INTEGER")
	@Version
	private Integer hbVersion;
	@Embedded
	@AssociationOverrides({
		@AssociationOverride(
			name="masterB",
			joinColumns={
				@JoinColumn(name="DTLA_MTRB_ID_A_COMPONENT", columnDefinition="INTEGER"),
				@JoinColumn(name="DTLA_MTRB_ID_B_COMPONENT", columnDefinition="INTEGER"),
			}
		),
		@AssociationOverride(
			name="detailACompComp.masterB",
			joinColumns={
				@JoinColumn(name="DTLA_MTRB_ID_A_COMPONENT", columnDefinition="INTEGER", insertable=false, updatable=false),
				@JoinColumn(name="DTLA_MTRB_ID_B_COMPONENT", columnDefinition="INTEGER", insertable=false, updatable=false)
			}
		)
	})
	@AttributeOverrides({
		@AttributeOverride(name="vcharA", column=@Column(name="DTLA_VCHAR_A_COMPONENT", columnDefinition="VARCHAR(200)")),
		@AttributeOverride(name="vcharB", column=@Column(name="DTLA_VCHAR_B_COMPONENT", columnDefinition="VARCHAR(2000)")),
		@AttributeOverride(name="blobA", column=@Column(name="DTLA_BLOB_A_COMPONENT", columnDefinition="BLOB")),
		@AttributeOverride(name="blobB", column=@Column(name="DTLA_BLOB_B_COMPONENT", columnDefinition="BLOB")),		
	})
	private DetailAComp detailAComp;
	
	public DetailAComp getDetailAComp() {
		return detailAComp;
	}
	public Integer getHbVersion() {
		return hbVersion;
	}
	public void setHbVersion(Integer hbVersion) {
		this.hbVersion = hbVersion;
	}
	public DetailACompId getCompId() {
		return compId;
	}
	public void setCompId(DetailACompId compId) {
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
	public void setDetailAComp(DetailAComp detailAComp) {
		this.detailAComp = detailAComp;
	}
}
