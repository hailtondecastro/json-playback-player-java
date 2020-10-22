package org.jsonplayback.player.hibernate;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Component
public class HibernateJpaCompat {

	public <R> CriteriaCompat<R> createCriteria(EntityManager em, Class<R> clazz) {
		return new CriteriaCompatBase<>(em, clazz);
	}

	public <R> CriteriaCompat<R> createCriteria(Session session, Class<R> clazz) {
		return new CriteriaCompatBase<>(session, clazz);
	}

	public Connection getConnection(Session session, EntityManager em) {
		if (session != null) {
			final AtomicReference<Connection> connRef = new AtomicReference<>();
			session.doWork(connection -> {
				connRef.set(connection);
			});
			return connRef.get();
		} else if (em != null) {
			return em.unwrap(java.sql.Connection.class);
		} else {
			throw new RuntimeException("This should not happen");
		}
	}
}
