package ua.sunbeam.genericstore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.error.PasswordResetCooldown;
import ua.sunbeam.genericstore.model.DAO.ResetPasswordTokenRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.ResetPasswordToken;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class RPTService {

    @Value("${expiry_in_msec}")
    private int expiryInMillisecond;

    public RPTService(ResetPasswordTokenRepository resetPasswordTokenRepository) {
        this.rptRepository = resetPasswordTokenRepository;
    }

    ResetPasswordTokenRepository rptRepository;


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
        return !resetPasswordToken.getExpiryDateInMilliseconds().after(new Timestamp(System.currentTimeMillis()));
    }

    public boolean ValidateRPT(LocalUser user, ResetPasswordToken token) {
        if (IsRTPNotExpired(token)) {
            return user.getResetPasswordTokens().contains(token);
        }
        return false;
    }

    public ResetPasswordToken GenerateRPT(LocalUser user) {
        ResetPasswordToken rpt = new ResetPasswordToken();
        rpt.setExpiryDateInMilliseconds(new Timestamp(System.currentTimeMillis() + expiryInMillisecond));
        rpt.setLocalUser(user);
        rpt.setToken(UUID.randomUUID().toString());
        rptRepository.save(rpt);
        user.addResetPasswordToken(rpt);
        return rpt;
    }
}
