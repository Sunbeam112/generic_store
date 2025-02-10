package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;
import ua.sunbeam.genericstore.api.model.LoginBody;
import ua.sunbeam.genericstore.api.model.RegistrationBody;
import ua.sunbeam.genericstore.api.security.JWTUtils;
import ua.sunbeam.genericstore.error.EmailFailureException;
import ua.sunbeam.genericstore.error.UserAlreadyExist;
import ua.sunbeam.genericstore.error.UserNotVerifiedException;
import ua.sunbeam.genericstore.model.DAO.UserRepository;
import ua.sunbeam.genericstore.model.DAO.VerificationTokenRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.VerificationToken;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    //5 minutes
    private final int COOLDOWN_IN_MS = 300000;

    final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EncryptionService encryptionService;
    private final JWTUtils jwtUtils;

    public UserService(UserRepository userRepository, EmailService emailService, VerificationTokenRepository verificationTokenRepository,
                       EncryptionService encryptionService, JWTUtils jwtUtils) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.encryptionService = encryptionService;
        this.jwtUtils = jwtUtils;

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
                        emailService.sendEmailConformationMessage(verificationToken);
                    }
                    throw new UserNotVerifiedException(resend);
                }

            }
        }
        return null;

    }

    public LocalUser registerUser(@Valid @RequestBody RegistrationBody body)
            throws UserAlreadyExist, EmailFailureException, MethodArgumentNotValidException {
        Optional<LocalUser> opUser = userRepository.findByEmailIgnoreCase(body.getEmail());
        if (opUser.isPresent()) {
            throw new UserAlreadyExist();
        }

        LocalUser user = new LocalUser();
        user.setEmail(body.getEmail());
        user.setPassword(encryptionService.encryptPassword(body.getPassword()));
        VerificationToken token = createVerificationToken(user);
        emailService.sendEmailConformationMessage(token);
        return userRepository.save(user);

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
}
