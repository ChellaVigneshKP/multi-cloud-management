package com.multicloud.vms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CloudAccount")
@Getter
@Setter
public class CloudAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cloudProvider;

    @Column(columnDefinition = "TEXT")
    private String credentials; // Encrypted credentials

    @Column
    private String region;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}