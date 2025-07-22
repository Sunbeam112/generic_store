package ua.sunbeam.genericstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "local_user")
@Getter
@Setter
public class LocalUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @JsonIgnore // Good for not exposing this directly
    @Column(name = "is_email_verified", nullable = false)
    @ColumnDefault("false")
    private boolean isEmailVerified;

    @JsonIgnore // Essential for security
    @Column(name = "password", nullable = false, length = 1000)
    private String password;


    @JsonIgnore
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id desc")
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id desc")
    private List<ResetPasswordToken> resetPasswordTokens = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<UserOrder> userOrders = new ArrayList<>();


    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO: Implement roles/authorities if needed for authorization
        return Collections.emptyList();
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return email; // Email is used as the username for authentication
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true; // TODO: Implement account expiration logic if desired
    }


    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true; // TODO: Implement account locking logic (e.g., after multiple failed login attempts)
    }


    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // TODO: Implement password expiry logic
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        // This is often tied to email verification or an admin-enabled flag
        return isEmailVerified; // Example: account is enabled only if email is verified
    }
}