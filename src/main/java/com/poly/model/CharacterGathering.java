package com.poly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "character_gathering", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"character_id", "resource_type"}, name = "UK_CharGathering")
})
public class CharacterGathering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CharacterGame character;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "level", columnDefinition = "INT DEFAULT 1")
    private Integer level = 1;

    @Column(name = "exp", columnDefinition = "INT DEFAULT 0")
    private Integer exp = 0;

    @PrePersist
    protected void onPrePersist() {
        if (level == null) level = 1;
        if (exp == null) exp = 0;
    }
}