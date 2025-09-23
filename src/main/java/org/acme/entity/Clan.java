package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clan")
public class Clan extends PanacheEntity {

    @NotBlank
    @Size(max = 120)
    @Column(unique = true, nullable = false)
    public String name;

    @Column(nullable = false, length = 1000)
    @Size(max = 1000)
    public String description;

    @OneToMany(mappedBy = "clan")
    public List<Character> members = new ArrayList<>();

    public Clan(){}

    public Clan(String name, String description){
        this.name = name;
        this.description = description;
    }
}
