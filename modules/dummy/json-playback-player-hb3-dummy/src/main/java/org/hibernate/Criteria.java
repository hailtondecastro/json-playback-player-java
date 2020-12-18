package org.hibernate;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

public interface Criteria {

	Criteria add(Criterion classicCriterion);

	Criteria addOrder(Order order);

	List<?> list();

}
