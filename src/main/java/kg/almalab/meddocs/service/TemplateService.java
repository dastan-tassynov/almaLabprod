package kg.almalab.meddocs.service;
import kg.almalab.meddocs.model.*;
import kg.almalab.meddocs.repo.TemplateDocRepo;
import kg.almalab.meddocs.util.PdfWatermarkUtil;
import kg.almalab.meddocs.util.QrWatermarkUtil;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TemplateService {
    @Value("${app.templates.storage}")
    private String storage;

    private final TemplateDocRepo repo;

    public TemplateService(TemplateDocRepo r) {
        this.repo = r;
    }

    public TemplateDocument uploadTemplate(User user,
                                           MultipartFile file,
                                           String category) throws Exception {

        File folder = new File(storage);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File target = new File(folder, filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        TemplateDocument doc = new TemplateDocument();
        doc.setUser(user);
        doc.setCategory(category);
        doc.setFilename(filename);
        doc.setFileData(file.getBytes());
        doc.setStatus(TemplateStatus.SENT_TO_ADMIN);

        return repo.save(doc);
    }


//    public TemplateDocument uploadTemplate(
//            User user,
//            MultipartFile file,
//            String category
//    ) throws Exception {
//
//        // 1. Гарантируем, что папка существует
//        Path storagePath = Paths.get(storage);
//        Files.createDirectories(storagePath);
//
//        // 2. Безопасное имя файла
//        String originalName = StringUtils.cleanPath(
//                Objects.requireNonNull(file.getOriginalFilename())
//        );
//
//        String filename = UUID.randomUUID() + "_" + originalName;
//
//        // 3. Финальный путь
//        Path targetPath = storagePath.resolve(filename);
//
//        // 4. Сохраняем файл
//        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
//
//        // 5. Сохраняем в БД
//        TemplateDocument doc = new TemplateDocument();
//        doc.setUser(user);
//        doc.setCategory(category);
//        doc.setFilename(filename);
//        doc.setStatus(TemplateStatus.SENT_TO_ADMIN);
//
//        return repo.save(doc);
//    }



    @Transactional
    public TemplateDocument signAsAdmin(Long id, User admin) throws Exception {
        return sign(id, admin, "ADMIN", TemplateStatus.SENT_TO_SUPERADMIN);
    }

    @Transactional
    public TemplateDocument signAsSuper(Long id, User superAdmin) throws Exception {
        return sign(id, superAdmin, "SUPERADMIN", TemplateStatus.COMPLETED);
    }

    private TemplateDocument sign(
            Long id,
            User signer,
            String role,
            TemplateStatus nextStatus
    ) throws Exception {

        TemplateDocument doc = repo.findById(id).orElseThrow();
        File file = new File(storage, doc.getFilename());

        String text = signer.getFullName() + " | " + role;

        if (doc.getFilename().endsWith(".pdf")) {

            byte[] qr = QrWatermarkUtil.generateQR(text);

            PdfWatermarkUtil.addWatermarkAndSignature(
                    file, qr, signer.getFullName(), role
            );

        } else if (doc.getFilename().endsWith(".docx")) {

            try (FileInputStream fis = new FileInputStream(file)) {
                XWPFDocument xdoc = new XWPFDocument(fis);

                QrWatermarkUtil.addWatermark(xdoc);

                byte[] qr = QrWatermarkUtil.generateQR(text);

                XWPFParagraph p = xdoc.createParagraph();
                XWPFRun r = p.createRun();
                r.setText("Signed by " + role + ": " + signer.getFullName());
                r.addBreak();
                r.addPicture(
                        new ByteArrayInputStream(qr),
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "qr.png",
                        120 * 9525,
                        120 * 9525
                );

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    xdoc.write(fos);
                }
            }
        }

        // история подписей
        DocumentSignature s = new DocumentSignature();
        s.setSignerFullName(signer.getFullName());
        s.setSignerRole(role);
        s.setSignedAt(LocalDateTime.now());
        s.setDocument(doc);

        doc.getSignatures().add(s);
        doc.setStatus(nextStatus);

        return repo.save(doc);
    }


    @Transactional
    public List<TemplateDocument> getInboxForAdmin() {
        return repo.findByStatus(TemplateStatus.SENT_TO_ADMIN);
    }
    @Transactional
    public List<TemplateDocument> getInboxForSuper() {
        return repo.findByStatus(TemplateStatus.SENT_TO_SUPERADMIN);
    }
    @Transactional
    public List<TemplateDocument> getUserDocs(Long userId) {
        return repo.findByUserId(userId);
    }

    @Transactional
    public TemplateDocument returnForFix(Long id, String comment) {
        TemplateDocument doc = repo.findById(id).orElseThrow();

        doc.setStatus(TemplateStatus.RETURNED_FOR_FIX);
        doc.setComment(comment);

        return repo.save(doc);
    }

   }
