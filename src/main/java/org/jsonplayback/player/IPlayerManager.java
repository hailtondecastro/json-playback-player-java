package org.jsonplayback.player;

import java.util.function.Function;

import org.jsonplayback.player.implementation.PlayerManagerDefault;

public interface IPlayerManager {
	String serializeSignature(SignatureBean signatureBean);

	SignatureBean deserializeSignature(String signatureStr);

	/**
	 * Return not null value if the property is annotatted with
	 * {@link LazyProperty} for a
	 * {@link IDirectRawWriterWrapper#getCallback()}.{@link IDirectRawWriter#write(java.io.OutputStream)}
	 * call after set http header with {@link LazyProperty#contentTypePrefix()}.
	 * {@link LazyProperty#charset()} must be added to content-type if
	 * {@link LazyProperty#contentTypePrefix()} is "content-type: text/plain".
	 * Example:
	 * <code>directRawWriterWrapper.getLazyProperty().contentTypePrefix() + "; " + directRawWriterWrapper.getLazyProperty().charset()</code>
	 * 
	 * @param signature
	 * @return
	 */
	IDirectRawWriterWrapper needDirectWrite(SignatureBean signature);

	<T> T getBySignature(SignatureBean signature);

//	@SuppressWarnings("rawtypes")
//	boolean isNeverSigned(Class<?> clazz);

	Object getPlayerObjectId(Object object);

	IPlayerConfig getConfig();

	PlayerManagerDefault configure(IPlayerConfig config);

	/**
	 * Threadsafe
	 */
	void startJsonWriteIntersept();

	/**
	 * Threadsafe
	 */
	void stopJsonWriteIntersept();

	boolean isStarted();

//	boolean isPersistentClassOrComponent(Class<?> clazz);

	/**
	 * Sobreescreve a configuracao temporariamente. Eh thead safe e nao afeta as
	 * demais theads. Eh descartado em {@link #stopJsonWriteIntersept()}.
	 * 
	 * @param newConfig
	 * @return
	 */
	IPlayerManager overwriteConfigurationTemporarily(IPlayerConfig newConfig);

	<T> PlayerSnapshot<T> createPlayerSnapshot(T result);

	/**
	 * Initializes the Manager. It does as the initial metadata loads hibernate
	 * and all other necessary information. Home
	 * Eh automatically called in the first {@link #startJsonWriteIntersept ()} if not
	 * has been called yet. It can be recalled at any time. Not
	 * eh thread safe.
	 * 
	 * @return
	 */
	IPlayerManager init();

	IPlayerManager cloneWithNewConfiguration(IPlayerConfig newConfig);
	
	<O> IPlayerManager registerComponentOwner(O owner, Function<O, ?> propertyFunc);
	<O, T> IPlayerManager registerComponentOwner(Class<O> ownerClass, T targetOwned, Function<O, T> propertyFunc);	
}
/*gerando conflito*/