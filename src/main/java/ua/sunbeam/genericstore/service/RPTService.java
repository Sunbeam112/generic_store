package ua.sunbeam.genericstore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.error.PasswordResetCooldown;
import ua.sunbeam.genericstore.model.DAO.ResetPasswordTokenRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.ResetPasswordToken;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RPTService {

    private final ResetPasswordTokenRepository rtpRepository;
    private final ResetPasswordTokenRepository rptRepository;
    @Value("${expiry_in_msec}")
    private int expiryInMillisecond;

    public RPTService(ResetPasswordTokenRepository rtpRepository, ResetPasswordTokenRepository rptRepository) {
        this.rtpRepository = rtpRepository;
        this.rptRepository = rptRepository;
    }


    public ResetPasswordToken TryToCreateRPT(LocalUser user) {
        List<ResetPasswordToken> tokens = user.getResetPasswordTokens();
        if (tokens.isEmpty()) {
            return GenerateRPT(user);
        }
        if (IsRTPNotExpired(tokens.getLast())) throw new PasswordResetCooldown();
        else {
            return GenerateRPT(user);
        }
    }

    public boolean IsRTPNotExpired(ResetPasswordToken resetPasswordToken) {
        return resetPasswordToken.getExpiryDateInMilliseconds().after(new Timestamp(System.currentTimeMillis()));
    }

    public boolean VerifyRPT(String token_input) {
        if (token_input == null) return false;
        if (token_input.length() != 36) return false;
        ResetPasswordToken token = getTokenByToken(token_input);
        if (token == null) return false;
        if (token.getIsTokenUsed()) return false;
        if (IsRTPNotExpired(token)) {
            LocalUser user = token.getLocalUser();
            if (user.isEmailVerified()) {
                return user.getResetPasswordTokens().contains(token);
            }
        }
        return false;
    }

    public ResetPasswordToken GenerateRPT(LocalUser user) {
        ResetPasswordToken rpt = new ResetPasswordToken();
        rpt.setExpiryDateInMilliseconds(new Timestamp(System.currentTimeMillis() + expiryInMillisecond));
        rpt.setLocalUser(user);
        rpt.setToken(UUID.randomUUID().toString());

        rtpRepository.save(rpt);
        user.addResetPasswordToken(rpt);
        return rpt;
    }

    public ResetPasswordToken getTokenByToken(String token) {
        Optional<ResetPasswordToken> opToken = Optional.ofNullable(rtpRepository.getByTokenIgnoreCase(token));
        return opToken.orElse(null);
    }


    public void RemoveToken(ResetPasswordToken token) {
        if (rptRepository.existsById(token.getId()))
            rptRepository.delete(token);
    }
}
