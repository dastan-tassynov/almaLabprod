package kg.almalab.meddocs.util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.poi.xwpf.usermodel.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class QrWatermarkUtil {
    public static byte[] generateQR(String text) throws IOException {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            var matrix = writer.encode(text, BarcodeFormat.QR_CODE, 300, 300);

            BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 300; x++)
                for (int y = 0; y < 300; y++)
                    img.setRGB(x, y,
                            matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();

        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addWatermark(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText("AlmaLab");
        r.setColor("D3D3D3");
        r.setFontSize(80);
        r.setBold(true);
    }

    public static BufferedImage generate(String text) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}
