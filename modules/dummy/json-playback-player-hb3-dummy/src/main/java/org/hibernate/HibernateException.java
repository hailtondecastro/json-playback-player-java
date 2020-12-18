package org.hibernate;

/**
 * The base {@link Throwable} type for Hibernate.
 * <p/>
 * Note that all {@link java.sql.SQLException SQLExceptions} will be wrapped in some form of 
 * {@link JDBCException}.
 * 
 * @see JDBCException
 * 
 * @author Gavin King
 */
public class HibernateException extends RuntimeException {

	public HibernateException(Throwable root) {
		super(root);
	}

	public HibernateException(String string, Throwable root) {
		super(string, root);
	}

	public HibernateException(String s) {
		super(s);
	}
}