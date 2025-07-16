package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import ua.sunbeam.genericstore.api.model.LoginBody;
import ua.sunbeam.genericstore.api.model.RegistrationBody;
import ua.sunbeam.genericstore.api.security.JWTUtils;
import ua.sunbeam.genericstore.error.*;
import ua.sunbeam.genericstore.model.DAO.UserRepository;
import ua.sunbeam.genericstore.model.DAO.VerificationTokenRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.ResetPasswordToken;
import ua.sunbeam.genericstore.model.VerificationToken;
import ua.sunbeam.genericstore.service.EmailService.EmailVerificationService;
import ua.sunbeam.genericstore.service.EmailService.ResetPasswordEmailService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    //5 minutes
    private static final int COOLDOWN_IN_MS = 300000;
    final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EncryptionService encryptionService;
    private final RPTService rptService;
    private final JWTUtils jwtUtils;
    private final EmailVerificationService emailVerificationService;
    private final ResetPasswordEmailService resetPasswordEmailService;

    public UserService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository, EncryptionService encryptionService, RPTService rptService, JWTUtils jwtUtils, EmailVerificationService emailVerificationService, ResetPasswordEmailService resetPasswordEmailService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.encryptionService = encryptionService;
        this.rptService = rptService;
        this.jwtUtils = jwtUtils;
        this.emailVerificationService = emailVerificationService;
        this.resetPasswordEmailService = resetPasswordEmailService;
    }

    public String loginUser(@Valid LoginBody body) throws UserNotVerifiedException, EmailFailureException {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(body.getEmail());
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (encryptionService.decryptPassword(body.getPassword(), user.getPassword())) {
                if (user.isEmailVerified()) {
                    return jwtUtils.generateToken(user.getUsername());
                } else {
                    List<VerificationToken> verificationTokens = user.getVerificationTokens();
                    boolean resend = verificationTokens.isEmpty() ||
                            verificationTokens.getFirst()
                                    .getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis() - COOLDOWN_IN_MS));
                    if (resend) {
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenRepository.save(verificationToken);
                        emailVerificationService.sendEmailConformationMessage(verificationToken);
                    }
                    throw new UserNotVerifiedException(resend);
                }

            }
        }
        return null;

    }

    @Transactional
    public void registerUser(@Valid @RequestBody RegistrationBody body, BindingResult result)
            throws UserAlreadyExist, EmailFailureException {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(body.getEmail());
        if (opUser.isPresent()) {
            throw new UserAlreadyExist();
        }

        LocalUser user = new LocalUser();
        user.setEmail(body.getEmail());
        user.setPassword(encryptionService.encryptPassword(body.getPassword()));
        userRepository.save(user);
        VerificationToken token = createVerificationToken(user);
        verificationTokenRepository.save(token);
        emailVerificationService.sendEmailConformationMessage(token);

    }

    private VerificationToken createVerificationToken(LocalUser user) {
        VerificationToken token = new VerificationToken();
        token.setToken(jwtUtils.generateToken(user.getEmail()));
        token.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        token.setLocalUser(user);
        user.getVerificationTokens().add(token);
        return token;
    }

    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> opToken = verificationTokenRepository.findByToken(token);
        if (opToken.isPresent()) {
            VerificationToken verificationToken = opToken.get();
            LocalUser user = verificationToken.getLocalUser();
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                userRepository.save(user);
                verificationTokenRepository.deleteByLocalUser(user);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean setUserPasswordByEmail(String email,
                                          @NotNull @NotBlank @Size(min = 8, max = 64) String password,
                                          BindingResult result) throws EmailsNotVerifiedException {
        if (result.hasErrors()) {
            return false;
        }
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (user.isEmailVerified()) {
                user.setPassword(encryptionService.encryptPassword(password));
                userRepository.save(user);
                return true;
            } else {
                throw new EmailsNotVerifiedException();
            }
        }
        return false;
    }

    public boolean isUserExistsByEmail(String email) {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(email);
        return opUser.isPresent();
    }

    public boolean isUserExistsByID(Long id) {
        Optional<LocalUser> opUser = userRepository.findById(id);
        return opUser.isPresent();
    }


    public boolean isUserEmailVerified(String email) {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            return user.isEmailVerified();
        }
        return false;
    }

    public void trySendResetPasswordEmail(String email) throws EmailsNotVerifiedException, EmailFailureException, PasswordResetCooldown {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (user.isEmailVerified()) {
                try {
                    ResetPasswordToken rpt = rptService.tryToCreateRPT(user);
                    resetPasswordEmailService.sendResetPasswordEmail(rpt);
                    return;
                } catch (EmailFailureException ex) {
                    throw new EmailFailureException();
                } catch (PasswordResetCooldown ex) {
                    throw new PasswordResetCooldown();
                }

            } else {
                throw new EmailsNotVerifiedException();
            }
        }

    }


    public LocalUser getUserByID(Long id) {
        Optional<LocalUser> opUser = userRepository.findById(id);
        return opUser.orElse(null);
    }

    public Optional<LocalUser> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }
}
