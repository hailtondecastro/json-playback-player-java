package org.jsonplayback.hbsupport;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;

public class OrderCompat {
	private boolean ascending;
	private boolean ignoreCase;
	private String propertyName;

	/**
	 * Ascending order
	 *
	 * @param propertyName The property to order on
	 *
	 * @return The build Order instance
	 */
	public static OrderCompat asc(String propertyName) {
		return new OrderCompat( propertyName, true );
	}

	/**
	 * Descending order.
	 *
	 * @param propertyName The property to order on
	 *
	 * @return The build Order instance
	 */
	public static OrderCompat desc(String propertyName) {
		return new OrderCompat( propertyName, false );
	}

	/**
	 * Constructor for Order.  Order instances are generally created by factory methods.
	 *
	 * @see #asc
	 * @see #desc
	 */
	protected OrderCompat(String propertyName, boolean ascending) {
		this.propertyName = propertyName;
		this.ascending = ascending;
	}

	/**
	 * Should this ordering ignore case?  Has no effect on non-character properties.
	 *
	 * @return {@code this}, for method chaining
	 */
	public OrderCompat ignoreCase() {
		ignoreCase = true;
		return this;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public boolean isAscending() {
		return ascending;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}
	
	protected Criteria applyToHbCriteria(CriteriaCompat<?> criteriaCompat) {
		org.hibernate.criterion.Order order = null;
		if (this.ascending) {
			order = org.hibernate.criterion.Order.asc(this.propertyName);
		} else {
			order = org.hibernate.criterion.Order.desc(this.propertyName);
		}
		return criteriaCompat.getCriteria().addOrder(order);
	}
	
	protected CriteriaQuery<?> applyToJpaCriteria(CriteriaCompat<?> criteriaCompat) {
		List<Order> newOrderList = new ArrayList<>(criteriaCompat.getCriteriaQuery().getOrderList());
		Order jpaOrder = null;
		if (this.ascending) {
			jpaOrder = criteriaCompat.getCriteriaBuilder().asc(criteriaCompat.getRoot().get(this.propertyName));
		} else {
			jpaOrder = criteriaCompat.getCriteriaBuilder().desc(criteriaCompat.getRoot().get(this.propertyName));
		}
		newOrderList.add(jpaOrder);
		return criteriaCompat.getCriteriaQuery().orderBy(newOrderList);		
	}
}
