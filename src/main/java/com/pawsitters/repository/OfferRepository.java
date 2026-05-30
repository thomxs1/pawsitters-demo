package com.pawsitters.repository;

import com.pawsitters.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByRequestId(Long requestId);
    List<Offer> findByHostId(Long hostId);
}
