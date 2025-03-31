package dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageVerificationResponse {
    private final boolean allImagesValid;
    private final Instant timestamp;
    private final Map<String, Boolean> imageStatus;
    private final List<String> missingImages;
    private final List<String> corruptedImages;
    private final int totalImages;
    private final int validImagesCount;
    private final String basePath;

    public ImageVerificationResponse(Map<String, Boolean> imageStatus) {
        this.timestamp = Instant.now();
        this.imageStatus = Map.copyOf(imageStatus);
        this.totalImages = imageStatus.size();

        this.missingImages = imageStatus.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        this.validImagesCount = totalImages - missingImages.size();
        this.allImagesValid = missingImages.isEmpty();
        this.corruptedImages = List.of(); // Can be enhanced to detect corrupt files
        this.basePath = "/static/images/";
    }

    public String getVerificationSummary() {
        if (allImagesValid) {
            return String.format("All %d images are valid and accessible", totalImages);
        }
        return String.format("%d/%d images valid. Missing: %s",
                validImagesCount,
                totalImages,
                String.join(", ", missingImages));
    }

    public boolean isImageValid(String imageName) {
        return imageStatus.getOrDefault(imageName, false);
    }
}