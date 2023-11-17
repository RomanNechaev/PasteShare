package ru.nechaev.pasteshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nechaev.pasteshare.entitity.Paste;

import java.util.UUID;

public interface PasteRepository extends JpaRepository<Paste, UUID> {

}
