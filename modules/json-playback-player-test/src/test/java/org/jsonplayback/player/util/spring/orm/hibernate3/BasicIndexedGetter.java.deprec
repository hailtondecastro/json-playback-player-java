package org.jsonplayback.player.util.spring.orm.hibernate3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.PropertyAccessException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.property.Getter;

/**
 * TODO: Explicar depois
 * @author Hailton de Castro
 *
 */
public class BasicIndexedGetter implements Getter {
    private static final Logger logger = LoggerFactory.getLogger(BasicIndexedGetter.class);
    
    private Class clazz;
    private final transient Method method;
    private final String propertyName;
    
    private int index = 0;

    public BasicIndexedGetter(Class clazz, Method method, String propertyName) {
        this.clazz = clazz;
        this.method = method;
        this.propertyName = propertyName;
    }

    /**
     * {@inheritDoc}
     */
    public Object get(Object target) throws HibernateException {
        try {
            if (target.getClass().isArray()) {
                Object[] objectsArr = (Object[])target;
                if (logger.isTraceEnabled())
                    logger.trace("Acabei de receber um target em formato de array:" + Arrays.deepToString(objectsArr));
                return objectsArr[this.index];
            }
            return method.invoke(target, (Object[]) null);
        } catch (InvocationTargetException ite) {
            throw new PropertyAccessException(ite,
                    "Exception occurred inside", false, clazz,
                    propertyName);
        } catch (IllegalAccessException iae) {
            throw new PropertyAccessException(iae,
                    "IllegalAccessException occurred while calling", false,
                    clazz, propertyName);
            // cannot occur
        } catch (IllegalArgumentException iae) {
            logger.error("IllegalArgumentException in class: "
                    + clazz.getName() + ", getter method of property: "
                    + propertyName);
            throw new PropertyAccessException(iae,
                    "IllegalArgumentException occurred calling", false,
                    clazz, propertyName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getForInsert(Object target, Map mergeMap,
            SessionImplementor session) {
        return get(target);
    }

    /**
     * {@inheritDoc}
     */
    public Class getReturnType() {
        return method.getReturnType();
    }

    /**
     * {@inheritDoc}
     */
    public Member getMember() {
        return method;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
        return method;
    }

    /**
     * {@inheritDoc}
     */
    public String getMethodName() {
        return method.getName();
    }

    public String toString() {
        return "BasicGetter(" + clazz.getName() + '.' + propertyName + ')';
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
	Object readResolve() {
		return BasicPropertyIndexedAccessor.createGetter(clazz, propertyName);
	}
}
