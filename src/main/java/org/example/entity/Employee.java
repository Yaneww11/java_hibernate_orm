package org.example.entity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
public class Employee extends Person {
    @ManyToOne
    private Company company;

    @OneToMany(mappedBy = "assignedEmployee")
    private Set<Building> managedBuildings;

    public Employee() {
        super();
    }

    public Employee(String name, Gender gender, LocalDate dateOfBirth) {
        super(name, gender, dateOfBirth);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<Building> getManagedBuildings() {
        return managedBuildings;
    }

    public void setManagedBuildings(Set<Building> managedBuildings) {
        this.managedBuildings = managedBuildings;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", company=" + company +
                '}';
    }
}