package kg.almalab.meddocs.repo;


import kg.almalab.meddocs.model.DocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentSignatureRepo extends JpaRepository<DocumentSignature, Long> {

    List<DocumentSignature> findAllBySignedAtBefore(LocalDateTime date);
}
