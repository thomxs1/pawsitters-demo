package com.pawsitters.repository;

import com.pawsitters.model.CareRequest;
import com.pawsitters.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareRequestRepository extends JpaRepository<CareRequest, Long> {
    List<CareRequest> findByStatus(RequestStatus status);
    List<CareRequest> findByPetOwnerId(Long ownerId);
}
