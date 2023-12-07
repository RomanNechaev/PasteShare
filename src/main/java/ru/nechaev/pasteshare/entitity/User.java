package ru.nechaev.pasteshare.entitity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "role")
    private Role role;
    private String password;

    public User(String name, String email, LocalDateTime lastLogin, Role role) {
        this.name = name;
        this.email = email;
        this.lastLogin = lastLogin;
        this.role = role;
    }
}
