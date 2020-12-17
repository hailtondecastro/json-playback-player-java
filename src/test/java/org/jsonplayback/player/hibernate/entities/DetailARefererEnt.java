package org.jsonplayback.player.hibernate.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="DETAIL_A_REFERER")
public class DetailARefererEnt {
	@Id()
	@Column(name="DTAR_ID", columnDefinition="INTEGER")
	private Integer id;
	
	@Column(name="DTAR_VCHAR_A", columnDefinition="VARCHAR(200)")
	private String vcharA;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="DTAR_DTLA_MTRA_ID", columnDefinition="INTEGER"),
		@JoinColumn(name="DTAR_DTLA_SUB_ID", columnDefinition="INTEGER")
	})
	private DetailAEnt detailA;

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

	public DetailAEnt getDetailA() {
		return detailA;
	}

	public void setDetailA(DetailAEnt detailA) {
		this.detailA = detailA;
	}
}
