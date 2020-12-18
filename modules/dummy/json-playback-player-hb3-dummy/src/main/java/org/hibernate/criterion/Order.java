package org.hibernate.criterion;

import java.io.Serializable;

/**
 * Represents an order imposed upon a <tt>Criteria</tt> result set
 * @author Gavin King
 */
public class Order implements Serializable {
	public static Order asc(String propertyName) {
		return null;
	}

	public static Order desc(String propertyName) {
		return null;
	}

}
