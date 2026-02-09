package kg.almalab.meddocs.util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class QrWatermarkUtil {

    public static byte[] generateQRBytes(String data) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        // Ключевой момент: указываем UTF-8
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1); // Минимальная белая рамка
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 500, 500, hints);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    /**
     * Добавляет диагональный водяной знак "AlmaLab" НА ЗАДНИЙ ПЛАН.
     * Используется VML разметка, чтобы текст не двигал основное содержимое.
     */
//    public static void addDiagonalWatermark(XWPFDocument doc) {
//        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
//        XWPFParagraph paragraph = header.createParagraph();
//        XWPFRun run = paragraph.createRun();
//
//        // XML разметка для вставки текста "за текстом" под углом
//        String watermarkXml =
//                "<xml-fragment xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">" +
//                        "<v:shapetype id=\"_x0000_t136\" coordsize=\"21600,21600\" o:spt=\"136\" adj=\"10800\" path=\"m@7,l@8,m@5,21600l@6,21600e\">" +
//                        "<v:path textpathok=\"t\" o:connecttype=\"rect\"/>" +
//                        "</v:shapetype>" +
//                        "<v:shape id=\"Watermark\" type=\"#_x0000_t136\" style=\"position:absolute;margin-left:0;margin-top:0;" +
//                        "width:400pt;height:100pt;z-index:-251658240;rotation:315;mso-position-horizontal:center;mso-position-horizontal-relative:margin;" +
//                        "mso-position-vertical:center;mso-position-vertical-relative:margin\" fillcolor=\"#FF0000\" stroked=\"f\">" +
//                        "<v:fill opacity=\"1.0\"/>" +
//                        "<v:textpath style=\"font-family:&quot;Arial&quot;;font-size:80pt\" string=\"AlmaLab\"/>" +
//                        "</v:shape>" +
//                        "</xml-fragment>";
//
//        try {
//            run.getCTR().addNewPict().set(org.apache.xmlbeans.XmlObject.Factory.parse(watermarkXml));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void addDiagonalWatermark(XWPFDocument doc) {
        String text = "AlmaLab";
        XWPFHeader header = doc.getHeaderList().isEmpty() ? doc.createHeader(HeaderFooterType.DEFAULT) : doc.getHeaderList().get(0);
        XWPFParagraph paragraph = header.getParagraphs().isEmpty() ? header.createParagraph() : header.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();

        String color = "#00A0B0";
        String opacity = "0.12";  // Сделал чуть светлее для элегантности
        int fontSize = 44;        // Размер как заказывали

        String[] positions = {"top:120pt", "top:380pt", "top:640pt"};

        for (int i = 0; i < positions.length; i++) {
            // font-weight:normal делает шрифт "худым"
            // rotated="315" - четкий наклон
            String vmlXml =
                    "<v:shape xmlns:v=\"urn:schemas-microsoft-com:vml\" id=\"WM" + i + "\" type=\"#_x0000_t136\" " +
                            "style=\"position:absolute;width:500pt;height:100pt;v-text-anchor:middle;" +
                            "mso-position-horizontal:center;mso-position-horizontal-relative:page;" +
                            positions[i] + ";mso-position-vertical-relative:page;z-index:-251658240\" " +
                            "fillcolor=\"" + color + "\" stroked=\"f\" rotated=\"315\">" +
                            "<v:fill opacity=\"" + opacity + "\"/>" +
                            "<v:textpath style=\"font-family:&quot;Times New Roman&quot;;font-size:" + fontSize + "pt;font-weight:normal\" string=\"" + text + "\"/>" +
                            "</v:shape>";

            try {
                org.apache.xmlbeans.XmlObject xmlObject = org.apache.xmlbeans.XmlObject.Factory.parse(vmlXml);
                run.getCTR().addNewPict().set(xmlObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
