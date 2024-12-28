package org.mhacioglu.peaktrackserver.repository;

import org.mhacioglu.peaktrackserver.model.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {
    @Query("SELECT b FROM BlacklistedToken b WHERE b.expiryDate < ?1")
    List<BlacklistedToken> findExpiredTokens(Instant now);
}
