package org.jsonplayback.hbsupport;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.hibernate.Session;

public interface CriteriaCompat<R> {
	public <P> CriteriaCompat<R> add(CriterionCompat<R, P> criterion);
	
	public CriteriaCompat<R> addOrder(OrderCompat order);
	
	public EntityManager getEntityManager();

	public Session getSession();

	public Criteria getCriteria();

	public CriteriaBuilder getCriteriaBuilder();

	public CriteriaQuery<R> getCriteriaQuery();

	public Root<R> getRoot();

	public Class<R> getRootClass();

	public List<R> list();
}
