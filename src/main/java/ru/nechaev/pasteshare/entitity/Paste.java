package ru.nechaev.pasteshare.entitity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "paste")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class Paste {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @Audited(targetAuditMode = NOT_AUDITED)
    private User user;
    @Column(name = "title")
    private String title;
    @Column(name = "content_location")
    @GeneratedValue
    private String contentLocation;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "expired_at")
    private LocalDateTime expiredAt = LocalDateTime.now().plusYears(1);
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "visibility")
    private Visibility visibility;
    @Column(name = "last_visited")
    private LocalDateTime lastVisited;
    @Column(name = "version")
    private Long version;

    public Paste(User user, String title, String contentLocation, LocalDateTime expiredAt, Visibility visibility, LocalDateTime lastVisited, Long version) {
        this.user = user;
        this.title = title;
        this.contentLocation = contentLocation;
        this.expiredAt = expiredAt;
        this.visibility = visibility;
        this.lastVisited = lastVisited;
        this.version = version;
    }

    public Paste(User user, String title, String contentLocation, Visibility visibility, LocalDateTime lastVisited, Long version) {
        this.user = user;
        this.title = title;
        this.contentLocation = contentLocation;
        this.visibility = visibility;
        this.lastVisited = lastVisited;
        this.version = version;
    }
}
