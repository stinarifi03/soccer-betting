package dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ClearResponse {
    private final List<String> clearedFields;
    private final String message;
    private final boolean success;
    private final String timestamp;

    public ClearResponse(List<String> clearedFields, String message) {
        this.clearedFields = clearedFields;
        this.message = message;
        this.success = !clearedFields.isEmpty();
        this.timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public int getClearedFieldCount() {
        return clearedFields.size();
    }

    public boolean isFullReset() {
        return clearedFields.contains("all");
    }
}