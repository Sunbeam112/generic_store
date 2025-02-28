package ua.sunbeam.genericstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.sql.Timestamp;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalUser getLocalUser() {
        return localUser;
    }

    public void setLocalUser(LocalUser localUser) {
        this.localUser = localUser;
    }

    public Timestamp getExpiryDateInMilliseconds() {
        return expiryDateInMilliseconds;
    }

    public void setExpiryDateInMilliseconds(Timestamp createdDate) {
        this.expiryDateInMilliseconds = createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public int compareTo(ResetPasswordToken rpt) {
        if (getExpiryDateInMilliseconds() == null || rpt.getExpiryDateInMilliseconds() == null) {
            throw new IllegalArgumentException("Date can't be null");
        }

        return getExpiryDateInMilliseconds().compareTo(rpt.getExpiryDateInMilliseconds());
    }
}