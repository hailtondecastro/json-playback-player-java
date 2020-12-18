package org.jsonplayback.player.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReflectionUtil {
	static {
	}

	private static WeakReference<Map<String, Class<?>>> primitiveTypesWR = new WeakReference<Map<String,Class<?>>>(null);
	private static Map<String, Class<?>> getPrimitiveTypes() {
		Map<String, Class<?>> primitiveTypes = ReflectionUtil.primitiveTypesWR.get();
		if (primitiveTypes == null) {
			primitiveTypes = new HashMap<>();
			primitiveTypes.put(int.class.getName(), int.class);
			primitiveTypes.put(boolean.class.getName(), boolean.class);
			primitiveTypes.put(long.class.getName(), long.class);
			ReflectionUtil.primitiveTypesWR = new WeakReference<Map<String,Class<?>>>(primitiveTypes);
		}
		return primitiveTypes;
	}
	
	public static Object runByReflection(String classStr, String methodName, String[] argsClassStrArr, Object instance, Object[] argsValues) {
		Class<?> clazz;
		try {
			clazz = Class.forName(classStr);
			Class<?>[] argsClassArr = new Class<?>[argsClassStrArr.length];
			for (int i = 0; i < argsClassStrArr.length; i++) {
				argsClassArr[i] = ReflectionUtil.correctClass(argsClassStrArr[i]);
			}
			Method method = clazz.getMethod(methodName, argsClassArr);
			return method.invoke(instance, argsValues);
		} catch (Throwable e) {
			throw new RuntimeException(
					"This should not happen. " + classStr + ", " + methodName + ", " + Arrays.toString(argsClassStrArr), e);
		}
	}
	
	public static Object instanciteByReflection(String classStr, String[] argsClassStrArr, Object[] argsValues) {
		Class<?> clazz;
		try {
			clazz = Class.forName(classStr);
			Class<?>[] argsClassArr = new Class<?>[argsClassStrArr.length];
			for (int i = 0; i < argsClassStrArr.length; i++) {
				argsClassArr[i] = ReflectionUtil.correctClass(argsClassStrArr[i]);
			}
			Constructor<?> cont = clazz.getConstructor(argsClassArr);
			return cont.newInstance(argsValues);
		} catch (Throwable e) {
			throw new RuntimeException(
					"This should not happen. " + classStr + ", " + Arrays.toString(argsClassStrArr), e);
		}
	}
	
	public static Class<?> correctClass(String name) {
		if (ReflectionUtil.getPrimitiveTypes().containsKey(name)) {
			return ReflectionUtil.getPrimitiveTypes().get(name);
		} else if (name == boolean.class.getName()) {
			return boolean.class;
		} else {
			try {
				return Class.forName(name);
			} catch (Throwable e) {
				throw new RuntimeException("This should not happen.", e);
			}
		}
	}
}
