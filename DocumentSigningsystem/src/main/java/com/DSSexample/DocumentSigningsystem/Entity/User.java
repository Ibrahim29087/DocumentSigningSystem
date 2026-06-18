package com.DSSexample.DocumentSigningsystem.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.annotation.Role;

import java.time.LocalDateTime;
import java.util.List;

/*
Without @Builder

You create objects like this:

User user = new User(
        "1",
        "Ali Khan",
        "ali@gmail.com",
        "12345",
        Role.USER,
        null,
        null,
        null,
        null
);

Problem:

confusing parameter order
hard to read
difficult when class has many fields
        With @Builder

You can create objects like this:
User user = User.builder()
        .fullName("Ali Khan")
        .email("ali@gmail.com")
        .password("12345")
        .role(Role.USER)
        .build();
*/

@Entity
@Table(name="users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(name="id",updatable = false,nullable = false,length = 36)
    private String id;

    @Column(name = "full_name",nullable = false)
    private String fullName;

    @Column(name = "email",nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false)
    private Role role;

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Document> documents;

    @PrePersist
    protected void onCreate(){
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt=LocalDateTime.now();
    }
    public enum Role
    {
        ADMIN,USER
    }
}
