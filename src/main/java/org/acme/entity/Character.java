package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sorcerer")
public class Character extends PanacheEntity {

    @NotBlank
    @Size(max = 150)
    public String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    public Rank rank;

    @ManyToOne
    @JoinColumn(name = "clan_id")
    public Clan clan;

    @ManyToMany
    @JoinTable(name = "character_technique",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "technique_id"))
    public List<Technique> techniques = new ArrayList<>();

    @OneToOne(mappedBy = "owner")
    public DomainExpansion domainExpansion;

    public Character() {}

    public Character(String name, Rank rank, Clan clan, List<Technique> techniques, DomainExpansion domainExpansion) {
        this.name = name;
        this.rank = rank;
        this.clan = clan;
        if (techniques != null) this.techniques = techniques;
        this.domainExpansion = domainExpansion;
    }
}