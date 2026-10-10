package com.mediconnect.repository;

import com.mediconnect.entity.AccessToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<AccessToken> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);
    void deleteByTokenHash(String tokenHash);
    void deleteByExpiresAtBefore(Instant now);
}
