package ru.nechaev.pasteshare.entitity;

import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@RevisionEntity
@Table(name = "revinfo")
public class CustomRevisionEntity {
    @Id
    @RevisionNumber
    @GeneratedValue(generator = "auditRevisionSeq")
    @SequenceGenerator(name = "auditRevisionSeq", sequenceName = "audit_revinfo_seq", schema = "public", allocationSize = 1)
    @Column(name = "rev")
    private int id;

    @RevisionTimestamp
    @Column(name = "revtstmp")
    private long timestamp;
}
