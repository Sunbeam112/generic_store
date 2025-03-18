package ua.sunbeam.genericstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "reset_password_token")
public class ResetPasswordToken implements Comparable<ResetPasswordToken> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "expiry_date_in_milliseconds", nullable = false)
    private Timestamp expiryDateInMilliseconds;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "local_user_id")
    private LocalUser localUser;

    @Column(name = "token", nullable = false, unique = true, length = 1024)
    private String token;

    @Column(name = "is_token_used", nullable = false)
    @ColumnDefault("false")
    private Boolean isTokenUsed = false;


    @Override
    public int compareTo(ResetPasswordToken rpt) {
        if (getExpiryDateInMilliseconds() == null || rpt.getExpiryDateInMilliseconds() == null) {
            throw new IllegalArgumentException("Date can't be null");
        }

        return getExpiryDateInMilliseconds().compareTo(rpt.getExpiryDateInMilliseconds());
    }
    
}