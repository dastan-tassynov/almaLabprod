package kg.almalab.meddocs.util;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.io.File;
import java.io.IOException;
public class PdfWatermarkUtil {
    public static void addWatermarkAndSignature(
            File file,
            byte[] qr,
            String signer,
            String role
    ) throws Exception {

        PDDocument doc = PDDocument.load(file);

        for (PDPage page : doc.getPages()) {
            PDRectangle box = page.getMediaBox();

            try (PDPageContentStream cs =
                         new PDPageContentStream(doc, page,
                                 PDPageContentStream.AppendMode.APPEND, true)) {

                // 🔹 WATERMARK
                cs.setFont(PDType1Font.HELVETICA_BOLD, 80);
                cs.setNonStrokingColor(220, 220, 220);
                cs.beginText();
                cs.setTextMatrix(1, 0, 0, 1,
                        box.getWidth() / 4,
                        box.getHeight() / 2);
                cs.showText("AlmaLab");
                cs.endText();

                // 🔹 QR
                PDImageXObject qrImg =
                        PDImageXObject.createFromByteArray(doc, qr, "qr");

                cs.drawImage(qrImg, 40, 40, 120, 120);

                // 🔹 SIGN TEXT
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.setNonStrokingColor(0, 0, 0);
                cs.newLineAtOffset(40, 170);
                cs.showText("Signed by " + role + ": " + signer);
                cs.endText();
            }
        }

        doc.save(file);
        doc.close();
    }
}

