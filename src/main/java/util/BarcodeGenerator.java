package util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class BarcodeGenerator {

    /**
     * Generates a barcode as a Base64-encoded PNG string
     * (Better for web than filesystem writes)
     */
    public static String generateBase64Barcode(String barcodeText) throws WriterException, IOException {
        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.CODE_128, 200, 100);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * Generates a barcode as byte[] (for direct HTTP response)
     */
    public static byte[] generateBarcodeBytes(String barcodeText, int width, int height)
            throws WriterException, IOException {
        Code128Writer barcodeWriter = new Code128Writer();

        // Generate the barcode with specified dimensions
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.CODE_128, width, height);

        // Convert to PNG bytes
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        }
    }

    // Overloaded method with default dimensions (matches JavaFX default)
    public static byte[] generateBarcodeBytes(String barcodeText) throws WriterException, IOException {
        return generateBarcodeBytes(barcodeText, 260, 60); // Default to JavaFX dimensions
    }
}
