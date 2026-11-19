package org.example.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="Equipo")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true, nullable = false, length = 50)
    private String nombre;
    private String categoria;
    private Boolean disponible;
}
