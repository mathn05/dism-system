package com.csdlpt.web.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "station")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Station {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "is_headquarter", nullable = false)
    private Boolean isHeadquarter;
}