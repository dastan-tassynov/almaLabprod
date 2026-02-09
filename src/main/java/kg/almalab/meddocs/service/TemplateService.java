package kg.almalab.meddocs.service;
import kg.almalab.meddocs.model.*;
import kg.almalab.meddocs.repo.DocumentSignatureRepo;
import kg.almalab.meddocs.repo.TemplateDocRepo;
import kg.almalab.meddocs.util.QrWatermarkUtil;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtrRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TemplateService {
    @Value("${app.templates.storage}")
    private String storage;

    private final TemplateDocRepo repo;

    private final DocumentSignatureRepo signatureRepo;

    public TemplateService(TemplateDocRepo r, DocumentSignatureRepo signatureRepo) {
        this.repo = r;
        this.signatureRepo = signatureRepo;
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

    private TemplateDocument sign(Long id, User signer, String role, TemplateStatus nextStatus) throws Exception {
        TemplateDocument doc = repo.findById(id).orElseThrow();

        // 1. Сохраняем подпись (решаем проблему TransientObjectException)
        DocumentSignature s = new DocumentSignature();
        s.setSignerFullName(signer.getFullName());
        s.setSignerRole(role);
        s.setSignedAt(LocalDateTime.now());
        s.setDocument(doc);
        signatureRepo.save(s);

        doc.getSignatures().add(s);
        doc.setStatus(nextStatus);

        File file = new File(storage, doc.getFilename());

        if (doc.getFilename().endsWith(".docx")) {
            // Читаем файл полностью в память
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            try (XWPFDocument xdoc = new XWPFDocument(new ByteArrayInputStream(fileBytes))) {

                // 2. Добавляем вотермарк
                QrWatermarkUtil.addDiagonalWatermark(xdoc);
                XWPFHeader headerWithWatermark = xdoc.getHeaderList().get(0);
                String relationId = xdoc.getRelationId(headerWithWatermark);

// 2. Проходим по ВСЕМ секциям документа (это решает проблему невидимости)
// В сложных документах настройки хранятся в конце Body или в разрывах
                for (XWPFParagraph p : xdoc.getParagraphs()) {
                    if (p.getCTP().getPPr() != null && p.getCTP().getPPr().getSectPr() != null) {
                        bindHeader(p.getCTP().getPPr().getSectPr(), relationId);
                    }
                }
// И для основной секции документа
                bindHeader(xdoc.getDocument().getBody().addNewSectPr(), relationId);

                // 3. Добавляем QR (если это финальный этап)
                if (nextStatus == TemplateStatus.COMPLETED) {
                    XWPFParagraph p = xdoc.createParagraph();
                    p.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun r = p.createRun();
                    r.addBreak();
                    r.setText("Документ подтвержден AlmaLab");
                    r.addBreak();

                    // Формируем данные для сканера: ФИО и Дата
                    StringBuilder qrData = new StringBuilder();
                    qrData.append("Организация: AlmaLab\n");
                    Set<String> signedUsers = new LinkedHashSet<>();

                    for (DocumentSignature sig : doc.getSignatures()) {
                        String name = sig.getSignerFullName();
                        String date = sig.getSignedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                        // Если этого пользователя еще не добавляли в текст QR
                        if (!signedUsers.contains(name)) {
                            qrData.append("\nПодпись: ").append(name)
                                    .append("\nДата: ").append(date).append("\n");
                            signedUsers.add(name);
                        }
                    }

                    byte[] qrImage = QrWatermarkUtil.generateQRBytes(qrData.toString());

                    // Оптимальный размер (примерно 5 см)
                    int width = (int)(2.0 * 952500);
                    int height = (int)(2.0 * 952500);

                    r.addPicture(new ByteArrayInputStream(qrImage),
                            XWPFDocument.PICTURE_TYPE_PNG, "qr.png", width, height);
                }

                // 4. СОХРАНЕНИЕ: Сначала в массив, потом в файл
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    xdoc.write(baos);
                    byte[] resultBytes = baos.toByteArray();

                    // Обновляем массив в БД
                    doc.setFileData(resultBytes);

                    // Перезаписываем файл на диске
                    Files.write(file.toPath(), resultBytes);
                }
            }
        }

        return repo.save(doc);
    }

    private void bindHeader(CTSectPr sectPr, String relId) {
        // Очищаем старые, чтобы не было конфликтов
        if (sectPr.sizeOfHeaderReferenceArray() > 0) {
            for (int i = sectPr.sizeOfHeaderReferenceArray() - 1; i >= 0; i--) {
                sectPr.removeHeaderReference(i);
            }
        }
        // Привязываем наш вотермарк
        CTHdrFtrRef ref = sectPr.addNewHeaderReference();
        ref.setType(STHdrFtr.DEFAULT);
        ref.setId(relId);
    }


    @Transactional
    public List<TemplateDocument> getInboxForAdmin() {
        // Админ видит всё, что не является черновиком (т.е. отправлено админу, суперу или завершено)
        return repo.findAll().stream()
                .filter(d -> d.getStatus() != TemplateStatus.NEW && d.getStatus() != TemplateStatus.RETURNED_FOR_FIX)
                .toList();
    }

    @Transactional
    public List<TemplateDocument> getInboxForSuper() {
        // Супер-админ видит документы на этапе финала или уже готовые
        return repo.findAll().stream()
                .filter(d -> d.getStatus() == TemplateStatus.SENT_TO_SUPERADMIN ||
                        d.getStatus() == TemplateStatus.COMPLETED)
                .toList();
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
