package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "technique")
public class Technique extends PanacheEntity {

    @NotBlank
    @Column(nullable = false, unique = true)
    @Size(max = 150)
    public String name;

    @Column(nullable = false, length = 1000)
    @Size(max = 1000)
    public String description;

    @ManyToMany(mappedBy = "techniques")
    public List<Character> users = new ArrayList<>();

    public Technique(){}
    public Technique(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
