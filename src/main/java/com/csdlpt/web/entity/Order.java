package com.csdlpt.web.entity;

import com.csdlpt.web.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String id;

    @Column(nullable = false)
    private OrderType type;

    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "from_station_id")
    private Station fromStation;

    @ManyToOne
    @JoinColumn(name = "to_station_id")
    private Station toStation;
}