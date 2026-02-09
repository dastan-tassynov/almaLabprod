package kg.almalab.meddocs.util;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.io.File;
import java.io.IOException;
public class PdfWatermarkUtil {
    public static void addWatermarkAndSignature(File file, byte[] qr, String signer, String role) throws Exception {
        PDDocument doc = PDDocument.load(file);
        for (PDPage page : doc.getPages()) {
            PDRectangle box = page.getMediaBox();
            try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                // 🔹 1. ПРОЗРАЧНЫЙ ВЕРТИКАЛЬНЫЙ WATERMARK
                PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                gs.setNonStrokingAlphaConstant(0.15f); // 15% прозрачности
                cs.setGraphicsStateParameters(gs);

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 110);
                cs.setNonStrokingColor(200, 200, 200);
                // Матрица поворота на 90 градусов (вертикально по центру справа)
                cs.setTextMatrix(0, 1, -1, 0, box.getWidth() - 60, box.getHeight() / 3);
                cs.showText("AlmaLab");
                cs.endText();

                // 🔹 2. QR И ПОДПИСЬ (сбрасываем прозрачность на 100%)
                gs.setNonStrokingAlphaConstant(1.0f);
                cs.setGraphicsStateParameters(gs);

                PDImageXObject qrImg = PDImageXObject.createFromByteArray(doc, qr, "qr");
                // Отрисовка QR в нижнем правом углу
                cs.drawImage(qrImg, box.getWidth() - 140, 50, 100, 100);

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                cs.newLineAtOffset(box.getWidth() - 140, 40);
                cs.showText("Signed by " + role + ": " + signer);
                cs.endText();
            }
        }
        doc.save(file);
        doc.close();
    }
}

