package dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TicketRequest {
    @NotBlank(message = "Ticket code is required")
    @Size(min = 8, max = 20, message = "Ticket code must be between 8-20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Ticket code can only contain uppercase letters, numbers and hyphens")
    private String ticketCode;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must be less than 100 characters")
    private String location;

    @NotBlank(message = "TG is required")
    @Size(max = 50, message = "TG must be less than 50 characters")
    private String tg;

    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in YYYY-MM-DD format")
    private String date;

    @NotBlank(message = "IB is required")
    @Size(max = 50, message = "IB must be less than 50 characters")
    private String ib;

    @NotBlank(message = "NS is required")
    @Size(max = 50, message = "NS must be less than 50 characters")
    private String ns;

    @NotBlank(message = "Quota type is required")
    @Size(max = 50, message = "Quota type must be less than 50 characters")
    private String quotaType;

    @NotBlank(message = "Bet type is required")
    @Size(max = 50, message = "Bet type must be less than 50 characters")
    private String betType;

    @NotBlank(message = "Bet chosen is required")
    @Size(max = 50, message = "Bet chosen must be less than 50 characters")
    private String betChosen;

    @NotNull(message = "Bonus amount is required")
    @PositiveOrZero(message = "Bonus amount must be positive or zero")
    @DecimalMax(value = "1000.00", message = "Bonus amount cannot exceed 1000.00")
    private Double bonusAmount;
}