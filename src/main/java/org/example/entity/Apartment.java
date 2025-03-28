package org.example.entity;
import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "apartment_number")
    private String apartmentNumber;

    @Column(nullable = false)
    private double area;

    @Column(name = "have_pet")
    private boolean havePet;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    @ManyToOne
    private Owner owner;

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL)
    private Set<Resident> residents;

    public Apartment() {
    }

    public Apartment(String apartmentNumber, double area, boolean havePet, Building building, Owner owner) {
        this.apartmentNumber = apartmentNumber;
        this.area = area;
        this.havePet = havePet;
        this.building = building;
        this.owner = owner;
    }

    public long getId() {
        return id;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public boolean isHavePet() {
        return havePet;
    }

    public void setHavePet(boolean havePet) {
        this.havePet = havePet;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Apartment{" +
                "id=" + id +
                ", apartmentNumber='" + apartmentNumber + '\'' +
                ", area=" + area +
                ", havePet=" + havePet +
                ", building=" + building +
                ", owner=" + owner +
                '}';
    }
}
