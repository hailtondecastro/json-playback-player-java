package org.jsonplayback.player.util.spring.orm.hibernate3;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.property.Getter;
import org.hibernate.tuple.Tuplizer;
import org.hibernate.tuple.component.ComponentTuplizer;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.type.ComponentType;
import org.hibernate.util.StringHelper;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * TODO: Explicar depois
 * @author Hailton de Castro
 *
 */
public class CustomLocalSessionFactoryBean extends LocalSessionFactoryBean {

    @Override
    protected void postProcessConfiguration(Configuration config)
            throws HibernateException {
        super.postProcessConfiguration(config);
        this.waBasicIndexedGetterA(config);
    }

    @Override
    protected void afterSessionFactoryCreation() throws Exception {
        super.afterSessionFactoryCreation();
        this.waBasicIndexedGetterB();
    }

    /**
     * TODO: Explicar depois
     * @param config
     */
    @SuppressWarnings("unchecked")
    protected void waBasicIndexedGetterA(Configuration config) {
        for (Object persistentClassObj : IteratorUtils
                .toList(config.getClassMappings())) {
            PersistentClass persistentClass = (PersistentClass) persistentClassObj;
            this.waBasicIndexedGetterRecA(config,
                    new PropertyContainer(persistentClass, null));
        }
    }

    protected void waBasicIndexedGetterRecA(Configuration config,
            PropertyContainer propertyContainer) {
        for (Property propertyMapping : propertyContainer
                .getPropertiesIncludeId()) {
            if (StringHelper.isEmpty(propertyMapping.getPropertyAccessorName())
                    || "property".equals(
                            propertyMapping.getPropertyAccessorName())) {
                propertyMapping.setPropertyAccessorName(
                        org.jsonplayback.player.util.spring.orm.hibernate3.BasicPropertyIndexedAccessor.class
                                .getName());
            }
        }

        for (Property propertyMapping : propertyContainer
                .getPropertiesIncludeId()) {
            if (propertyMapping.isComposite()) {
                Component component = (Component) propertyMapping.getValue();
                this.waBasicIndexedGetterRecA(config,
                        new PropertyContainer(component));
            }
        }
    }

    /**
     * TODO: Explicar depois
     */
    protected void waBasicIndexedGetterB() {
        SessionFactoryImplementor sessionFactoryImp = (SessionFactoryImplementor) this
                .getSessionFactory();
        for (Object persistentClassObj : IteratorUtils
                .toList(this.getConfiguration().getClassMappings())) {
            PersistentClass persistentClass = (PersistentClass) persistentClassObj;
            this.waBasicIndexedGetterRecB(
                    new PropertyContainer(persistentClass, sessionFactoryImp));
        }
    }

    protected void waBasicIndexedGetterRecB(
            PropertyContainer propertyContainer) {

        SessionFactoryImplementor sessionFactoryImp = (SessionFactoryImplementor) this
                .getSessionFactory();
        for (EntityMode entityMode : new EntityMode[] { EntityMode.DOM4J,
                EntityMode.MAP, EntityMode.POJO }) {
            Tuplizer entityTuplizer = propertyContainer.getTuplizer(entityMode);
            Getter[] gettersArr = propertyContainer
                    .getGettersIncludeId(entityMode);
            if (entityTuplizer != null) {
                for (int i = 0; i < propertyContainer.getPropertiesIncludeId()
                        .size(); i++) {
                    if (gettersArr[i] instanceof BasicIndexedGetter) {
                        BasicIndexedGetter basicIndexedGetter = (BasicIndexedGetter) gettersArr[i];
                        basicIndexedGetter.setIndex(i);
                    }
                }
            }
        }

        Configuration config = this.getConfiguration();

        for (Property propertyMapping : propertyContainer
                .getPropertiesIncludeId()) {
            if (propertyMapping.isComposite()) {
                Component component = (Component) propertyMapping.getValue();
                this.waBasicIndexedGetterRecB(new PropertyContainer(component));
            }
        }
    }

    private class PropertyContainer {
        private PersistentClass persistentClass;
        private Component component;
        SessionFactoryImplementor sessionFactoryImp;

        public PropertyContainer(PersistentClass persistentClass,
                SessionFactoryImplementor sessionFactoryImp) {
            this.persistentClass = persistentClass;
            this.sessionFactoryImp = sessionFactoryImp;
        }

        public PropertyContainer(Component component) {
            this.component = component;
        }

        public String getEntOuClassName() {
            if (this.persistentClass != null) {
                return this.persistentClass.getEntityName();
            } else {
                return this.component.getComponentClassName();
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public List<Property> getPropertiesIncludeId() {
            ArrayList<Property> propertyListG = new ArrayList<Property>();
            ArrayList propertyList = new ArrayList();
            if (this.persistentClass != null) {
                propertyList.addAll(IteratorUtils
                        .toList(this.persistentClass.getPropertyIterator()));
                propertyList.add(this.persistentClass.getIdentifierProperty());
            }
            if (this.component != null) {
                propertyList.addAll(IteratorUtils
                        .toList(this.component.getPropertyIterator()));
            }

            for (Object propertyMappingObject : propertyList) {
                propertyListG.add((Property) propertyMappingObject);
            }

            return propertyListG;
        }

        public Tuplizer getTuplizer(EntityMode entityMode) {
            if (this.component != null)
                if (this.component.getType() instanceof ComponentType) {
                    ComponentType componentType = (ComponentType) this.component
                            .getType();
                    return componentType.getTuplizerMapping()
                            .getTuplizer(entityMode);
                } else {
                    throw new RuntimeException(
                            "'this.component' deve ser 'org.hibernate.type.ComponentType'. "
                                    + this.component.getClass().getName());
                }

            else {
                return this.sessionFactoryImp
                        .getEntityPersister(
                                this.persistentClass.getEntityName())
                        .getEntityMetamodel().getTuplizer(entityMode);
            }
        }

        public Getter[] getGettersIncludeId(EntityMode entityMode) {
            List<Getter> gettesList = new ArrayList<Getter>();
            if (this.persistentClass != null) {
                EntityTuplizer entityTuplizer = (EntityTuplizer) this
                        .getTuplizer(entityMode);
                gettesList.add(entityTuplizer.getIdentifierGetter());
                Tuplizer tuplizer = this.getTuplizer(entityMode);
                for (int i = 0; i < this.getPropertiesIncludeId().size()
                        - 1; i++) {
                    gettesList.add(tuplizer.getGetter(i));
                }

            } else {
                ComponentTuplizer componentTuplizer = (ComponentTuplizer) this
                        .getTuplizer(entityMode);
                for (int i = 0; i < this.getPropertiesIncludeId().size(); i++) {
                    gettesList.add(componentTuplizer.getGetter(i));
                }
            }
            Getter[] gettesArr = new Getter[gettesList.size()];
            return gettesList.toArray(gettesArr);
        }
    }
}
