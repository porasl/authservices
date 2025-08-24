package com.porasl.authservices.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;
    private String confirmationPassword;
    private String userEmail; /*** can be ignored if user itself asks for password change */

    // Manual getters to ensure compilation
    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword() { return newPassword; }
    public String getConfirmationPassword() { return confirmationPassword; }
    public String getUserEmail() { return userEmail; }
    
    // Manual setters
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public void setConfirmationPassword(String confirmationPassword) { this.confirmationPassword = confirmationPassword; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}