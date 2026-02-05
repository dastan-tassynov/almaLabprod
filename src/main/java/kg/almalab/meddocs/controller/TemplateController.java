package kg.almalab.meddocs.controller;
import kg.almalab.meddocs.model.TemplateDocument;
import kg.almalab.meddocs.model.User;
import kg.almalab.meddocs.repo.TemplateDocRepo;
import kg.almalab.meddocs.repo.UserRepo;
import kg.almalab.meddocs.service.TemplateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/templates")
public class TemplateController {
    private final TemplateService service;
    private final TemplateDocRepo templateDocRepo;
    private final UserRepo userRepo;

    public TemplateController(TemplateService s, TemplateDocRepo templateDocRepo, UserRepo u) {
        this.service = s;
        this.templateDocRepo = templateDocRepo;
        this.userRepo = u;
    }

    private User me(Principal p) {
        return userRepo.findByUsername(p.getName());
    }

    @PostMapping("/upload")
    public TemplateDocument upload(
            Principal p,
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String cat
    ) throws Exception {
        return service.uploadTemplate(me(p), file, cat);
    }

    @PostMapping("/{id}/admin-sign")
    public TemplateDocument adminSign(Principal p, @PathVariable Long id) throws Exception {
        return service.signAsAdmin(id, me(p));
    }

    @PostMapping("/{id}/super-sign")
    public TemplateDocument superSign(Principal p, @PathVariable Long id) throws Exception {
        return service.signAsSuper(id, me(p));
    }

    @GetMapping("/inbox-admin")
    public List<TemplateDocument> inboxAdmin() {
        return service.getInboxForAdmin();
    }

    @GetMapping("/inbox-super")
    public List<TemplateDocument> inboxSuper() {
        return service.getInboxForSuper();
    }

    @GetMapping("/my")
    public List<TemplateDocument> myDocs(Principal p) {
        return service.getUserDocs(me(p).getId());
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPERADMIN')")
    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) {

        TemplateDocument doc = templateDocRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Документ не найден"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + doc.getFilename() + "\"")
                .contentType(getMediaType(doc.getFilename()))
                .body(doc.getFileData());
    }

    @GetMapping("/{id}")
    public TemplateDocument getOne(@PathVariable Long id) {
        return templateDocRepo.findById(id).orElseThrow();
    }

    @PostMapping("/{id}/return")
    public TemplateDocument returnForFix(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        return service.returnForFix(id, body.get("comment"));
    }


    private MediaType getMediaType(String filename) {
        if (filename.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        if (filename.endsWith(".docx")) {
            return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

}
