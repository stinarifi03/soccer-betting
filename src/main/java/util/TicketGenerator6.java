package util;

import com.google.zxing.WriterException;
import domain.Bet;
import domain.Match;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketGenerator6 {
    private static final PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font font4 = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font font2 = new PDType1Font(Standard14Fonts.FontName.COURIER);
    private static final PDType1Font font3 = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);
    private static final int TICKET_WIDTH = 300;
    private static final String CURRENCY = "€";
    private static final Font DETAILS_FONT = new Font("Courier New", Font.PLAIN, 10);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 42);
    private static final Font LEAGUE_FONT = new Font("Arial", Font.BOLD, 10);

    private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();

    public static BufferedImage generateTicket(Bet bet, String ticketCode, String location,
                                               String timestamp, String tg, String date,
                                               String ib, String ns, String quotaType,
                                               String betType, String betChosen,
                                               double odds, double bonusAmount,
                                               double totalWinnings,
                                               BufferedImage barcodeImage) throws IOException {

        BufferedImage ticketImage = new BufferedImage(TICKET_WIDTH, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = ticketImage.createGraphics();

        // White background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, TICKET_WIDTH, 800);
        g.setColor(Color.BLACK);

        int yPos = 10; // Starting Y position

        // 1. Header Section
        yPos = drawHeader(g, yPos);

        // 2. Ticket Details
        yPos = drawDetails(g, yPos, ticketCode, location, tg, timestamp, date, ib, ns);

        // 3. Bet Information
        yPos = drawBetInfo(g, yPos, bet, quotaType, betType, betChosen, odds);

        // 4. Amount Section
        yPos = drawAmountSection(g, yPos, bet.getBetAmount(), bonusAmount, totalWinnings);

        // 5. Footer
        yPos = drawFooter(g, yPos);

        // 6. Barcode
        drawBarcode(g, yPos, barcodeImage);

        g.dispose();
        return ticketImage;
    }

    private static int drawHeader(Graphics2D g, int yPos) throws IOException {
        // Logo
        BufferedImage logo = loadImageResource("/images/goldbet_logo.png");
        g.drawImage(logo, 5, yPos, 50, (int)(50 * ((double)logo.getHeight()/logo.getWidth())), null);

        // Title
        g.setFont(HEADER_FONT);
        g.drawString("GoldBet", 60, yPos + 40);

        // QR Code
        BufferedImage qrCode = loadImageResource("/images/qr_code.png");
        g.setFont(new Font("Arial", Font.BOLD, 6));
        g.drawString("BONUS BENVENUTO ONLI", TICKET_WIDTH - 75, yPos + 10);
        g.drawImage(qrCode, TICKET_WIDTH - 75, yPos + 15, 70, 70, null);

        return yPos + 85;
    }

    private static int drawDetails(Graphics2D g, int yPos, String ticketCode, String location,
                                   String tg, String timestamp, String date, String ib, String ns) {
        g.setFont(DETAILS_FONT);

        // Top line
        g.drawLine(5, yPos, TICKET_WIDTH - 5, yPos);
        yPos += 10;

        // Details text
        g.drawString("CB-" + ticketCode, 10, yPos);
        yPos += 12;
        g.drawString("CC-4098   NC-GBO Italy S.p.A   PV-14274   TM-4", 10, yPos);
        yPos += 12;
        g.drawString("NP-" + location + "   TG-" + tg, 10, yPos);
        yPos += 12;
        g.drawString("GE-" + date + "      OE-" + timestamp + "      CG-2005376", 10, yPos);
        yPos += 12;
        g.drawString("IB-" + ib + String.format("%" + (18 - ib.length()) + "s", "") + "NS-" + ns, 10, yPos);
        yPos += 15;

        // Bottom line
        g.drawLine(5, yPos, TICKET_WIDTH - 5, yPos);
        return yPos + 5;
    }

    private static int drawBetInfo(Graphics2D g, int yPos, Bet bet, String quotaType,
                                   String betType, String betChosen, double odds) {
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString(quotaType.toUpperCase(), (TICKET_WIDTH - g.getFontMetrics().stringWidth(quotaType.toUpperCase()))/2, yPos);
        yPos += 20;

        g.setFont(DETAILS_FONT);

        Map<String, List<Match>> matchesByLeague = groupMatchesByLeague(bet.getMatches());

        for (Map.Entry<String, List<Match>> entry : matchesByLeague.entrySet()) {
            // League header
            yPos = drawLeagueHeader(g, yPos, entry.getKey());

            // Matches
            for (Match match : entry.getValue()) {
                yPos = drawMatch(g, yPos, match);
            }
        }

        return yPos;
    }

    private static int drawLeagueHeader(Graphics2D g, int yPos, String leagueName) {
        // Top line
        g.drawLine(20, yPos, TICKET_WIDTH - 20, yPos);
        yPos += 5;

        // League name
        g.setFont(LEAGUE_FONT);
        int textWidth = g.getFontMetrics().stringWidth(leagueName);
        g.drawString(leagueName, (TICKET_WIDTH - textWidth)/2, yPos);
        yPos += 15;

        // Bottom line
        g.drawLine(20, yPos, TICKET_WIDTH - 20, yPos);
        yPos += 10;

        return yPos;
    }

    private static int drawMatch(Graphics2D g, int yPos, Match match) {
        g.setFont(DETAILS_FONT);

        // Match info line 1
        String prefix = String.format("(%s) %s %s%s %s %s",
                match.getMatchId(),
                match.getCode(),
                match.getSport(),
                String.format("%" + (15 - match.getSport().length()) + "s", ""),
                match.getDate(),
                match.getTime());

        g.drawString(prefix, 10, yPos);
        yPos += 12;

        // Teams
        g.setFont(new Font("Courier New", Font.BOLD, 10));
        g.drawString(match.getTeam1() + " - " + match.getTeam2(), 10, yPos);
        yPos += 12;

        // Bet info
        g.setFont(DETAILS_FONT);
        String betLine = String.format("%s%s%s%s%s",
                match.getBetType(),
                String.format("%" + (35 - match.getBetType().length()) + "s", ""),
                match.getBetChosen(),
                String.format("%" + 2 + "s", ""),
                formatNumber(match.getOdds()));

        g.drawString(betLine, 10, yPos);
        yPos += 20;

        return yPos;
    }

    private static int drawAmountSection(Graphics2D g, int yPos, double betAmount,
                                         double bonusAmount, double totalWinnings) {
        g.setFont(new Font("Arial", Font.BOLD, 12));

        // Bet amount
        String betAmountStr = "IMPORTO SCOMMESSO";
        g.drawString(betAmountStr, 10, yPos);
        g.drawString(formatNumber(betAmount) + CURRENCY,
                TICKET_WIDTH - 10 - g.getFontMetrics().stringWidth(formatNumber(betAmount) + CURRENCY),
                yPos);
        yPos += 15;

        // Bonus if applicable
        if (bonusAmount > 0) {
            g.drawString("BONUS", 10, yPos);
            g.drawString(formatNumber(bonusAmount) + CURRENCY,
                    TICKET_WIDTH - 10 - g.getFontMetrics().stringWidth(formatNumber(bonusAmount) + CURRENCY),
                    yPos);
            yPos += 15;
        }

        // Total winnings box
        yPos = drawWinningsBox(g, yPos, totalWinnings);

        // Disclaimer
        g.setFont(new Font("Arial", Font.BOLD, 8));
        String disclaimer = "Consulta le probabilità di vincita e i regolamenti di gioco su goldbet.it.\n" +
                "Il gioco è vietato ai minori di 18 anni e può causare dipendenza patologica.";
        for (String line : disclaimer.split("\n")) {
            int textWidth = g.getFontMetrics().stringWidth(line);
            g.drawString(line, (TICKET_WIDTH - textWidth)/2, yPos);
            yPos += 10;
        }

        return yPos + 10;
    }

    private static int drawWinningsBox(Graphics2D g, int yPos, double totalWinnings) {
        // Box dimensions
        int boxWidth = TICKET_WIDTH - 20;
        int boxHeight = 36;

        // Draw box
        g.drawRect(10, yPos, boxWidth, boxHeight);

        // Left pattern
        for (int x = 12; x < 89; x += 8) {
            g.fillRect(x, yPos + 1, 3, boxHeight - 2);
        }

        // Right pattern
        for (int x = TICKET_WIDTH - 89; x < TICKET_WIDTH - 12; x += 8) {
            g.fillRect(x, yPos + 1, 3, boxHeight - 2);
        }

        // Text
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String totalText = "VINCITA TOTALE";
        int totalTextWidth = g.getFontMetrics().stringWidth(totalText);
        g.drawString(totalText, (TICKET_WIDTH - totalTextWidth)/2, yPos + 15);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        String amountText = formatNumber(totalWinnings) + CURRENCY;
        int amountTextWidth = g.getFontMetrics().stringWidth(amountText);
        g.drawString(amountText, (TICKET_WIDTH - amountTextWidth)/2, yPos + 30);

        return yPos + boxHeight + 10;
    }

    private static int drawFooter(Graphics2D g, int yPos) throws IOException {
        int xPos = (TICKET_WIDTH - (40*4 + 5*3)) / 2; // Center 4 images with 5px spacing

        BufferedImage gioco = loadImageResource("/images/gioco.jpeg");
        g.drawImage(gioco, xPos, yPos, 40, (int)(40 * ((double)gioco.getHeight()/gioco.getWidth())), null);
        xPos += 45;

        BufferedImage plus18 = loadImageResource("/images/+18.jpg");
        g.drawImage(plus18, xPos, yPos, 40, (int)(40 * ((double)plus18.getHeight()/plus18.getWidth())), null);
        xPos += 45;

        BufferedImage amdTimone = loadImageResource("/images/amd_timone.png");
        g.drawImage(amdTimone, xPos, yPos, 40, (int)(40 * ((double)amdTimone.getHeight()/amdTimone.getWidth())), null);
        xPos += 45;

        BufferedImage adm = loadImageResource("/images/adm.jpg");
        g.drawImage(adm, xPos, yPos, 80, (int)(80 * ((double)adm.getHeight()/adm.getWidth())), null);

        return yPos + 50;
    }

    private static void drawBarcode(Graphics2D g, int yPos, BufferedImage barcodeImage) {
        if (barcodeImage != null) {
            g.drawImage(barcodeImage, (TICKET_WIDTH - 260)/2, yPos, 260, 60, null);
        }
    }

    public static byte[] generatePdfTicket(Bet bet, String ticketCode, String location,
                                           String timestamp, String tg, String date,
                                           String ib, String ns, String quotaType,
                                           String betType, String betChosen,
                                           double odds, double bonusAmount,
                                           double totalWinnings) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(TICKET_WIDTH, 800));
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Convert JavaFX colors to PDF colors
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.addRect(0, 0, TICKET_WIDTH, 800);
                contentStream.fill();
                contentStream.setNonStrokingColor(Color.BLACK);

                // 1. Draw header
                PDImageXObject logo = PDImageXObject.createFromFile(
                        "src/main/resources/static/images/goldbet_logo.png", document);
                contentStream.drawImage(logo, 5, 750, 50, 50 * logo.getHeight() / logo.getWidth());

                contentStream.setFont(font, 42);
                contentStream.beginText();
                contentStream.newLineAtOffset(60, 770);
                contentStream.showText("GoldBet");
                contentStream.endText();

                // Continuing from the header section in generatePdfTicket()

// 2. Draw ticket details section
                contentStream.setFont(font2, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(10, 730);
                contentStream.showText("CB-" + ticketCode);
                contentStream.newLineAtOffset(0, -12);
                contentStream.showText("CC-4098   NC-GBO Italy S.p.A   PV-14274   TM-4");
                contentStream.newLineAtOffset(0, -12);
                contentStream.showText("NP-" + location + "   TG-" + tg);
                contentStream.newLineAtOffset(0, -12);
                contentStream.showText("GE-" + date + "      OE-" + timestamp + "      CG-2005376");
                contentStream.newLineAtOffset(0, -12);
                contentStream.showText("IB-" + ib + String.format("%" + (18 - ib.length()) + "s", "") + "NS-" + ns);
                contentStream.endText();

// Draw lines around details
                contentStream.setLineWidth(1.5f);
                contentStream.moveTo(10, 740);
                contentStream.lineTo(TICKET_WIDTH - 10, 740);
                contentStream.moveTo(10, 690);
                contentStream.lineTo(TICKET_WIDTH - 10, 690);
                contentStream.stroke();

// 3. Draw bet information section
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                float fontSize = 15;
                String text = quotaType.toUpperCase();
                float textWidth = font.getStringWidth(text) / 1000 * fontSize;
                float xPosition = (TICKET_WIDTH - textWidth) / 2;

                contentStream.setFont(font, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition, 670);  // y=670 from bottom
                contentStream.showText(text);
                contentStream.endText();

// Group matches by league
                Map<String, List<Match>> matchesByLeague = groupMatchesByLeague(bet.getMatches());
                int yPos = 650;

                for (Map.Entry<String, List<Match>> entry : matchesByLeague.entrySet()) {
                    // 1. Define font and text
                    PDType1Font leagueFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                    float leagueFontSize = 10;
                    String leagueName = entry.getKey();

                    // 2. Calculate text width
                    float leagueNameWidth = leagueFont.getStringWidth(leagueName) / 1000 * leagueFontSize;

                    // 3. Draw league header line (top)
                    contentStream.setLineWidth(2f);
                    contentStream.moveTo(20, yPos);
                    contentStream.lineTo(TICKET_WIDTH - 20, yPos);
                    contentStream.stroke();

                    // 4. Draw centered league name
                    contentStream.setFont(leagueFont, leagueFontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(
                            (TICKET_WIDTH - leagueNameWidth) / 2,  // x position
                            yPos - 5  // y position (5 units below the line)
                    );
                    contentStream.showText(leagueName);
                    contentStream.endText();

                    contentStream.moveTo(20, yPos - 15);
                    contentStream.lineTo(TICKET_WIDTH - 20, yPos - 15);
                    contentStream.stroke();

                    yPos -= 20;

                    // Matches
                    for (Match match : entry.getValue()) {
                        contentStream.setFont(font2, 10);
                        contentStream.beginText();

                        // Match info line 1
                        String prefix = String.format("(%s) %s %s%s %s %s",
                                match.getMatchId(),
                                match.getCode(),
                                match.getSport(),
                                String.format("%" + (15 - match.getSport().length()) + "s", ""),
                                match.getDate(),
                                match.getTime());

                        contentStream.newLineAtOffset(10, yPos);
                        contentStream.showText(prefix);
                        yPos -= 12;

                        // Teams (bold)
                        contentStream.setFont(font3, 10);
                        contentStream.newLineAtOffset(0, yPos);
                        contentStream.showText(match.getTeam1() + " - " + match.getTeam2());
                        yPos -= 12;

                        // Bet info
                        contentStream.setFont(font2, 10);
                        String betLine = String.format("%s%s%s%s%s",
                                match.getBetType(),
                                String.format("%" + (35 - match.getBetType().length()) + "s", ""),
                                match.getBetChosen(),
                                String.format("%" + 2 + "s", ""),
                                formatNumber(match.getOdds()));

                        contentStream.newLineAtOffset(0, yPos);
                        contentStream.showText(betLine);
                        yPos -= 20;
                    }
                }

// 4. Draw amount section
                yPos -= 10; // Add some vertical spacing

// Define fonts and text
                PDType1Font labelFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font amountFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                float labelFontSize = 12;
                float amountFontSize = 12;

                String labelText = "IMPORTO SCOMMESSO";
                String amountText = formatNumber(bet.getBetAmount()) + CURRENCY;

// Calculate amount text width
                float amountWidth = amountFont.getStringWidth(amountText) / 1000 * amountFontSize;

// Draw label and amount
                contentStream.setFont(labelFont, labelFontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(10, yPos); // Left-aligned label
                contentStream.showText(labelText);

// Right-aligned amount
                contentStream.newLineAtOffset(TICKET_WIDTH - 20 - amountWidth - 10, 0);
                contentStream.showText(amountText);
                contentStream.endText();

                yPos -= 15; // Adjust position for next element



// Bonus if applicable
                if (bonusAmount > 0) {
                    // Define fonts and text
                    PDType1Font labelFontBonus = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                    PDType1Font amountFontBonus = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                    float fontSizeBonus = 12;

                    String labelTextBonus = "BONUS";
                    String amountTextBonus = formatNumber(bonusAmount) + CURRENCY;

                    // Calculate amount text width
                    float amountWidthBonus = amountFontBonus.getStringWidth(amountTextBonus) / 1000 * fontSizeBonus;

                    // Draw label and amount
                    contentStream.setFont(labelFontBonus, fontSizeBonus);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(10, yPos); // Left-aligned label
                    contentStream.showText(labelTextBonus);

                    // Right-aligned amount
                    contentStream.newLineAtOffset(TICKET_WIDTH - 20 - amountWidthBonus - 10, 0);
                    contentStream.showText(amountTextBonus);
                    contentStream.endText();

                    yPos -= 15; // Adjust position for next element
                }

// 5. Draw winnings box
                int boxY = yPos - 36;
                contentStream.setLineWidth(1);
                contentStream.addRect(10, boxY, TICKET_WIDTH - 20, 36);
                contentStream.stroke();

// Left pattern
                for (int x = 12; x < 89; x += 8) {
                    contentStream.addRect(x, boxY + 1, 3, 34);
                    contentStream.fill();
                }

// Right pattern
                for (int x = TICKET_WIDTH - 89; x < TICKET_WIDTH - 12; x += 8) {
                    contentStream.addRect(x, boxY + 1, 3, 34);
                    contentStream.fill();
                }

// Winnings text
                // Define fonts and sizes
                PDType1Font totalLabelFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font totalAmountFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                float totalLabelFontSize = 12;
                float totalAmountFontSize = 16;

//  Draw "VINCITA TOTALE" label (centered)
                String totalLabel = "VINCITA TOTALE";
                float totalLabelWidth = totalLabelFont.getStringWidth(totalLabel) / 1000 * totalLabelFontSize;
                float totalLabelX = (TICKET_WIDTH - totalLabelWidth) / 2;

                contentStream.setFont(totalLabelFont, totalLabelFontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(totalLabelX, boxY + 15);
                contentStream.showText(totalLabel);
                contentStream.endText();

// Draw total winnings amount (centered)
                String totalAmount = formatNumber(totalWinnings) + CURRENCY;
                float totalAmountWidth = totalAmountFont.getStringWidth(totalAmount) / 1000 * totalAmountFontSize;
                float totalAmountX = (TICKET_WIDTH - totalAmountWidth) / 2;

                contentStream.setFont(totalAmountFont, totalAmountFontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(totalAmountX, boxY + 30);
                contentStream.showText(totalAmount);
                contentStream.endText();

// 6. Draw footer images
                int imageY = boxY - 50;
                int imageX = (TICKET_WIDTH - (40*4 + 5*3)) / 2;

                PDImageXObject gioco = PDImageXObject.createFromFile(
                        "src/main/resources/static/images/gioco.jpeg", document);
                contentStream.drawImage(gioco, imageX, imageY, 40, 40 * gioco.getHeight() / gioco.getWidth());

                PDImageXObject plus18 = PDImageXObject.createFromFile(
                        "src/main/resources/static/images/+18.jpg", document);
                contentStream.drawImage(plus18, imageX + 45, imageY, 40, 40 * plus18.getHeight() / plus18.getWidth());

                PDImageXObject amdTimone = PDImageXObject.createFromFile(
                        "src/main/resources/static/images/amd_timone.png", document);
                contentStream.drawImage(amdTimone, imageX + 90, imageY, 40, 40 * amdTimone.getHeight() / amdTimone.getWidth());

                PDImageXObject adm = PDImageXObject.createFromFile(
                        "src/main/resources/static/images/adm.jpg", document);
                contentStream.drawImage(adm, imageX + 135, imageY, 80, 80 * adm.getHeight() / adm.getWidth());

// 7. Draw disclaimer
                // Define disclaimer font
                PDType1Font disclaimerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD); // Or use PDType1Font.HELVETICA_BOLD for bold
                float disclaimerFontSize = 8;

// Disclaimer text
                String disclaimer = "Consulta le probabilità di vincita e i regolamenti di gioco su goldbet.it.\n" +
                        "Il gioco è vietato ai minori di 18 anni e può causare dipendenza patologica.";
                String[] lines = disclaimer.split("\n");

// Draw each line of disclaimer
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    // Calculate text width and center position
                    float lineWidth = disclaimerFont.getStringWidth(line) / 1000 * disclaimerFontSize;
                    float xPosition2 = (TICKET_WIDTH - lineWidth) / 2;
                    float yPosition = boxY - 70 - (i * 10); // 10 units between lines

                    // Draw the text
                    contentStream.setFont(disclaimerFont, disclaimerFontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition2, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                }

                // 6. Draw barcode
                byte[] barcodeBytes = BarcodeGenerator.generateBarcodeBytes(ticketCode);
                PDImageXObject barcode = PDImageXObject.createFromByteArray(
                        document, barcodeBytes, "barcode");
                contentStream.drawImage(barcode, 20, 30, 260, 60);
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    public static byte[] convertToPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private static BufferedImage loadImageResource(String path) throws IOException {
        if (IMAGE_CACHE.containsKey(path)) {
            return IMAGE_CACHE.get(path);
        }

        try (InputStream stream = TicketGenerator6.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Image not found: " + path);
            }
            BufferedImage image = ImageIO.read(stream);
            IMAGE_CACHE.put(path, image);
            return image;
        }
    }

    private static Map<String, List<Match>> groupMatchesByLeague(List<Match> matches) {
        Map<String, List<Match>> grouped = new HashMap<>();
        for (Match match : matches) {
            String league = match.getLeagueName() != null ? match.getLeagueName() : "NO LEAGUE";
            grouped.computeIfAbsent(league, k -> new ArrayList<>()).add(match);
        }
        return grouped;
    }

    private static String formatNumber(double number) {
        return String.format("%.2f", number).replace(".", ",");
    }
}