package org.example;

import org.example.configuration.SessionFactoryUtil;
import org.example.dao.*;
import org.example.dto.PersonDto;
import org.example.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        SessionFactoryUtil.getSessionFactory().openSession();
        
        System.out.println("========== ELECTRONIC PROPERTY MANAGER DEMO ==========");

        demonstrateCompanyOperations();
        demonstrateEmployeeOperations();
        demonstrateBuildingOperations();
        demonstrateApartmentOperations();
        demonstrateOwnerOperations();
        demonstrateResidentOperations();
        demonstrateFeeOperations();
        
        System.out.println("========== DEMO COMPLETED ==========");
    }
    
    private static void demonstrateCompanyOperations() {
        System.out.println("\n----- COMPANY OPERATIONS -----");
        
        CompanyDao companyDao = new CompanyDao();
        
        // Read operations
        System.out.println("Listing all companies:");
        List<Company> companies = companyDao.getAll();
        companies.forEach(c -> System.out.println("- " + c.getName() + " (ID: " + c.getId() + ")"));
        
        if (!companies.isEmpty()) {
            Company company = companyDao.getById(21);
            System.out.println("\nSelected company: " + company.getName());
            
            // Get company by name
            System.out.println("\nGetting company by name:");
            List<Company> foundCompanies = companyDao.getByName(company.getName());
            foundCompanies.forEach(c -> System.out.println("- " + c.getName()));
            
            // Get company buildings
            System.out.println("\nCompany buildings:");
            Set<Building> buildings = CompanyDao.getCompanyBuildings(company.getId());
            buildings.forEach(b -> System.out.println("- " + b.getAddress()));
            
            // Get company employees as DTOs
            System.out.println("\nCompany employees (DTO):");
            List<PersonDto> employeeDtos = CompanyDao.getCompanyEmployeesDTO(company.getId());
            employeeDtos.forEach(e -> System.out.println("- " + e.getName()));
            
            // Get companies by revenue collected
            System.out.println("\nCompanies ordered by revenue collected:");
            List<Company> revenueCompanies = companyDao.getCompaniesByRevenueCollected();
            revenueCompanies.forEach(c -> System.out.println("- " + c.getName()));
            
            // Update example
            System.out.println("\nUpdating company: " + company.getName());
            String oldName = company.getName();
            company.setName(oldName + " (Updated)");
            companyDao.update(company);
            System.out.println("Updated to: " + company.getName());
            
            // Restore original name
            company.setName(oldName);
            companyDao.update(company);
            System.out.println("Restored to: " + company.getName());
            
            // Actually demonstrate assignment operations
            if (!buildings.isEmpty() && !employeeDtos.isEmpty()) {
                Building building = buildings.iterator().next();
                long employeeId = employeeDtos.get(0).getId();
                
                System.out.println("\nDemonstrating assignment operations:");
                
                // Assign building to company (even though it might already be assigned)
                boolean buildingAssigned = CompanyDao.assignBuildingToCompany(building.getId(), company.getId());
                String success = "  * Building " + building.getId() + " is now assigned to " + company.getName();
                System.out.println("- assignBuildingToCompany: " + (buildingAssigned ? success : "Failed"));
                System.out.println(CompanyDao.getCompanyBuildings(company.getId()));
                
                // Remove building from company and then reassign it
                boolean buildingRemoved = CompanyDao.removeBuildingFromCompany(building.getId());
                success = "  * Building " + building.getId() + " is now removed from " + company.getName();
                System.out.println("- removeBuildingFromCompany: " + (buildingRemoved ? success : "Failed"));
                System.out.println(CompanyDao.getCompanyBuildings(company.getId()));
                
                if (buildingRemoved) {
                    // Re-assign the building to the company
                    buildingAssigned = CompanyDao.assignBuildingToCompany(building.getId(), company.getId());
                    System.out.println("- Re-assignBuildingToCompany: " + (buildingAssigned ? "Success" : "Failed"));
                    System.out.println(CompanyDao.getCompanyBuildings(company.getId()));
                }
                
                // Demonstrate employee assignment
                EmployeeDao employeeDao = new EmployeeDao();
                Employee employee = employeeDao.getById(employeeId);
                if (employee != null) {
                    Company currentCompany = employee.getCompany();
                    
                    // Assign employee to company (temporarily)
                    boolean employeeAssigned = CompanyDao.assignEmployeeToCompany(employeeId, company.getId());
                    success = "  * Employee " + employee.getName() + " is now assigned to " + company.getName();
                    System.out.println("- assignEmployeeToCompany: " + (employeeAssigned ? success : "Failed"));
                    
                    // Assign building to employee
                    boolean buildingToEmployee = CompanyDao.assignBuildingToEmployee(building.getId(), employeeId);
                    success = "  * Building " + building.getId() + " is now assigned to employee " + employee.getName();
                    System.out.println("- assignBuildingToEmployee: " + (buildingToEmployee ? success : "Failed"));
                    
                    // Restore original employee company if needed
                    if (currentCompany != null && currentCompany.getId() != company.getId()) {
                        boolean employeeRestored = CompanyDao.assignEmployeeToCompany(employeeId, currentCompany.getId());
                        success = "  * Employee " + employee.getName() + " is now restored to original company " + currentCompany.getName();
                        System.out.println("- Restored employee to original company: " + (employeeRestored ? success : "Failed"));
                    }
                }
            }
        }
    }
    
    private static void demonstrateEmployeeOperations() {
        System.out.println("\n----- EMPLOYEE OPERATIONS -----");
        
        EmployeeDao employeeDao = new EmployeeDao();
        
        // Read operations
        System.out.println("Listing all employees:");
        List<Employee> employees = employeeDao.getAll();
        employees.forEach(e -> System.out.println("- " + e.getName() + " (ID: " + e.getId() + ")"));
        
        if (!employees.isEmpty()) {
            Employee employee = employees.get(0);
            System.out.println("\nSelected employee: " + employee.getName());
            
            // Get employee by ID
            System.out.println("\nGetting employee by ID:");
            Employee retrievedEmployee = employeeDao.getById(employee.getId());
            System.out.println("Retrieved: " + retrievedEmployee.getName());
            
            // Get employee's managed buildings
            System.out.println("\nEmployee's managed buildings:");
            try {
                Set<Building> managedBuildings = EmployeeDao.getEmployeeManagedBuildings(employee.getId());
                managedBuildings.forEach(b -> System.out.println("- " + b.getAddress()));
            } catch (Exception e) {
                System.out.println("No managed buildings found or error occurred");
            }
            
            // Get employees sorted by name
            System.out.println("\nEmployees sorted by name:");
            List<Employee> sortedEmployees = employeeDao.getEmployeesSortedByName();
            sortedEmployees.forEach(e -> System.out.println("- " + e.getName()));
            
            // Get employees by buildings count
            System.out.println("\nEmployees ordered by buildings count:");
            List<Employee> buildingCountEmployees = employeeDao.getEmployeesByBuildingsCount();
            buildingCountEmployees.forEach(e -> System.out.println("- " + e.getName()));
            
            // If employee has a company, demonstrate other methods
            if (employee.getCompany() != null) {
                long companyId = employee.getCompany().getId();
                
                // Get buildings count by employee in company
                System.out.println("\nBuildings count by employee in company:");
                Map<Employee, Integer> buildingsCountMap = employeeDao.getBuildingsCountByEmployeeInCompany(companyId);
                buildingsCountMap.forEach((e, count) -> System.out.println("- " + e.getName() + ": " + count + " buildings"));
                
                // Get detailed buildings by employee in company
                System.out.println("\nDetailed buildings by employee in company:");
                Map<Employee, Set<Building>> detailedMap = employeeDao.getDetailedBuildingsByEmployeeInCompany(companyId);
                detailedMap.forEach((e, buildings) -> {
                    System.out.println("- " + e.getName() + ": " + buildings.size() + " buildings");
                    buildings.forEach(b -> System.out.println("  * " + b.getAddress()));
                });
            }
            
            // Update example
            System.out.println("\nUpdating employee: " + employee.getName());
            String oldName = employee.getName();
            employee.setName(oldName + " (Updated)");
            employeeDao.update(employee);
            System.out.println("Updated to: " + employee.getName());
            
            // Restore original name
            employee.setName(oldName);
            employeeDao.update(employee);
            System.out.println("Restored to: " + employee.getName());
        }
    }
    
    private static void demonstrateBuildingOperations() {
        System.out.println("\n----- BUILDING OPERATIONS -----");
        
        BuildingDao buildingDao = new BuildingDao();
        
        // Read operations
        System.out.println("Listing all buildings:");
        List<Building> buildings = buildingDao.getAll();
        buildings.forEach(b -> System.out.println("- " + b.getAddress() + " (ID: " + b.getId() + ")"));
        
        if (!buildings.isEmpty()) {
            Building building = buildings.get(0);
            System.out.println("\nSelected building: " + building.getAddress());
            
            // Get building by ID
            System.out.println("\nGetting building by ID:");
            Building retrievedBuilding = buildingDao.getById(building.getId());
            System.out.println("Retrieved: " + retrievedBuilding.getAddress());
            
            // Get company ID if available and get buildings by company
            if (building.getCompany() != null) {
                long companyId = building.getCompany().getId();
                System.out.println("\nBuildings by company ID " + companyId + ":");
                List<Building> companyBuildings = buildingDao.getByCompanyId(companyId);
                System.out.println(companyBuildings);
            }

            // Get apartment count in building
            System.out.println("\nApartment count in building: " +
                    buildingDao.getApartmentCountInBuilding(building.getId()));

            Set<Apartment> apartments = BuildingDao.getBuildingApartments(building.getId());
            apartments.forEach(a -> System.out.println("- Apartment #" + a.getApartmentNumber()));

            // Get resident count in building
            System.out.println("\nResident count in building: " +
                    buildingDao.getResidentCountInBuilding(building.getId()));

            List<Resident> residents = buildingDao.getResidentsInBuilding(building.getId());
            residents.forEach(r -> System.out.println("- " + r.getName()));
            
            // Update example
            System.out.println("\nUpdating building: " + building.getAddress());
            String oldAddress = building.getAddress();
            building.setAddress(oldAddress + " (Updated)");
            buildingDao.update(building);
            System.out.println("Updated to: " + building.getAddress());
            
            // Restore original address
            building.setAddress(oldAddress);
            buildingDao.update(building);
            System.out.println("Restored to: " + building.getAddress());
        }
    }
    
    private static void demonstrateApartmentOperations() {
        System.out.println("\n----- APARTMENT OPERATIONS -----");
        
        ApartmentDao apartmentDao = new ApartmentDao();
        
        // Read operations
        System.out.println("Listing all apartments:");
        List<Apartment> apartments = apartmentDao.getAll();
        apartments.forEach(a -> System.out.println("- Apartment #" + a.getApartmentNumber() + " (ID: " + a.getId() + ")"));
        
        if (!apartments.isEmpty()) {
            Apartment apartment = apartments.get(0);
            System.out.println("\nSelected apartment: #" + apartment.getApartmentNumber());
            
            // Get apartment by ID
            System.out.println("\nGetting apartment by ID:");
            Apartment retrievedApartment = apartmentDao.getById(apartment.getId());
            System.out.println("Retrieved: Apartment #" + retrievedApartment.getApartmentNumber());
            
            // Get building ID if available and get apartments by building
            if (apartment.getBuilding() != null) {
                long buildingId = apartment.getBuilding().getId();
                System.out.println("\nApartments by building ID " + buildingId + ":");
                List<Apartment> buildingApartments = apartmentDao.getByBuildingId(buildingId);
                buildingApartments.forEach(a -> System.out.println("- Apartment #" + a.getApartmentNumber()));
            }
            
            // Get apartment residents as DTOs
            System.out.println("\nApartment residents (DTO):");
            List<PersonDto> residentDtos = ApartmentDao.getApartmentResidentsDTO(apartment.getId());
            residentDtos.forEach(r -> System.out.println("- " + r.getName()));

        }
    }
    
    private static void demonstrateOwnerOperations() {
        System.out.println("\n----- OWNER OPERATIONS -----");
        
        OwnerDao ownerDao = new OwnerDao();
        
        // Read operations
        System.out.println("Listing all owners:");
        List<Owner> owners = ownerDao.getAll();
        owners.forEach(o -> System.out.println("- " + o.getName() + " (ID: " + o.getId() + ")"));
        
        if (!owners.isEmpty()) {
            Owner owner = owners.get(0);
            System.out.println("\nSelected owner: " + owner.getName());
            
            // Get owner by ID
            System.out.println("\nGetting owner by ID:");
            Owner retrievedOwner = ownerDao.getById(owner.getId());
            System.out.println("Retrieved: " + retrievedOwner.getName());
            
            // Get owner apartments
            System.out.println("\nOwner's apartments:");
            Set<Apartment> ownerApartments = OwnerDao.getOwnerApartments(owner.getId());
            ownerApartments.forEach(a -> System.out.println("- Apartment #" + a.getApartmentNumber()));
            
            // Update example
            System.out.println("\nUpdating owner: " + owner.getName());
            String oldName = owner.getName();
            owner.setName(oldName + " (Updated)");
            ownerDao.update(owner);
            System.out.println("Updated to: " + owner.getName());
            
            // Restore original name
            owner.setName(oldName);
            ownerDao.update(owner);
            System.out.println("Restored to: " + owner.getName());
        }
    }
    
    private static void demonstrateResidentOperations() {
        System.out.println("\n----- RESIDENT OPERATIONS -----");
        
        ResidentDao residentDao = new ResidentDao();
        
        // Read operations
        System.out.println("Listing all residents:");
        List<Resident> residents = residentDao.getAll();
        residents.forEach(r -> System.out.println("- " + r.getName() + " (ID: " + r.getId() + ")"));
        
        if (!residents.isEmpty()) {
            Resident resident = residents.get(0);
            System.out.println("\nSelected resident: " + resident.getName());
            
            // Get resident by ID
            System.out.println("\nGetting resident by ID:");
            Resident retrievedResident = residentDao.getById(resident.getId());
            System.out.println("Retrieved: " + retrievedResident.getName());
            
            // Get apartment ID if available and get residents by apartment
            if (resident.getApartment() != null) {
                long apartmentId = resident.getApartment().getId();
                System.out.println("\nResidents by apartment ID " + apartmentId + ":");
                List<Resident> apartmentResidents = residentDao.getByApartmentId(apartmentId);
                apartmentResidents.forEach(r -> System.out.println("- " + r.getName()));
            }
            
            // Get resident fees
            System.out.println("\nResident fees:");
            List<Fee> fees = residentDao.getResidentFees(resident.getId());
            fees.forEach(f -> System.out.println("- Amount: " + f.getAmount() + ", Due: " + f.getDueDate() + ", Paid: " + f.isPaid()));
            
            // Get residents sorted by name (ascending)
            System.out.println("\nResidents sorted by name (ascending):");
            List<Resident> nameAscResidents = residentDao.getResidentsSortedByName(true);
            nameAscResidents.forEach(r -> System.out.println("- " + r.getName()));
            
            // Get residents sorted by name (descending)
            System.out.println("\nResidents sorted by name (descending):");
            List<Resident> nameDescResidents = residentDao.getResidentsSortedByName(false);
            nameDescResidents.forEach(r -> System.out.println("- " + r.getName()));
            
            // Get residents sorted by age (ascending)
            System.out.println("\nResidents sorted by age (ascending - oldest first):");
            List<Resident> ageAscResidents = residentDao.getResidentsSortedByAge(true);
            ageAscResidents.forEach(r -> System.out.println("- " + r.getName() + " (" + r.getDateOfBirth() + ")"));
            
            // Get residents sorted by age (descending)
            System.out.println("\nResidents sorted by age (descending - youngest first):");
            List<Resident> ageDescResidents = residentDao.getResidentsSortedByAge(false);
            ageDescResidents.forEach(r -> System.out.println("- " + r.getName() + " (" + r.getDateOfBirth() + ")"));
            
            // Update example
            System.out.println("\nUpdating resident: " + resident.getName());
            String oldName = resident.getName();
            resident.setName(oldName + " (Updated)");
            residentDao.update(resident);
            System.out.println("Updated to: " + resident.getName());
            
            // Restore original name
            resident.setName(oldName);
            residentDao.update(resident);
            System.out.println("Restored to: " + resident.getName());
        }
    }
    
    private static void demonstrateFeeOperations() {
        System.out.println("\n----- FEE OPERATIONS -----");
        
        FeeDao feeDao = new FeeDao();
        
        // Read operations
        System.out.println("Listing all fees:");
        List<Fee> fees = feeDao.getAll();
        fees.forEach(f -> System.out.println("- ID: " + f.getId() + ", Amount: " + f.getAmount() + 
            ", Due: " + f.getDueDate() + ", Paid: " + f.isPaid()));
        
        if (!fees.isEmpty()) {
            Fee fee = fees.get(0);
            System.out.println("\nSelected fee: ID " + fee.getId() + ", Amount: " + fee.getAmount());
            
            // Get fee by ID
            System.out.println("\nGetting fee by ID:");
            Fee retrievedFee = feeDao.getById(fee.getId());
            System.out.println("Retrieved: ID " + retrievedFee.getId() + ", Amount: " + retrievedFee.getAmount());
            
            // Get unpaid fees
            System.out.println("\nUnpaid fees:");
            List<Fee> unpaidFees = feeDao.getUnpaidFees();
            unpaidFees.forEach(f -> System.out.println("- ID: " + f.getId() + ", Amount: " + f.getAmount()));
            
            // Get overdue fees
            System.out.println("\nOverdue fees:");
            List<Fee> overdueFees = feeDao.getOverdueFees();
            overdueFees.forEach(f -> System.out.println("- ID: " + f.getId() + ", Amount: " + f.getAmount() + 
                ", Due Date: " + f.getDueDate()));
            
            // Get fee by resident
            if (fee.getResident() != null) {
                long residentId = fee.getResident().getId();
                System.out.println("\nFees for resident ID " + residentId + ":");
                List<Fee> residentFees = feeDao.getFeesByResident(residentId);
                residentFees.forEach(f -> System.out.println("- ID: " + f.getId() + ", Amount: " + f.getAmount() + ", Paid: " + f.isPaid()));
                
                // Calculate fee for resident
                System.out.println("\nCalculated fee for resident(" + fee.getResident().getId() + "):");
                BigDecimal calculatedFee = feeDao.calculateFeeForResident(fee.getResident());
                System.out.println(calculatedFee);
            }
            
            // Get amounts to be paid by company
            System.out.println("\nAmounts to be paid by company:");
            Map<Company, BigDecimal> companyAmounts = feeDao.getAmountsToBePaidByCompany();
            companyAmounts.forEach((company, amount) -> 
                System.out.println("- Company: " + company.getName() + ", Amount: " + amount));
            
            // Get amounts to be paid by building
            System.out.println("\nAmounts to be paid by building:");
            Map<Building, BigDecimal> buildingAmounts = feeDao.getAmountsToBePaidByBuilding();
            buildingAmounts.forEach((building, amount) -> 
                System.out.println("- Building: " + building.getAddress() + ", Amount: " + amount));
            
            // Get amounts to be paid by resident
            System.out.println("\nAmounts to be paid by resident:");
            Map<Resident, BigDecimal> residentAmounts = feeDao.getAmountsToBePaidByResident();
            residentAmounts.forEach((resident, amount) -> 
                System.out.println("- Resident: " + resident.getName() + ", Amount: " + amount));
            
            // Get paid amounts by resident
            System.out.println("\nPaid amounts by resident:");
            Map<Resident, BigDecimal> paidAmounts = feeDao.getPaidAmountsByResident();
            paidAmounts.forEach((resident, amount) -> 
                System.out.println("- Resident: " + resident.getName() + ", Amount: " + amount));


            // Actually demonstrate generating monthly fees
//            System.out.println("\nDemonstrating monthly fee generation:");
//            int feeCountBefore = feeDao.getAll().size();
//            System.out.println("Fees before generation: " + feeCountBefore);
//
//            feeDao.generateMonthlyFees();
//
//            int feeCountAfter = feeDao.getAll().size();
//            System.out.println("Fees after generation: " + feeCountAfter);
//            System.out.println("New fees generated: " + (feeCountAfter - feeCountBefore));
//            System.out.println("Due date of new fees: " + LocalDate.now().plusMonths(1));
            
            // Actually demonstrate marking a fee as paid
            System.out.println("\nDemonstrating fee payment:");
            // Find an unpaid fee to demonstrate payment
            Fee unpaidFee = null;
            for (Fee f : unpaidFees) {
                if (!f.isPaid()) {
                    unpaidFee = f;
                    break;
                }
            }
            
            if (unpaidFee != null) {
                System.out.println("Found unpaid fee ID: " + unpaidFee.getId() + " with amount: " + unpaidFee.getAmount());
                boolean paymentResult = feeDao.markFeeAsPaid(unpaidFee.getId());
                System.out.println("markFeeAsPaid result: " + (paymentResult ? "Success" : "Failed"));
                
                // Verify the fee is now paid
                Fee verifyFee = feeDao.getById(unpaidFee.getId());
                System.out.println("Fee is now paid: " + verifyFee.isPaid() + 
                                   ", Paid date: " + verifyFee.getPaidDate());
            } else {
                System.out.println("No unpaid fees found to demonstrate payment");
            }
            

        }
    }
}
