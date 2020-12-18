package org.jsonplayback.player;

public class TapeAction {
	
	private String ownerSignatureStr;
	private Long ownerCreationId;
	private Long ownerCreationRefId;
	private String settedSignatureStr;
	private Long settedCreationId;
	private Long settedCreationRefId;
	private String ownerPlayerType;
	private TapeActionType actionType;
	private String fieldName;
	
	private Tape tapeOwner;

	public String getOwnerSignatureStr() {
		return ownerSignatureStr;
	}

	public void setOwnerSignatureStr(String ownerSignatureStr) {
		this.ownerSignatureStr = ownerSignatureStr;
	}

	public Long getOwnerCreationId() {
		return ownerCreationId;
	}

	public void setOwnerCreationId(Long ownerCreationId) {
		this.ownerCreationId = ownerCreationId;
	}

	public Long getOwnerCreationRefId() {
		return ownerCreationRefId;
	}

	public void setOwnerCreationRefId(Long ownerCreationRefId) {
		this.ownerCreationRefId = ownerCreationRefId;
	}

	public String getSettedSignatureStr() {
		return settedSignatureStr;
	}

	public void setSettedSignatureStr(String settedSignatureStr) {
		this.settedSignatureStr = settedSignatureStr;
	}

	public Long getSettedCreationId() {
		return settedCreationId;
	}

	public void setSettedCreationId(Long settedCreationId) {
		this.settedCreationId = settedCreationId;
	}

	public Long getSettedCreationRefId() {
		return settedCreationRefId;
	}

	public void setSettedCreationRefId(Long settedCreationRefId) {
		this.settedCreationRefId = settedCreationRefId;
	}

	public String getOwnerPlayerType() {
		return ownerPlayerType;
	}

	public void setOwnerPlayerType(String ownerPlayerType) {
		this.ownerPlayerType = ownerPlayerType;
	}

	public TapeActionType getActionType() {
		return actionType;
	}

	public void setActionType(TapeActionType actionType) {
		this.actionType = actionType;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Tape getTapeOwner() {
		return tapeOwner;
	}

	public void setTapeOwner(Tape tapeOwner) {
		this.tapeOwner = tapeOwner;
	}
	
	
}
/*gerando conflito*/