package org.example.dao;

import org.example.dto.PersonDto;
import org.example.entity.Building;
import org.example.entity.Company;
import org.example.entity.Employee;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.example.configuration.SessionFactoryUtil;

import java.util.List;
import java.util.Set;

public class CompanyDao {
    public void create(Company company) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            String company_name =company.getName();
            List<Company> existingCompany = this.getByName(company_name);

            if (!existingCompany.isEmpty()) {
                throw new RuntimeException("Company with name '" + company_name + "' already exists");
            }

            Transaction transaction = session.beginTransaction();
            session.persist(company);
            transaction.commit();
        }
    }

    public Company getById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.get(Company.class, id);
        }
    }

    public List<Company> getByName(String name) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Company WHERE name = :name", Company.class)
                    .setParameter("name", name)
                    .list();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve company by name: " + name, e);
        }
    }

    public List<Company> getAll() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Company", Company.class).list();
        }
    }

    public void update(Company company) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(company);
            transaction.commit();
        }
    }

    public void delete(Company company) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(company);
            transaction.commit();
        }
    }

    public void deleteById(long id) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Company company = session.get(Company.class, id);
            if (company != null) {
                session.remove(company);
            }
            transaction.commit();
        }
    }

    public static Set<Building> getCompanyBuildings(long id) {
        Company company;
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            company = session.createQuery(
                            "select c from Company c " +
                                    "join fetch c.buildings " +
                                    "where c.id = :id",
                            Company.class)
                    .setParameter("id", id)
                    .getSingleResult();
            transaction.commit();
        }
        return company.getBuildings();
    }

    public static List<PersonDto> getCompanyEmployeesDTO(long company_id) {
        List<PersonDto> employees;
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();
            employees = session.createQuery(
                            "select new org.example.dto.PersonDto(e.id, e.name) " +
                                    "from Employee e " +
                                    "join e.company c " +
                                    "where c.id = :id",
                            PersonDto.class)
                    .setParameter("id", company_id)
                    .getResultList();
            transaction.commit();
        }
        return employees;
    }

    public static Boolean assignBuildingToCompany(long buildingId, long companyId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Company company = session.get(Company.class, companyId);
            Building building = session.get(Building.class, buildingId);

            if (company != null && building != null) {
                company.getBuildings().add(building);
                building.setCompany(company);

                Set<Employee> employees = company.getEmployees();
                if (employees != null && !employees.isEmpty()) {
                    // Find employee with the fewest assigned buildings
                    Employee selectedEmployee = employees.stream()
                            .min((e1, e2) -> {
                                int count1 = e1.getManagedBuildings() == null ? 0 : e1.getManagedBuildings().size();
                                int count2 = e2.getManagedBuildings() == null ? 0 : e2.getManagedBuildings().size();
                                return Integer.compare(count1, count2);
                            })
                            .orElse(null);

                    if (selectedEmployee != null) {
                        if (selectedEmployee.getManagedBuildings() == null) {
                            selectedEmployee.setManagedBuildings(new java.util.HashSet<>());
                        }

                        // Assign building to this employee
                        selectedEmployee.getManagedBuildings().add(building);
                        building.setAssignedEmployee(selectedEmployee);
                    }
                }

                transaction.commit();
                return true;
            }
            transaction.commit();
            return false;
        }
    }

    public static Boolean removeBuildingFromCompany(long buildingId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Building building = session.get(Building.class, buildingId);
            if (building != null) {
                Company company = building.getCompany();
                if (company != null) {
                    company.getBuildings().remove(building);
                    building.setCompany(null);
                    transaction.commit();
                    return true;
                }
            }
            transaction.commit();
            return false;
        }
    }

    public static Boolean assignEmployeeToCompany(long employeeId, long companyId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Company company = session.get(Company.class, companyId);
            Employee employee = session.get(Employee.class, employeeId);
            if (company != null && employee != null) {
                company.getEmployees().add(employee);
                employee.setCompany(company);
                transaction.commit();
                return true;
            }
            transaction.commit();
            return false;
        }
    }

    public static Boolean removeEmployeeFromCompany(long employeeId, long companyId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Company company = session.get(Company.class, companyId);
            Employee employee = session.get(Employee.class, employeeId);
            if (company != null && employee != null) {
                company.getEmployees().remove(employee);
                employee.setCompany(null);
                employee.getManagedBuildings().clear();
                transaction.commit();
                return true;
            }
            transaction.commit();
            return false;
        }
    }

    public static Boolean assignBuildingToEmployee(long buildingId, long employeeId) {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Employee employee = session.get(Employee.class, employeeId);
            Building building = session.get(Building.class, buildingId);

            if (employee != null && building != null) {
                Company employeeCompany = employee.getCompany();
                Company buildingCompany = building.getCompany();

                if (employeeCompany == null || buildingCompany == null) {
                    transaction.rollback();
                    System.out.println("Companies don't match");
                    return false;
                }

                if (employeeCompany.getId() != buildingCompany.getId()) {
                    transaction.rollback();
                    System.out.println("Companies don't match");
                    return false;
                }

                employee.getManagedBuildings().add(building);
                building.setAssignedEmployee(employee);
                transaction.commit();
                return true;
            }
            transaction.commit();
            return false;
        }
    }
    public List<Company> getCompaniesByRevenueCollected() {
        try (Session session = SessionFactoryUtil.getSessionFactory().openSession()) {
            String hql = "SELECT c, SUM(f.amount) AS revenue " +
                    "FROM Company c " +
                    "JOIN c.buildings b " +
                    "JOIN b.apartments a " +
                    "JOIN a.residents r " +
                    "JOIN Fee f ON f.resident.id = r.id " +
                    "WHERE f.isPaid = true " +
                    "GROUP BY c " +
                    "ORDER BY revenue DESC";

            return session.createQuery(hql, Object[].class)
                    .getResultList()
                    .stream()
                    .map(result -> (Company) result[0])
                    .toList();
        }
    }
}
