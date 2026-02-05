package kg.almalab.meddocs.model;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String n) { fullName = n; }

    public String getUsername() { return username; }
    public void setUsername(String u) { username = u; }

    public String getPassword() { return password; }
    public void setPassword(String p) { password = p; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole r) { role = r; }
}
