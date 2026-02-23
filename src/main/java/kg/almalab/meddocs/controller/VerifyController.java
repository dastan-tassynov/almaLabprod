package kg.almalab.meddocs.controller;

import kg.almalab.meddocs.model.DocumentSignature;
import kg.almalab.meddocs.repo.DocumentSignatureRepo;
import kg.almalab.meddocs.repo.TemplateDocRepo;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*") // Разрешаем доступ со всех доменов (для публичной проверки)
@RequestMapping("/api/public/verify")
public class VerifyController {

    private final DocumentSignatureRepo repo;

    private final TemplateDocRepo templateRepository;

    public VerifyController(DocumentSignatureRepo repo, TemplateDocRepo templateRepository) {
        this.repo = repo;
        this.templateRepository = templateRepository;
    }

    @GetMapping("/{id}")
    public Map<String, Object> verifyDocument(@PathVariable Long id) {
        return templateRepository.findById(id).map(doc -> {
            Map<String, Object> res = new HashMap<>();
            res.put("exists", true);
            res.put("filename", doc.getFilename());
            res.put("category", doc.getCategory());
            res.put("createdAt", doc.getCreatedAt());

            // Информация о том, кто загрузил (наша "авто-подпись")
            res.put("author", doc.getUser().getFullName());

            // Список всех, кто подтвердил (Админ, Директор)
            res.put("signatures", doc.getSignatures());

            res.put("status", doc.getStatus());
            return res;
        }).orElseGet(() -> {
            Map<String, Object> error = new HashMap<>();
            error.put("exists", false);
            return error;
        });
    }
}
