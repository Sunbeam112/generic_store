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
@Getter // Apply to all fields by default
@Setter // Apply to all fields by default
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
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.REMOVE, orphanRemoval = true) // Consider REMOVE only or no cascade
    private List<Address> addresses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id desc")
    private List<ResetPasswordToken> resetPasswordTokens = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<UserOrder> userOrders = new ArrayList<>();


    public void addVerificationToken(VerificationToken token) {
        this.verificationTokens.add(token);
        token.setLocalUser(this);
    }

    public void removeVerificationToken(VerificationToken token) {
        this.verificationTokens.remove(token);
        token.setLocalUser(null);
    }

    public void addAddress(Address address) {
        this.addresses.add(address);
        address.setLocalUser(this);
    }

    public void removeAddress(Address address) {
        this.addresses.remove(address);
        address.setLocalUser(null);
    }

    public void addResetPasswordToken(ResetPasswordToken resetPasswordToken) {
        this.resetPasswordTokens.add(resetPasswordToken);
        resetPasswordToken.setLocalUser(this);
    }

    public void removeResetPasswordToken(ResetPasswordToken resetPasswordToken) {
        this.resetPasswordTokens.remove(resetPasswordToken);
        resetPasswordToken.setLocalUser(null);
    }

    public void addUserOrder(UserOrder order) {
        this.userOrders.add(order);
        order.setLocalUser(this);
    }

    public void removeUserOrder(UserOrder order) {
        this.userOrders.remove(order);
        order.setLocalUser(null);
    }



    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO: Implement roles/authorities if needed for authorization
        return Collections.emptyList(); // Or a custom empty list implementation
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Email is used as the username for authentication
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // TODO: Implement account expiration logic if desired
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // TODO: Implement account locking logic (e.g., after multiple failed login attempts)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // TODO: Implement password expiry logic
    }

    @Override
    public boolean isEnabled() {
        // This is often tied to email verification or an admin-enabled flag
        return isEmailVerified; // Example: account is enabled only if email is verified
    }
}