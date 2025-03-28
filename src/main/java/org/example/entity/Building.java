package org.example.entity;
import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String address;

    @ManyToOne
    private Company company;

    @ManyToOne
    private Employee assignedEmployee;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
    private Set<Apartment> apartments;

    private boolean has_elevator;

    public boolean isHas_elevator() {
        return has_elevator;
    }

    public void setHas_elevator(boolean has_elevator) {
        this.has_elevator = has_elevator;
    }

    public Building() {
    }

    public Building(String address) {
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Employee getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(Employee assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public Set<Apartment> getApartments() {
        return apartments;
    }

    public void setApartments(Set<Apartment> apartments) {
        this.apartments = apartments;
    }

    @Override
    public String toString() {
        return "Building{" +
                ", address='" + address + '\'' +
                ", id=" + id +
                '}';
    }
}
