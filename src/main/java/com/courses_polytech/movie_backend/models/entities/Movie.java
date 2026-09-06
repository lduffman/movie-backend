package com.courses_polytech.movie_backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder(toBuilder = true)
@Entity
@Table(name = "movies")
public class Movie extends AuditDateEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 20)
    @NotNull
    @Column(name = "external_id", length = 20, unique = true, nullable = false)
    private String externalId;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "year", nullable = false)
    private Integer year;

    @Builder.Default
    @NotNull
    @Column(name = "genres", columnDefinition = "text[]")
    private List<String> genres = new ArrayList<>();

    @Column(name = "rating", columnDefinition = "NUMERIC(3,1)")
    private BigDecimal rating;

    @Column(name = "votes")
    private Integer votes;

    @Column(name = "runtime")
    private Integer runtime;

    @Builder.Default
    @NotNull
    @Column(name = "directors", columnDefinition = "text[]")
    private List<String> directors = new ArrayList<>();

    @Builder.Default
    @NotNull
    @Column(name = "writers", columnDefinition = "text[]")
    private List<String> writers = new ArrayList<>();

    @Size(max = 1000)
    @Column(name = "poster_url", length = 1000)
    private String posterUrl;

    @Column(name = "plot", columnDefinition = "text")
    private String plot;
}
