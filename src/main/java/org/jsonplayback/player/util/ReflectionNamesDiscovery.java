package org.jsonplayback.player.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.Modifier;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

public class ReflectionNamesDiscovery {
    
    private static final Logger logger = LoggerFactory
            .getLogger(ReflectionNamesDiscovery.class);

    private static transient WeakReference<ConcurrentHashMap<Class<?>, Object>> proxyInstancesMapWR;
    
    private static <T> T getProxyInstance(Class<T> targetClass) {
        ConcurrentHashMap<Class<?>, Object> proxyInstancesMap = null;
        if (proxyInstancesMapWR == null || proxyInstancesMapWR.get() == null) {
            proxyInstancesMap = new ConcurrentHashMap<Class<?>, Object>();
            proxyInstancesMapWR = new WeakReference<ConcurrentHashMap<Class<?>, Object>>(proxyInstancesMap);
        } else {
            proxyInstancesMap = proxyInstancesMapWR.get();
        }
        if (!proxyInstancesMap.containsKey(targetClass)) {
            ProxyFactory factory = new ProxyFactory();
            if (!targetClass.isInterface())
            	factory.setSuperclass(targetClass);
            else
            	factory.setInterfaces(new Class<?>[]{ targetClass });
            factory.setFilter(methodFilter);
            Proxy proxy;
            try {
                proxy = (Proxy) factory.create(new Class<?>[0], new Object[0], methodHandler);
            } catch (NoSuchMethodException | IllegalArgumentException
                    | InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                throw new RuntimeException("Erro ao criar proxy", e);
            }
            proxyInstancesMap.put(targetClass, proxy);
        }
        return (T) proxyInstancesMap.get(targetClass);
    }
    
    
    private enum ElemntType {
        FIELD_BY_GET,
        METHOD,
        METHOD_FULL_NAME,
    }
    
    private static <T> String fieldByGetMethodPrivate(Function<T, ? extends Object> callback, Class<? extends T> targetClass) {
        T beanProxy = getProxyInstance(targetClass);
        pathEntriesTD.set(new ArrayList<>());
        lastElemntTypeTD.set(ElemntType.FIELD_BY_GET);
        lastCalledNameTD.set(null);
        
        callback.apply(beanProxy);
        
        if (logger.isTraceEnabled())
            logger.trace("retornando 'fildByGetMethod': " + lastCalledNameTD.get());
        String result = lastCalledNameTD.get();
        return result;
    }
    
    public static <T> String fieldByGetMethod(Function<T, ? extends Object> callback, Class<? extends T> targetClass) {
    	try {
    		return fieldByGetMethodPrivate(callback, targetClass);			
		} finally {
			lastCalledNameTD.set(null);
			lastElemntTypeTD.set(null);
			pathEntriesTD.set(null);
		}
    }
    
    public static <T> List<PathEntry> fieldByGetMethodEntries(Function<T, ? extends Object> callback, Class<? extends T> targetClass) {
    	try {
    		fieldByGetMethodPrivate(callback, targetClass);
    		return pathEntriesTD.get();
		} finally {
			lastCalledNameTD.set(null);
			lastElemntTypeTD.set(null);
			pathEntriesTD.set(null);
		}
    }
    
    public static <T, R> String method(Function<T, R> callback, Class<? extends T> targetClass) {
        T beanProxy = getProxyInstance(targetClass);
        lastElemntTypeTD.set(ElemntType.METHOD);
        callback.apply(beanProxy);
        
//        CopyProperties cp = null;
//        cp.addInclude(callback, targetClass)
        if (logger.isTraceEnabled())
            logger.trace("retornando 'method': " + lastCalledNameTD.get());
        return lastCalledNameTD.get();
    }
    
    public static <T, R> String methodFullName(Function<T, R> callback, Class<? extends T> targetClass) {
        T beanProxy = getProxyInstance(targetClass);
        lastElemntTypeTD.set(ElemntType.METHOD_FULL_NAME);
        callback.apply(beanProxy);
        
//        CopyProperties cp = null;
//        cp.addInclude(callback, targetClass)
        if (logger.isTraceEnabled())
            logger.trace("retornando 'methodFullName': " + lastCalledNameTD.get());
        return lastCalledNameTD.get();
    }
    
    static Pattern fieldFromMethodPattern = Pattern.compile("^get(.)(.*)$");
    static ThreadLocal<String> lastCalledNameTD = new ThreadLocal<String>();
    static ThreadLocal<ElemntType> lastElemntTypeTD = new ThreadLocal<ElemntType>();
    static ThreadLocal<List<PathEntry>> pathEntriesTD = new ThreadLocal<List<PathEntry>>();
    static MethodFilter methodFilter = new MethodFilter() {
        
        @Override
        public boolean isHandled(Method m) {
            // TODO Auto-generated method stub
            return true;
        }
    };
    static MethodHandler methodHandler = new MethodHandler() {

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed,
                Object[] args) throws Throwable {
            if (lastElemntTypeTD.get() == ElemntType.FIELD_BY_GET) {
                Matcher matcher = fieldFromMethodPattern
                        .matcher(thisMethod.getName());
                if (matcher.matches()) {
                	if (lastCalledNameTD.get() != null) {
                		lastCalledNameTD.set(lastCalledNameTD.get() + ".");
                	} else {
                		lastCalledNameTD.set("");
                	}
                	String fieldName = matcher.group(1).toLowerCase() + matcher.group(2);
                    lastCalledNameTD.set(lastCalledNameTD.get() +
                    		fieldName);
                    PathEntry pathEntry = new PathEntry();
                    pathEntry.setDirectOwnerType(self.getClass().getSuperclass());
                    pathEntry.setDirectFieldName(fieldName);
                    pathEntry.setDirectFieldType(thisMethod.getReturnType());
                    pathEntriesTD.get().add(pathEntry);
                } else {
                    throw new RuntimeException(
                            "Somente os metodos get podem ser chamados. Metodo chamado: "
                                    + thisMethod.getName());
                }
            } else if (lastElemntTypeTD.get() == ElemntType.METHOD) {
                lastCalledNameTD.set(thisMethod.getName());
            } else if (lastElemntTypeTD.get() == ElemntType.METHOD_FULL_NAME) {
                lastCalledNameTD.set(thisMethod.toString());
            }
            
            if (!Modifier
                    .isFinal(thisMethod.getReturnType().getModifiers())
                    && (thisMethod.getReturnType().isInterface()
                            || hasParameterlessPublicConstructor(
                                    thisMethod.getReturnType()))) {
                return getProxyInstance(thisMethod.getReturnType());
            }
            
            return null;
        }
    };
    
    private static boolean hasParameterlessPublicConstructor(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            // In Java 7-, use getParameterTypes and check the length of the array returned
            if (constructor.getParameterCount() == 0) { 
                return true;
            }
        }
        return false;
    }
}
