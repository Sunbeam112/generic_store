package ua.sunbeam.genericstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "local_user")
public class LocalUser implements UserDetails {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Getter
    @Setter
    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;


    @Column(name = "is_email_verified", nullable = false)
    @ColumnDefault("false")
    private boolean isEmailVerified;

    @Setter
    @JsonIgnore
    @Column(name = "password", nullable = false, length = 1000)
    private String password;


    @Getter
    @JsonIgnore
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id desc")
    private List<VerificationToken> verificationTokens = new ArrayList<>();

    @Setter
    @Getter
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @Setter
    @Getter
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id desc")
    private List<ResetPasswordToken> resetPasswordTokens = new ArrayList<>();

    @Setter
    @Getter
    @OneToMany(mappedBy = "localUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserOrder> userOrders = new ArrayList<>();

    public void addResetPasswordToken(ResetPasswordToken resetPasswordToken) {
        resetPasswordTokens.add(resetPasswordToken);
    }


    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
//        return UserDetails.super.isAccountNonExpired();
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
//        return UserDetails.super.isAccountNonLocked();
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
//        return UserDetails.super.isCredentialsNonExpired();
        return true;
    }

    @Override
    public boolean isEnabled() {
//        return UserDetails.super.isEnabled();
        return true;
    }

}