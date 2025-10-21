package com.eaglebank.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @Column(unique = true, updatable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "address_line3")
    private String addressLine3;

    @Column(name = "address_town", nullable = false)
    private String addressTown;

    @Column(name = "address_county", nullable = false)
    private String addressCounty;

    @Column(name = "address_postcode", nullable = false)
    private String addressPostcode;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Instant createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private Instant updatedTimestamp;


    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = "usr-" + UUID.randomUUID().toString().replace("-", "");
        }
        Instant now = Instant.now();
        this.createdTimestamp = now;
        this.updatedTimestamp = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTimestamp = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
