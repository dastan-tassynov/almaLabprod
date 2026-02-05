package kg.almalab.meddocs.controller;
import kg.almalab.meddocs.model.User;
import kg.almalab.meddocs.model.UserRole;
import kg.almalab.meddocs.model.dto.LoginRequest;
import kg.almalab.meddocs.model.dto.LoginResponse;
import kg.almalab.meddocs.repo.UserRepo;
import kg.almalab.meddocs.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/auth")
public class AuthController {
    private final UserRepo userRepo;
    private final PasswordEncoder encoder;
    private final AuthService authService;

    public AuthController(UserRepo repo, PasswordEncoder enc, AuthService authService) {
        this.userRepo = repo;
        this.encoder = enc;
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
    @PostMapping("/register")
    public User register(@RequestBody Map<String,String> req) {

        User u = new User();
        u.setFullName(req.get("fullName"));
        u.setUsername(req.get("username"));
        u.setPassword(encoder.encode(req.get("password")));
        u.setRole(UserRole.USER);

        return userRepo.save(u);
    }

    @PostMapping("/create-admins")
    public String setupAdmins() {

        if (userRepo.findByUsername("myrzagalieva") == null) {
            User a = new User();
            a.setFullName("Мырзагалиева");
            a.setUsername("myrzagalieva");
            a.setPassword(encoder.encode("admin123"));
            a.setRole(UserRole.ADMIN);
            userRepo.save(a);
        }

        if (userRepo.findByUsername("asylbekov") == null) {
            User s = new User();
            s.setFullName("Асылбеков");
            s.setUsername("asylbekov");
            s.setPassword(encoder.encode("super123"));
            s.setRole(UserRole.SUPERADMIN);
            userRepo.save(s);
        }

        return "Admins created";
    }
}
