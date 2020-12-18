package org.jsonplayback.player.hibernate;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.jsonplayback.player.util.ReflectionUtil;

public class CriteriaCompatBase<R> implements CriteriaCompat<R> {
	private EntityManager entityManager;
	private Session session;
	private Criteria criteria;
	private CriteriaBuilder criteriaBuilder;
	private CriteriaQuery<R> criteriaQuery;
	private Root<R> root;
	private Class<R> rootClass;

	
	@Override
	public EntityManager getEntityManager() {
		return entityManager;
	}
	@Override
	public Session getSession() {
		return session;
	}
	@Override
	public Criteria getCriteria() {
		return criteria;
	}
	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return criteriaBuilder;
	}
	@Override
	public CriteriaQuery<R> getCriteriaQuery() {
		return criteriaQuery;
	}

	@Override
	public Root<R> getRoot() {
		return root;
	}

	@Override
	public Class<R> getRootClass() {
		return rootClass;
	}

	public CriteriaCompatBase(EntityManager entityManager, Class<R> rootClass) {
		super();
		this.entityManager = entityManager;
		this.session = entityManager.unwrap(Session.class);
		this.rootClass = rootClass;
		this.criteriaBuilder = this.entityManager.getCriteriaBuilder();
		this.criteriaQuery = this.criteriaBuilder.createQuery(rootClass);
		this.root = this.criteriaQuery.from(rootClass);
	}
	
	public CriteriaCompatBase(Session session, Class<R> rootClass) {
		super();
		this.rootClass = rootClass;
		this.session = session;
		
		if (isJpa()) {
			throw new RuntimeException("Wrong contructor, using Hibernate 6. Use CriteriaCompatBase(EntityManager, Class<R>)");
		}
		
		this.criteria = (Criteria) ReflectionUtil.runByReflection(
				Session.class.getName(),
				"createCriteria",
				new String[]{
					Class.class.getName()
				},
				this.session,
				new Object[]{
					this.rootClass
				});
	}

	private Boolean isJpaPriv = null;
	private boolean isJpa() {
		if (this.isJpaPriv == null) {
			Class<?> sessionClass = this.session.getClass();
			for (Method method : sessionClass.getMethods()) {
				if (method.getName().equals("createCriteria")) {
					this.isJpaPriv = false;
					break;
				}
			}
			if (this.isJpaPriv == null) {
				this.isJpaPriv = true;
			}
		}
		return this.isJpaPriv;
	}
	
	@Override
	public <P> CriteriaCompat<R> add(CriterionCompat<R, P> criterion) {
		if (this.isJpa()) {
			this.criteriaQuery = criterion.applyToCriteria(this);
		} else {
			this.criteria = this.criteria.add(criterion.toClassicCriterion());
		}
		return this;
	}
	@SuppressWarnings("unchecked")
	@Override
	public CriteriaCompat<R> addOrder(OrderCompat order) {
		if (this.isJpa()) {
			this.criteriaQuery = (CriteriaQuery<R>) order.applyToJpaCriteria(this);
		} else {
			this.criteria = order.applyToHbCriteria(this);
		}
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<R> list() {
		if (this.isJpa()) {
			return this.entityManager.createQuery(criteriaQuery).getResultList();
		} else {
			return (List<R>)this.criteria.list();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public R uniqueResult() {
		if (this.isJpa()) {
			return this.entityManager.createQuery(criteriaQuery).getSingleResult();
		} else {
			return (R)this.criteria.uniqueResult();
		}
	}
}
