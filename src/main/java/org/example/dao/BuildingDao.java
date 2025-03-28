package org.example.dao;

import org.example.entity.Apartment;
import org.example.entity.Building;
import org.example.entity.Employee;
import org.example.entity.Resident;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.example.configuration.SessionFactoryUtil;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class BuildingDao {
    public void create(Building building) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(building);
            transaction.commit();
        }
    }

    public Building getById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.get(Building.class, id);
        }
    }

    public List<Building> getAll() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Building", Building.class).list();
        }
    }

    public List<Building> getByCompanyId(long companyId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Building b JOIN FETCH b.company WHERE b.id = :companyId", Building.class)
                    .setParameter("companyId", companyId)
                    .list();
        }
    }

    public void update(Building building) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(building);
            transaction.commit();
        }
    }

    public void deleteById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Building building = session.get(Building.class, id);
            if (building != null) {
                session.remove(building);
            }
            transaction.commit();
        }
    }

    public static Set<Apartment> getBuildingApartments(long id) {
        Building building;
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            building = session.createQuery(
                            "select b from Building b " +
                                    "join fetch b.apartments " +
                                    "where b.id = :id",
                            Building.class)
                    .setParameter("id", id)
                    .getSingleResult();
            transaction.commit();
        }
        return building.getApartments();
    }

    /**
     * Get the count of apartments in a building
     * @param buildingId the ID of the building
     * @return the number of apartments
     */
    public int getApartmentCountInBuilding(long buildingId) {
        Set<Apartment> apartments = getBuildingApartments(buildingId);
        return apartments.size();
    }
    
    /**
     * Get all residents living in a building
     * @param buildingId the ID of the building
     * @return List of Resident objects
     */
    public List<Resident> getResidentsInBuilding(long buildingId) {
        List<Resident> allResidents = new ArrayList<>();
        
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Set<Apartment> apartments = getBuildingApartments(buildingId);
            
            ResidentDao residentDao = new ResidentDao();
            for (Apartment apartment : apartments) {
                List<Resident> apartmentResidents = residentDao.getByApartmentId(apartment.getId());
                allResidents.addAll(apartmentResidents);
            }
        }
        
        return allResidents;
    }
    
    /**
     * Get the count of residents in a building
     * @param buildingId the ID of the building
     * @return the number of residents
     */
    public int getResidentCountInBuilding(long buildingId) {
        return getResidentsInBuilding(buildingId).size();
    }
}
