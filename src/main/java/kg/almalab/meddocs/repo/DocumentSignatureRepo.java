package kg.almalab.meddocs.repo;


import kg.almalab.meddocs.model.DocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentSignatureRepo extends JpaRepository<DocumentSignature, Long> {
}
