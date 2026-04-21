package com.csdlpt.web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Station")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Station {

    @Id
    @Column(length = 50)
    private String id;

    @Column(length = 255)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(name = "is_headquarter")
    private Boolean headquarter;

    public boolean isHeadquarter() {
        return Boolean.TRUE.equals(headquarter);
    }
}

