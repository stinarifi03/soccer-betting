package dto;

import domain.Match;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class MatchResponse {
    private final Match matchDetails;
    private final int matchCount;
    private final double totalOdds;
    private final double potentialWinnings;
    private final String formattedOdds;
    private final String formattedWinnings;
    private final boolean bonusEligible;
    private final boolean multiBet;

    public MatchResponse(Match match, int matchCount, double totalOdds, double potentialWinnings) {
        this.matchDetails = match;
        this.matchCount = matchCount;
        this.totalOdds = round(totalOdds, 2);
        this.potentialWinnings = round(potentialWinnings, 2);
        this.formattedOdds = formatDecimal(this.totalOdds);
        this.formattedWinnings = formatCurrency(this.potentialWinnings);
        this.bonusEligible = matchCount >= 5;
        this.multiBet = matchCount > 1;
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