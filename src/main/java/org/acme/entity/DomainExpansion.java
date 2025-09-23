package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "domain_expansion")
public class DomainExpansion extends PanacheEntity {

    @NotBlank
    @Column(nullable = false, unique = true)
    @Size(max = 150)
    public String name;

    @Column(nullable = false, length = 2000)
    @Size(max = 2000)
    public String effect;

    @OneToOne
    @JoinColumn(name = "owner_id", unique = true)
    public Character owner;

    public DomainExpansion() {}

    public DomainExpansion(String name, String effect, Character owner) {
        this.name = name;
        this.effect = effect;
        this.owner = owner;
    }
}