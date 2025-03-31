package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Bet {
    private List<Match> matches;
    private double betAmount;
    private double totalOdds;
    private double potentialWinnings;
    private String betId;
    private BetStatus status;

    public enum BetStatus {
        PENDING, WON, LOST, CASHED_OUT
    }

    public Bet() {
        this.matches = new ArrayList<>();
        this.status = BetStatus.PENDING;
    }

    public Bet(List<Match> matches, double betAmount) {
        this();
        this.matches = new ArrayList<>(matches);
        this.betAmount = betAmount;
        recalculate();
    }

    public void recalculate() {
        this.totalOdds = calculateTotalOdds();
        this.potentialWinnings = betAmount * totalOdds;
    }

    public double calculateTotalOdds() {
        return matches.stream()
                .mapToDouble(Match::getOdds)
                .reduce(1.0, (a, b) -> a * b);
    }

    // Getters and Setters
    public List<Match> getMatches() { return matches; }

    public void setMatches(List<Match> matches) {
        this.matches = new ArrayList<>(matches);
        recalculate();
    }

    public double getBetAmount() { return betAmount; }

    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
        recalculate();
    }

    public double getTotalOdds() { return totalOdds; }

    public void setTotalOdds(double totalOdds) {
        this.totalOdds = totalOdds;
        this.potentialWinnings = betAmount * totalOdds;
    }

    public double getPotentialWinnings() { return potentialWinnings; }

    public void setPotentialWinnings(double potentialWinnings) {
        this.potentialWinnings = potentialWinnings;
    }

    public String getBetId() { return betId; }
    public void setBetId(String betId) { this.betId = betId; }

    public BetStatus getStatus() { return status; }
    public void setStatus(BetStatus status) { this.status = status; }

    public void addMatch(Match match) {
        matches.add(match);
        recalculate();
    }

    public boolean removeMatch(Match match) {
        boolean removed = matches.remove(match);
        if (removed) {
            recalculate();
        }
        return removed;
    }

    public void clearMatches() {
        matches.clear();
        recalculate();
    }

    public String getFormattedPotentialWinnings() {
        return String.format("%,.2f", potentialWinnings);
    }

    public String getFormattedBetAmount() {
        return String.format("%,.2f", betAmount);
    }

    public String getFormattedTotalOdds() {
        return String.format("%.2f", totalOdds);
    }

    public int getMatchCount() {
        return matches.size();
    }

    public boolean isMultiBet() {
        return matches.size() > 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bet bet = (Bet) o;
        return Double.compare(bet.betAmount, betAmount) == 0 &&
                Double.compare(bet.totalOdds, totalOdds) == 0 &&
                Double.compare(bet.potentialWinnings, potentialWinnings) == 0 &&
                Objects.equals(matches, bet.matches) &&
                status == bet.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(matches, betAmount, totalOdds, potentialWinnings, status);
    }
}