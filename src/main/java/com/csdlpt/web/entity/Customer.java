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
@Table(name = "Customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @Column(length = 50)
    private String id;

    @Column(length = 255)
    private String name;

    @Column(name = "phonenumber", unique = true, length = 15)
    private String phoneNumber;

    @Column(length = 255)
    private String address;
}

