package com.pawsitters.repository;

import com.pawsitters.model.PetOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetOwnerRepository extends JpaRepository<PetOwner, Long> {
    Optional<PetOwner> findByEmail(String email);
}
