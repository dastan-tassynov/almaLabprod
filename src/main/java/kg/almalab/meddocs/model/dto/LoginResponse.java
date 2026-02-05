package kg.almalab.meddocs.model.dto;

public class LoginResponse {
    private String token;
    private String role;
    private String fullName;

    public LoginResponse(String token, String role, String fullName) {
        this.token = token;
        this.role = role;
        this.fullName = fullName;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }
}
