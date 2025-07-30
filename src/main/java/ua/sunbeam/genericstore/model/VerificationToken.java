package ua.sunbeam.genericstore.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
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



}