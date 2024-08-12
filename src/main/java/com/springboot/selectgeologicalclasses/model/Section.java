package com.springboot.selectgeologicalclasses.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "section_id")
    private List<GeologicalClass> geologicalClasses = new ArrayList<>();

    // Default constructor
    public Section() {
        this.geologicalClasses = new ArrayList<>();
    }
}