package dto;

import java.util.List;

public class ClearRequest {
    private List<String> fieldsToClear;

    // Getters and setters
    public List<String> getFieldsToClear() {
        return fieldsToClear;
    }

    public void setFieldsToClear(List<String> fieldsToClear) {
        this.fieldsToClear = fieldsToClear;
    }
}
