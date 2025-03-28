package org.example.dto;

public class PersonDto {
    private long id;
    private String name;

    public PersonDto(long id, String name){
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "EmployeeDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
