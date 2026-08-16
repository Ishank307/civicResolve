package com.workdna.civic.repository;

import com.workdna.civic.domain.model.IdentityMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdentityMappingRepository extends JpaRepository<IdentityMappingEntity, java.util.UUID> {

    Optional<IdentityMappingEntity> findByUserId(String userId);

    Optional<IdentityMappingEntity> findByEmail(String email);

    Optional<IdentityMappingEntity> findByDeviceFingerprint(String deviceFingerprint);

    List<IdentityMappingEntity> findByIdentityId(String identityId);
}
