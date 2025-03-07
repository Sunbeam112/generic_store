package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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

    final UserRepository userRepository;
    //5 minutes
    private final int COOLDOWN_IN_MS = 300000;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EncryptionService encryptionService;
    private final RPTService rptService;
    private final JWTUtils jwtUtils;
    private final EmailVerificationService emailVerificationService;
    private final ResetPasswordEmailService resetPasswordEmailService;

    public UserService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository,
                       EncryptionService encryptionService,
                       RPTService resetPasswordTokenService,
                       JWTUtils jwtUtils,
                       EmailVerificationService emailVerificationService,
                       EmailVerificationService emailVerificationService1,
                       ResetPasswordEmailService resetPasswordEmailService) {
        this.userRepository = userRepository;

        this.verificationTokenRepository = verificationTokenRepository;
        this.encryptionService = encryptionService;
        this.rptService = resetPasswordTokenService;
        this.jwtUtils = jwtUtils;
        this.emailVerificationService = emailVerificationService1;
        this.resetPasswordEmailService = resetPasswordEmailService;
    }

    public String loginUser(@Valid LoginBody body) throws Exception {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(body.getEmail());
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (encryptionService.decryptPassword(body.getPassword(), user.getPassword())) {
                if (user.isEmailVerified()) {
                    return jwtUtils.generateToken(user.getEmail());
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

    public void registerUser(@Valid @RequestBody RegistrationBody body, BindingResult result)
            throws UserAlreadyExist, EmailFailureException {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(body.getEmail());
        if (opUser.isPresent()) {
            throw new UserAlreadyExist();
        }
        List<FieldError> errors = result.getFieldErrors();
        if (result.hasErrors()) {
            return;
        }
        LocalUser user = new LocalUser();
        user.setEmail(body.getEmail());
        user.setPassword(encryptionService.encryptPassword(body.getPassword()));
        VerificationToken token = createVerificationToken(user);
        emailVerificationService.sendEmailConformationMessage(token);
        userRepository.save(user);
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


    public boolean SetUserPasswordByEmail(String email,
                                          @NotNull @NotBlank @Size(min = 8, max = 64) String password,
                                          BindingResult result) throws EmailsNotVerifiedException {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(email);
        List<FieldError> errors = result.getFieldErrors();
        if (result.hasErrors()) {
            return false;
        } else {
            if (opUser.isPresent()) {
                LocalUser user = opUser.get();
                if (user.isEmailVerified()) {
                    user.setPassword(encryptionService.encryptPassword(password));
                    return true;
                } else {
                    throw new EmailsNotVerifiedException();
                }
            }

        }
        return false;
    }

    public boolean IsUserExists(String email) {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(email);
        return opUser.isPresent();
    }

    public boolean IsUserEmailVerified(String email) {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            return user.isEmailVerified();
        }
        return false;
    }

    public ResetPasswordToken ResetPassword(String email) throws EmailsNotVerifiedException, EmailFailureException, PasswordResetCooldown {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (user.isEmailVerified()) {
                try {
                    ResetPasswordToken rpt = rptService.TryToCreateRPT(user);
                    resetPasswordEmailService.sendResetPasswordEmail(rpt);
                    return rpt;
                } catch (EmailFailureException ex) {
                    throw new EmailFailureException();
                }

            } else {
                throw new EmailsNotVerifiedException();
            }
        }
        return null;
    }

}
