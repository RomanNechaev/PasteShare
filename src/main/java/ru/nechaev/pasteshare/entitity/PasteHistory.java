package ru.nechaev.pasteshare.entitity;

import org.hibernate.envers.RevisionType;

public record PasteHistory(Paste paste, RevisionType revisionType) {
}
