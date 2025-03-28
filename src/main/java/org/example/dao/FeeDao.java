package org.example.dao;

import org.example.configuration.SessionFactoryUtil;
import org.example.entity.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FeeDao {
    private static final BigDecimal BASE_RATE_PER_SQM = new BigDecimal("1.0");
    private static final BigDecimal ELEVATOR_RATE_PER_PERSON = new BigDecimal("5.0");
    private static final BigDecimal PET_RATE = new BigDecimal("15.0");

    private static final int MIN_PERSON_AGE_FOR_ELEVATOR = 7;

    public void create(Fee fee) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(fee);
            transaction.commit();
        }
    }

    public Fee getById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.get(Fee.class, id);
        }
    }

    public List<Fee> getAll() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Fee", Fee.class).list();
        }
    }

    public void update(Fee fee) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(fee);
            transaction.commit();
        }
    }

    public void delete(Fee fee) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(fee);
            transaction.commit();
        }
    }

    public List<Fee> getUnpaidFees() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Fee WHERE isPaid = false", Fee.class).list();
        }
    }

    public List<Fee> getOverdueFees() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Fee WHERE isPaid = false AND dueDate < :today", Fee.class)
                    .setParameter("today", LocalDate.now())
                    .list();
        }
    }

    public List<Fee> getFeesByResident(long residentId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Fee WHERE resident.id = :residentId", Fee.class)
                    .setParameter("residentId", residentId)
                    .list();
        }
    }

    public boolean markFeeAsPaid(long feeId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Fee fee = session.get(Fee.class, feeId);

            if (fee != null) {
                fee.setPaid(true);
                fee.setPaidDate(LocalDate.now());
                transaction.commit();
                return true;
            }

            transaction.rollback();
            return false;
        }
    }

    public BigDecimal calculateFeeForResident(Resident resident) {
        BigDecimal totalFee = BigDecimal.ZERO;

        // Get apartment area fee
        Apartment apartment = resident.getApartment();
        if (apartment != null) {
            totalFee = totalFee.add(BASE_RATE_PER_SQM.multiply(BigDecimal.valueOf(apartment.getArea())));

            // Check if building has elevator and resident is over 7 years old
            Building building = apartment.getBuilding();
            if (building != null && building.isHas_elevator() && isOverAge(resident, MIN_PERSON_AGE_FOR_ELEVATOR)) {
                totalFee = totalFee.add(ELEVATOR_RATE_PER_PERSON);
            }

            // Add pet fee if apartment has pets
            if (apartment.isHavePet()) {
                totalFee = totalFee.add(PET_RATE);
            }
        }

        return totalFee;
    }

    private boolean isOverAge(Resident resident, int years) {
        if (resident.getDateOfBirth() == null) {
            return false;
        }

        LocalDate now = LocalDate.now();
        Period period = Period.between(resident.getDateOfBirth(), now);
        return period.getYears() > years;
    }

    public void generateMonthlyFees() {
        LocalDate dueDate = LocalDate.now().plusMonths(1);
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            ResidentDao residentDao = new ResidentDao();
            List<Resident> residents = residentDao.getAll();

            for (Resident resident : residents) {
                BigDecimal feeAmount = calculateFeeForResident(resident);

                Fee fee = new Fee(feeAmount, dueDate, resident);
                fee.setDescription("Monthly maintenance fee for " + dueDate.getMonth() + " " + dueDate.getYear());

                session.persist(fee);
            }

            transaction.commit();
        }
    }

    public Map<Company, BigDecimal> getAmountsToBePaidByCompany() {
        Map<Company, BigDecimal> result = new HashMap<>();
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            String hql = "SELECT c, SUM(f.amount) AS totalAmount " +
                    "FROM Company c " +
                    "JOIN c.buildings b " +
                    "JOIN b.apartments a " +
                    "JOIN a.residents r " +
                    "JOIN Fee f ON f.resident.id = r.id " +
                    "WHERE f.isPaid = false " +
                    "GROUP BY c";

            List<Object[]> queryResult = session.createQuery(hql, Object[].class).getResultList();
            for (Object[] row : queryResult) {
                Company company = (Company) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                result.put(company, amount);
            }
        }
        return result;
    }

    public Map<Building, BigDecimal> getAmountsToBePaidByBuilding() {
        Map<Building, BigDecimal> result = new HashMap<>();
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            String hql = "SELECT b, SUM(f.amount) AS totalAmount " +
                    "FROM Building b " +
                    "JOIN b.apartments a " +
                    "JOIN a.residents r " +
                    "JOIN Fee f ON f.resident.id = r.id " +
                    "WHERE f.isPaid = false " +
                    "GROUP BY b";

            List<Object[]> queryResult = session.createQuery(hql, Object[].class).getResultList();
            for (Object[] row : queryResult) {
                Building building = (Building) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                result.put(building, amount);
            }
        }
        return result;
    }

    public Map<Resident, BigDecimal> getAmountsToBePaidByResident() {
        Map<Resident, BigDecimal> result = new HashMap<>();
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            String hql = "SELECT r, SUM(f.amount) AS totalAmount " +
                    "FROM Resident r " +
                    "JOIN Fee f ON f.resident.id = r.id " +
                    "WHERE f.isPaid = false " +
                    "GROUP BY r";

            List<Object[]> queryResult = session.createQuery(hql, Object[].class).getResultList();
            for (Object[] row : queryResult) {
                Resident resident = (Resident) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                result.put(resident, amount);
            }
        }
        return result;
    }
}
