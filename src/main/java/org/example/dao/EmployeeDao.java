package org.example.dao;

import org.example.entity.Building;
import org.example.entity.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.example.configuration.SessionFactoryUtil;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class EmployeeDao {
    public void create(Employee employee) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(employee);
            transaction.commit();
        }
    }

    public Employee getById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.get(Employee.class, id);
        }
    }

    public List<Employee> getAll() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Employee", Employee.class).list();
        }
    }

    public List<Employee> getByCompanyId(long companyId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Employee e JOIN FETCH e.company c WHERE c.id = :companyId", Employee.class)
                    .setParameter("companyId", companyId)
                    .list();
        }
    }

    public void update(Employee employee) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(employee);
            transaction.commit();
        }
    }

    public void deleteById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Employee employee = session.get(Employee.class, id);
            if (employee != null) {
                session.remove(employee);
            }
            transaction.commit();
        }
    }

    public static Set<Building> getEmployeeManagedBuildings(long id) {
        Employee employee;
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            employee = session.createQuery(
                            "select e from Employee e " +
                                    "join fetch e.managedBuildings " +
                                    "where e.id = :id",
                            Employee.class)
                    .setParameter("id", id)
                    .getSingleResult();
            transaction.commit();
        }
        return employee.getManagedBuildings();
    }

    public List<Employee> getEmployeesSortedByName() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Employee e ORDER BY e.name", Employee.class)
                    .getResultList();
        }
    }

    public List<Employee> getEmployeesByBuildingsCount() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            String hql = "SELECT e, SIZE(e.managedBuildings) AS buildingCount " +
                    "FROM Employee e " +
                    "ORDER BY buildingCount DESC";

            return session.createQuery(hql, Object[].class)
                    .getResultList()
                    .stream()
                    .map(result -> (Employee) result[0])
                    .toList();
        }
    }

    /**
     * Gets a count of buildings managed by each employee in a company
     * @param companyId the ID of the company
     * @return Map with Employee objects as keys and Integer counts as values
     */
    public Map<Employee, Integer> getBuildingsCountByEmployeeInCompany(long companyId) {
        Map<Employee, Integer> result = new HashMap<>();
        
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            List<Employee> employees = getByCompanyId(companyId);
            
            for (Employee employee : employees) {
                Set<Building> buildings = getEmployeeManagedBuildings(employee.getId());
                result.put(employee, buildings.size());
            }
        }
        
        return result;
    }
    
    /**
     * Gets detailed information about buildings managed by each employee in a company
     * @param companyId the ID of the company
     * @return Map with Employee objects as keys and Sets of Building objects as values
     */
    public Map<Employee, Set<Building>> getDetailedBuildingsByEmployeeInCompany(long companyId) {
        Map<Employee, Set<Building>> result = new HashMap<>();
        
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            List<Employee> employees = getByCompanyId(companyId);
            
            for (Employee employee : employees) {
                Set<Building> buildings = getEmployeeManagedBuildings(employee.getId());
                result.put(employee, buildings);
            }
        }
        
        return result;
    }
}
