package org.example.dao;

import org.example.entity.Apartment;
import org.example.entity.Company;
import org.example.entity.Employee;
import org.example.entity.Owner;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.example.configuration.SessionFactoryUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OwnerDao {
    public void create(Owner owner) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(owner);
            transaction.commit();
        }
    }

    public Owner getById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.get(Owner.class, id);
        }
    }

    public List<Owner> getAll() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Owner", Owner.class).list();
        }
    }

    public void update(Owner owner) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(owner);
            transaction.commit();
        }
    }

    public void deleteById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Owner owner = session.get(Owner.class, id);
            if (owner != null) {
                session.remove(owner);
            }
            transaction.commit();
        }
    }

    public static Set<Apartment> getOwnerApartments(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            List<Owner> owners = session.createQuery(
                            "select o from Owner o " +
                                    "left join fetch o.apartments " +
                                    "where o.id = :id",
                            Owner.class)
                    .setParameter("id", id)
                    .getResultList();
            transaction.commit();
            System.out.println(owners);

            if (owners.isEmpty()) {
                return Collections.emptySet();  // Return an empty set if no owner is found
            }
            return owners.get(0).getApartments();
        }
    }
}