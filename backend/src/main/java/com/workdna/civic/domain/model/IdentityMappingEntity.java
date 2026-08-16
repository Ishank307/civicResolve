package com.workdna.civic.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "identity_mappings")
public class IdentityMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, length = 128)
    private String userId;

    @Column(name = "identity_id", nullable = false, length = 128)
    private String identityId;

    @Column(length = 256)
    private String email;

    @Column(name = "device_fingerprint", length = 256)
    private String deviceFingerprint;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected IdentityMappingEntity() {
    }

    public static IdentityMappingEntity of(String userId, String identityId) {
        IdentityMappingEntity entity = new IdentityMappingEntity();
        entity.userId = userId;
        entity.identityId = identityId;
        return entity;
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }
}
