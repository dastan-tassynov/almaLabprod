package kg.almalab.meddocs.controller;

import kg.almalab.meddocs.model.DocumentSignature;
import kg.almalab.meddocs.repo.DocumentSignatureRepo;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/verify")
public class VerifyController {

    private final DocumentSignatureRepo repo;

    public VerifyController(DocumentSignatureRepo repo) {
        this.repo = repo;
    }

    @GetMapping
    public Map<String, Object> verify(@RequestParam String signer) {

        List<DocumentSignature> list =
                repo.findAll().stream()
                        .filter(s -> s.getSignerFullName().equals(signer))
                        .toList();

        Map<String, Object> res = new HashMap<>();
        res.put("valid", !list.isEmpty());
        res.put("signatures", list);

        return res;
    }
}
