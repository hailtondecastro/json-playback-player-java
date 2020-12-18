package org.jsonplayback.player;

public interface IGetBySignatureListener {
	String getName();
	<T> T onBeforeBySignature(String signature, SignatureBean signatureBean);
	<T> T onAfterBySignature(String signature, SignatureBean signatureBean, T entity);
}
/*gerando conflito*/