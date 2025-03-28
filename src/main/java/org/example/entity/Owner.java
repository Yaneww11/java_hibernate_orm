package org.example.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
public class Owner extends Person {
    @OneToMany(mappedBy = "owner")
    private Set<Apartment> apartments;

    public Owner(String name, Gender gender, LocalDate dateOfBirth) {
        super(name, gender, dateOfBirth);
    }

    public Owner() {
        super();
    }

    public Set<Apartment> getApartments() {
        return apartments;
    }

    public void setApartment(Set<Apartment> apartments) {
        this.apartments = apartments;
    }
}