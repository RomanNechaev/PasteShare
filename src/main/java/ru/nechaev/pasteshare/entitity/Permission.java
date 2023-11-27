package ru.nechaev.pasteshare.entitity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "paste_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Paste paste;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Permission(User user, Paste paste) {
        this.user = user;
        this.paste = paste;
    }
}
