package ua.sunbeam.genericstore.api.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PasswordResetRequestBody {
    @NotNull
    @NotBlank
    @Size(min = 8, max = 64)
    private String newPassword;
    @NotBlank
    @NotNull
    private String token;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
