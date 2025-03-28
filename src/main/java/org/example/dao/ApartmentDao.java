package org.example.dao;

import org.example.dto.PersonDto;
import org.example.entity.Apartment;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.example.configuration.SessionFactoryUtil;
import java.util.List;

public class ApartmentDao {
    public void create(Apartment apartment) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(apartment);
            transaction.commit();
        }
    }

    public Apartment getById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.get(Apartment.class, id);
        }
    }

    public List<Apartment> getAll() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Apartment", Apartment.class).list();
        }
    }

    public List<Apartment> getByBuildingId(long buildingId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Apartment a JOIN FETCH  a.building WHERE a.id = :buildingId", Apartment.class)
                    .setParameter("buildingId", buildingId)
                    .list();
        }
    }

    public void update(Apartment apartment) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(apartment);
            transaction.commit();
        }
    }

    public void deleteById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Apartment apartment = session.get(Apartment.class, id);
            if (apartment != null) {
                session.remove(apartment);
            }
            transaction.commit();
        }
    }

    public static List<PersonDto> getApartmentResidentsDTO(long id) {
        List<PersonDto> residents;
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            residents = session.createQuery(
                            "select new org.example.dto.PersonDto(p.id, p.name) " +
                                    "from Resident p " +
                                    "join p.apartment c " +
                                    "where c.id = :id",
                            PersonDto.class)
                    .setParameter("id", id)
                    .getResultList();
            transaction.commit();
        }
        return residents;
    }

}