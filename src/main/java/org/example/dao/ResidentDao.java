package org.example.dao;

import org.example.entity.Fee;
import org.example.entity.Resident;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.example.configuration.SessionFactoryUtil;
import java.util.List;

public class ResidentDao {
    public void create(Resident resident) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(resident);
            transaction.commit();
        }
    }

    public Resident getById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.get(Resident.class, id);
        }
    }

    public List<Resident> getAll() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Resident", Resident.class).list();
        }
    }

    public List<Resident> getByApartmentId(long apartmentId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Resident WHERE apartment.id = :apartmentId", Resident.class)
                    .setParameter("apartmentId", apartmentId)
                    .list();
        }
    }

    public void update(Resident resident) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(resident);
            transaction.commit();
        }
    }

    public void deleteById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Resident resident = session.get(Resident.class, id);
            if (resident != null) {
                session.remove(resident);
            }
            transaction.commit();
        }
    }

    public List<Fee> getResidentFees(long residentId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Fee WHERE resident.id = :residentId", Fee.class)
                    .setParameter("residentId", residentId)
                    .list();
        }
    }

    public List<Resident> getResidentsSortedByName() {
        return getResidentsSortedByName(true);
    }

    public List<Resident> getResidentsSortedByName(boolean ascending_order) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            String orderDirection = ascending_order ? "ASC" : "DESC";
            return session.createQuery("FROM Resident r ORDER BY r.name " + orderDirection, Resident.class)
                    .getResultList();
        }
    }

    public List<Resident> getResidentsSortedByAge() {
        return getResidentsSortedByAge(true);
    }

    public List<Resident> getResidentsSortedByAge(boolean ascending_order) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            String orderDirection = ascending_order ? "ASC" : "DESC";
            return session.createQuery("FROM Resident r ORDER BY r.dateOfBirth " + orderDirection, Resident.class)
                    .getResultList(); // If ascending=true: oldest first, if ascending=false: youngest first
        }
    }
}
