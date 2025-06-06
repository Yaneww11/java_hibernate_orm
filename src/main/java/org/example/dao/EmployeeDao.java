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
                // Remove the employee from all managed buildings before deleting
                if (employee.getManagedBuildings() != null && !employee.getManagedBuildings().isEmpty()) {
                    // Get all buildings managed by this employee
                    Set<Building> managedBuildings = employee.getManagedBuildings();
                    // Remove the employee association from each building
                    for (Building building : managedBuildings) {
                        building.setAssignedEmployee(null);
                        session.merge(building);
                    }
                    // Clear the employee's managed buildings set
                    employee.getManagedBuildings().clear();
                }
                // Now we can safely delete the employee
                session.remove(employee);
            }
            transaction.commit();
        }
    }

    public static Set<Building> getEmployeeManagedBuildings(long employee_id) {
        Employee employee = null;
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            List<Employee> result = session.createQuery(
                            "select e from Employee e " +
                                    "join fetch e.managedBuildings " +
                                    "where e.id = :id",
                            Employee.class)
                    .setParameter("id", employee_id)
                    .getResultList();
            transaction.commit();

            if (!result.isEmpty()) {
                employee = result.get(0);
            }
        }
        return employee != null ? employee.getManagedBuildings() : Set.of();
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
