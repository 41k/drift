package drift.configuration.properties;

import drift.model.ImageCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotEmpty;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ConfigurationProperties(prefix = "static-content-storage")
public class StaticContentStorageProperties {

    @NotEmpty
    private Map<String, String> imageDirectories;

    public String getImageDirectoryPath(ImageCategory imageCategory) {
        var key = imageCategory.getKey();
        return Optional.ofNullable(imageDirectories.get(key))
                .orElseThrow(() -> new IllegalArgumentException("Images directory is not configured for key=" + key));
    }

    public String getImageUrlPattern(ImageCategory imageCategory) {
        return getImageDirectoryPath(imageCategory) + "**";
    }
}
