package org.jsonplayback.hbsupport;

import java.util.function.Function;

import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.criterion.Criterion;

public class CriterionCompatBase<R, P> implements CriterionCompat<R, P> {
	private Criterion criterion;
	private Function<CriteriaCompat<R>, CriteriaQuery<R>> createParameterCB;
	
	public CriterionCompatBase(Criterion criterion, Function<CriteriaCompat<R>, CriteriaQuery<R>> createParameterCB) {
		super();
		this.criterion = criterion;
		this.createParameterCB = createParameterCB;
	}

	@Override
	public Criterion toClassicCriterion() {
		return this.criterion;
	}

	@Override
	public CriteriaQuery<R> applyToCriteria(CriteriaCompat<R> criteriaCompat) {
		return createParameterCB.apply(criteriaCompat);
	}
}
