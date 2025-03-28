package org.example.entity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Resident extends Person {
    @ManyToOne
    private Apartment apartment;

    public Resident(String name, Gender gender, LocalDate dateOfBirth) {
        super(name, gender, dateOfBirth);
    }

    public Resident() {
        super();
    }

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }
}
