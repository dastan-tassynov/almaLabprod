package kg.almalab.meddocs.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DocumentSignature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String signerFullName;
    private String signerRole;
    private LocalDateTime signedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private TemplateDocument document;

    // ===== getters & setters =====

    public Long getId() {
        return id;
    }

    public String getSignerFullName() {
        return signerFullName;
    }

    public void setSignerFullName(String signerFullName) {
        this.signerFullName = signerFullName;
    }

    public String getSignerRole() {
        return signerRole;
    }

    public void setSignerRole(String signerRole) {
        this.signerRole = signerRole;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public TemplateDocument getDocument() {
        return document;
    }

    public void setDocument(TemplateDocument document) {
        this.document = document;
    }

}
