package dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MatchRequest {
    @NotBlank(message = "Team 1 is required")
    @Size(max = 100, message = "Team 1 name must be less than 100 characters")
    private String team1;

    @NotBlank(message = "Team 2 is required")
    @Size(max = 100, message = "Team 2 name must be less than 100 characters")
    private String team2;

    @NotBlank(message = "Match date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    private String matchDate;

    @NotNull(message = "Odds are required")
    @Positive(message = "Odds must be positive")
    @DecimalMin(value = "1.01", message = "Odds must be at least 1.01")
    private Double odds;

    @NotBlank(message = "Bet type is required")
    @Size(max = 50, message = "Bet type must be less than 50 characters")
    private String betType;

    @NotBlank(message = "Bet choice is required")
    @Size(max = 50, message = "Bet choice must be less than 50 characters")
    private String betChosen;

    @Size(max = 100, message = "League name must be less than 100 characters")
    private String leagueName;

    @Size(max = 50, message = "Match ID must be less than 50 characters")
    private String matchId;

    @Size(max = 20, message = "Code must be less than 20 characters")
    private String code;

    @Size(max = 50, message = "Sport must be less than 50 characters")
    private String sport;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    private String date;

    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Time must be in HH:MM format")
    private String time;
}