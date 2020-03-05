package org.jsonplayback.hbsupport;

import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.criterion.Criterion;

public interface CriterionCompat<R, P> {
	Criterion toClassicCriterion();
	CriteriaQuery<R> applyToCriteria(CriteriaCompat<R> criteriaCompat);
}
