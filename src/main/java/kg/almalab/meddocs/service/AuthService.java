package kg.almalab.meddocs.service;

import kg.almalab.meddocs.config.JwtService;
import kg.almalab.meddocs.model.User;
import kg.almalab.meddocs.model.dto.LoginRequest;
import kg.almalab.meddocs.model.dto.LoginResponse;
import kg.almalab.meddocs.repo.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepo userRepo,
                       PasswordEncoder encoder,
                       JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepo.findByUsername(request.getUsername());
        System.out.println("password" + user.getPassword());

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generate(user);

        return new LoginResponse(
                token,
                user.getRole().name(),
                user.getFullName()
        );
    }
}
