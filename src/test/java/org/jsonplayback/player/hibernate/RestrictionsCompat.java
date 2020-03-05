package org.jsonplayback.hbsupport;

import org.hibernate.criterion.Restrictions;

public class RestrictionsCompat {
	public static CriterionCompat<?, ?> eq(String propertyName, Object value) {
		return new CriterionCompatBase<Object, Object>(
			Restrictions.eq(propertyName, value),
			(input) -> {
				input.getCriteriaQuery().where(input.getCriteriaBuilder().equal(input.getRoot().get(propertyName), value));
				return null;
			}
		);
	}
}
