package org.jsonplayback.player.util.spring.orm.hibernate3;

import java.beans.Introspector;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.Getter;
import org.hibernate.util.ReflectHelper;

public class BasicPropertyIndexedAccessor
        extends org.hibernate.property.BasicPropertyAccessor {

    private static final Logger logger = LoggerFactory.getLogger(BasicPropertyIndexedAccessor.class);

    private static BasicIndexedGetter getGetterOrNull(Class theClass,
            String propertyName) {
        if (theClass == Object.class || theClass == null) {
            return null;
        }

        Method method = BasicPropertyIndexedAccessor.getterMethod(theClass, propertyName);

        if (method != null) {
            if (!ReflectHelper.isPublic(theClass, method)) {
                method.setAccessible(true);
            }
            return new BasicIndexedGetter(theClass, method, propertyName);
        } else {
            BasicIndexedGetter getter = BasicPropertyIndexedAccessor.getGetterOrNull(theClass.getSuperclass(),
                    propertyName);
            if (getter == null) {
                Class[] interfaces = theClass.getInterfaces();
                for (int i = 0; getter == null && i < interfaces.length; i++) {
                    getter = BasicPropertyIndexedAccessor.getGetterOrNull(interfaces[i], propertyName);
                }
            }
            return getter;
        }
    }

    @Override
    public Getter getGetter(Class theClass, String propertyName)
            throws PropertyNotFoundException {
        // TODO Auto-generated method stub
        return BasicPropertyIndexedAccessor.createGetter(theClass, propertyName);
    }

    public static Getter createGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        BasicIndexedGetter result = BasicPropertyIndexedAccessor.getGetterOrNull(theClass, propertyName);
        if (result==null) {
            throw new PropertyNotFoundException( 
                    "Could not find a getter for " + 
                    propertyName + 
                    " in class " + 
                    theClass.getName() 
            );
        }
        return result;
    }
    
    private static Method getterMethod(Class theClass, String propertyName) {
        Method[] methods = theClass.getDeclaredMethods();
        for (Method method : methods) {
            // if the method has parameters, skip it
            if (method.getParameterTypes().length != 0) {
                continue;
            }
            // if the method is a "bridge", skip it
            if (method.isBridge()) {
                continue;
            }

            final String methodName = method.getName();

            // try "get"
            if (methodName.startsWith("get")) {
                String testStdMethod = Introspector
                        .decapitalize(methodName.substring(3));
                String testOldMethod = methodName.substring(3);
                if (testStdMethod.equals(propertyName)
                        || testOldMethod.equals(propertyName)) {
                    return method;
                }
            }

            // if not "get", then try "is"
            if (methodName.startsWith("is")) {
                String testStdMethod = Introspector
                        .decapitalize(methodName.substring(2));
                String testOldMethod = methodName.substring(2);
                if (testStdMethod.equals(propertyName)
                        || testOldMethod.equals(propertyName)) {
                    return method;
                }
            }
        }

        return null;
    }
}
