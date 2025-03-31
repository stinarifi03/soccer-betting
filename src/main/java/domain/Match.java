package domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Match {
    private String team1;
    private String team2;
    private String matchDate;
    private double odds;
    private String betType;
    private String betChosen;
    private String leagueName;
    private String matchId;
    private String code;
    private String sport;
    private String date;
    private String time;
    private LocalDateTime timestamp;

    public Match() {
        this.timestamp = LocalDateTime.now();
    }

    public Match(String team1, String team2, String matchDate, double odds, String betType,
                 String betChosen, String leagueName, String matchId, String code,
                 String sport, String date, String time) {
        this();
        this.team1 = team1;
        this.team2 = team2;
        this.matchDate = matchDate;
        this.odds = odds;
        this.betType = betType;
        this.betChosen = betChosen;
        this.leagueName = leagueName;
        this.matchId = matchId;
        this.code = code;
        this.sport = sport;
        this.date = date;
        this.time = time;
    }

    // Getters and Setters
    public String getTeam1() { return team1; }
    public void setTeam1(String team1) { this.team1 = team1; }

    public String getTeam2() { return team2; }
    public void setTeam2(String team2) { this.team2 = team2; }

    public String getMatchDate() { return matchDate; }
    public void setMatchDate(String matchDate) { this.matchDate = matchDate; }

    public double getOdds() { return odds; }
    public void setOdds(double odds) { this.odds = odds; }

    public String getBetType() { return betType; }
    public void setBetType(String betType) { this.betType = betType; }

    public String getBetChosen() { return betChosen; }
    public void setBetChosen(String betChosen) { this.betChosen = betChosen; }

    public String getLeagueName() { return leagueName; }
    public void setLeagueName(String leagueName) { this.leagueName = leagueName; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Double.compare(match.odds, odds) == 0 &&
                Objects.equals(team1, match.team1) &&
                Objects.equals(team2, match.team2) &&
                Objects.equals(matchDate, match.matchDate) &&
                Objects.equals(betType, match.betType) &&
                Objects.equals(betChosen, match.betChosen) &&
                Objects.equals(leagueName, match.leagueName) &&
                Objects.equals(matchId, match.matchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(team1, team2, matchDate, odds, betType, betChosen, leagueName, matchId);
    }

    @Override
    public String toString() {
        return String.format("%s vs %s (%s) - %s @ %.2f", team1, team2, leagueName, betChosen, odds);
    }
}