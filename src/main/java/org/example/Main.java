package org.example;

import org.example.configuration.SessionFactoryUtil;
import org.example.dao.*;
import org.example.entity.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        SessionFactoryUtil.getSessionFactory().openSession();

        // Test Company
        CompanyDao companyDao = new CompanyDao();
        Company company_object = companyDao.getById(21);
        System.out.println("Created company: " + company_object);

        // Test Employee
        EmployeeDao employeeDao = new EmployeeDao();
        Employee employee = employeeDao.getById(1);
        employee.setCompany(company_object);
        employeeDao.update(employee);
        System.out.println("Created employee: " + employee);
        System.out.println("Employee's company: " + employee.getCompany());

        BuildingDao buildingDao = new BuildingDao();
        Building building = buildingDao.getById(13);
        building.setCompany(company_object);
        buildingDao.update(building);
        System.out.println("Building's company: " + building.getCompany());

//        CompanyDao.getCompanyEmployeesDTO(21)
//                .stream()
//                .forEach(System.out::println);
//
//        CompanyDao.getCompanyBuilding(21)
//                .stream()
//                .forEach(System.out::println);
//
//        ApartmentDao.getApartmentResidentsDTO(1)
//                .stream()
//                .forEach(System.out::println);

        OwnerDao ownerDao = new OwnerDao();
        Owner owner = ownerDao.getById(1);
        ApartmentDao apartmentDao = new ApartmentDao();
        Apartment apartment = apartmentDao.getById(1);
        apartment.setOwner(owner);
        apartmentDao.update(apartment);

//        BuildingDao.getBuildingApartments(1)
//                .stream()
//                .forEach(System.out::println);
//
//        EmployeeDao.getEmployeeManagedBuildings(11)
//                .stream()
//                .forEach(System.out::println);

        ResidentDao residentDao = new ResidentDao();
        Resident resident = residentDao.getById(1);
        FeeDao feeDao = new FeeDao();

//        feeDao.generateMonthlyFees();
//        residentDao.getResidentFees(5)
//                .stream()
//                .forEach(fee -> feeDao.markFeeAsPaid(fee.getId()));
//
//        feeDao.getUnpaidFees()
//                .stream()
//                .forEach(System.out::println);

//        companyDao.getCompaniesByRevenueCollected()
//                .stream()
//                .forEach(System.out::println);
//        companyDao.assignBuildingToEmployee(3, 2);
        employeeDao.getEmployeesByBuildingsCount()
                .stream()
                .forEach(System.out::println);

//          companyDao.assignBuildingToCompany(12, 21);
//        System.out.println(companyDao.getCompanyEmployeesDTO(21));
//        Set<Building> managedBuildings = EmployeeDao.getEmployeeManagedBuildings(employee.getId());
//        System.out.println(managedBuildings);
//          companyDao.assignEmployeeToCompany(1, 21);
//          companyDao.removeBuildingFromCompany(12);
//        System.out.println(residentDao.getResidentsSortedByAge(false));

//        System.out.println(employeeDao.getBuildingsCountByEmployeeInCompany(2));
//        System.out.println(buildingDao.getResidentCountInBuilding(1));
//        System.out.println(feeDao.getAmountsToBePaidByBuilding());
//        System.out.println(feeDao.getPaidAmountsByResident());
//          System.out.println(feeDao.markFeeAsPaid(3));
// TODO: fix delete
        employeeDao.deleteById(8);
    }
}