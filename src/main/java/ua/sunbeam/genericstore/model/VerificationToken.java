package ua.sunbeam.genericstore.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "verification_token")
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;


    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "local_user_id", nullable = false)
    private LocalUser localUser;

    @Column(name = "created_timestamp", nullable = false)
    private Timestamp createdTimestamp;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}