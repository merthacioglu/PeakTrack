package org.mhacioglu.peaktrackserver.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class BlacklistedToken {
    @Id
    private String token;
    private Instant expiryDate;


}
