package kg.almalab.meddocs.repo;

import kg.almalab.meddocs.model.TemplateDocument;
import kg.almalab.meddocs.model.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface TemplateDocRepo extends JpaRepository<TemplateDocument, Long> {
    List<TemplateDocument> findByUserId(Long userId);

    List<TemplateDocument> findByStatus(TemplateStatus status);
}
