package dto;

import domain.Match;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Getter
public class TicketResponse {
    private final List<Match> matches;
    private final double betAmount;
    private final String formattedBetAmount;
    private final String ticketCode;
    private final String location;
    private final String timestamp;
    private final String tg;
    private final String date;
    private final String ib;
    private final String ns;
    private final String quotaType;
    private final String betType;
    private final String betChosen;
    private final double odds;
    private final double bonusAmount;
    private final double totalWinnings;
    private final String formattedTotalWinnings;
    private final String formattedOdds;
    private final String barcodeUrl;
    private final Map<String, String> imageUrls;
    private final String pdfDownloadUrl;
    private final boolean hasBonus;
    private final int matchCount;

    public TicketResponse(List<Match> matches, double betAmount, String ticketCode,
                          String location, String timestamp, String tg, String date,
                          String ib, String ns, String quotaType, String betType,
                          String betChosen, double odds, double bonusAmount,
                          double totalWinnings, String barcodeUrl,
                          Map<String, String> imageUrls) {
        this.matches = matches;
        this.betAmount = round(betAmount, 2);
        this.formattedBetAmount = formatCurrency(this.betAmount);
        this.ticketCode = ticketCode;
        this.location = location;
        this.timestamp = timestamp;
        this.tg = tg;
        this.date = date;
        this.ib = ib;
        this.ns = ns;
        this.quotaType = quotaType;
        this.betType = betType;
        this.betChosen = betChosen;
        this.odds = round(odds, 2);
        this.bonusAmount = round(bonusAmount, 2);
        this.totalWinnings = round(totalWinnings, 2);
        this.formattedTotalWinnings = formatCurrency(this.totalWinnings);
        this.formattedOdds = formatDecimal(this.odds);
        this.barcodeUrl = barcodeUrl;
        this.imageUrls = imageUrls;
        this.pdfDownloadUrl = "/api/tickets/print?code=" + ticketCode;
        this.hasBonus = bonusAmount > 0;
        this.matchCount = matches != null ? matches.size() : 0;
    }

    private static double round(double value, int places) {
        return BigDecimal.valueOf(value)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static String formatDecimal(double value) {
        return String.format("%.2f", value);
    }

    private static String formatCurrency(double value) {
        return String.format("€%,.2f", value);
    }
}