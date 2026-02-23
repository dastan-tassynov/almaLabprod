package kg.almalab.meddocs.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TemplateDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private String category;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private TemplateStatus status = TemplateStatus.NEW;

    @ManyToOne
    private User user;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    @Column(name = "file_data", columnDefinition = "oid")
    private byte[] fileData;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentSignature> signatures = new ArrayList<>();

    private  String comment;

    public List<DocumentSignature> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<DocumentSignature> signatures) {
        this.signatures = signatures;
    }

    public Long getId() { return id; }

    public String getComment(){return comment;}

    public  String setComment(String c){ comment = c;
        return c;
    }

    public String getFilename() { return filename; }
    public void setFilename(String f) { filename = f; }

    public String getCategory() { return category; }
    public void setCategory(String c) { category = c; }

    public TemplateStatus getStatus() { return status; }
    public void setStatus(TemplateStatus s) { status = s; }

    public User getUser() { return user; }
    public void setUser(User u) { user = u; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] signedPdf) {
        fileData = signedPdf;
    }

    public LocalDateTime setCreatedAt(LocalDateTime created){
        return  createdAt = created;}
}
