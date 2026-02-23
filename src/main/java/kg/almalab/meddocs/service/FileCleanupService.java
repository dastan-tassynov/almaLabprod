package kg.almalab.meddocs.service;

import kg.almalab.meddocs.model.DocumentSignature;
import kg.almalab.meddocs.model.TemplateDocument;
import kg.almalab.meddocs.repo.DocumentSignatureRepo;
import kg.almalab.meddocs.repo.TemplateDocRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FileCleanupService {

    @Value("${app.templates.storage}")
    private String storagePath;
    private final DocumentSignatureRepo signatureRepo;
    private final TemplateDocRepo templateRepo;

    public FileCleanupService(DocumentSignatureRepo signatureRepo, TemplateDocRepo templateRepo) {
        this.signatureRepo = signatureRepo;
        this.templateRepo = templateRepo;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Каждый день в полночь
    public void cleanupOldDocuments() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);

        // 1. Ищем сигнатуры, созданные более месяца назад
        List<DocumentSignature> oldSignatures = signatureRepo.findAllBySignedAtBefore(monthAgo);

        for (DocumentSignature sig : oldSignatures) {
            // 2. Получаем объект документа, связанный с подписью
            TemplateDocument doc = sig.getDocument();

            if (doc != null) {
                // 3. Удаляем физический файл с диска
                // Склеиваем путь к папке из конфига и имя файла из БД
                File file = new File(storagePath, doc.getFilename());
                if (file.exists()) {
                    file.delete();
                }

                // 4. Сначала удаляем из дочерней таблицы (подписи)
                signatureRepo.delete(sig);

                // 5. Затем удаляем из родительской таблицы (документы)
                templateRepo.delete(doc);
            } else {
                // Если вдруг документа нет, просто удаляем сиротскую подпись
                signatureRepo.delete(sig);
            }
        }
    }
}
