package drift.configuration;

import drift.configuration.properties.StaticContentStorageProperties;
import drift.model.ImageCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticContentConfiguration implements WebMvcConfigurer {

    @Autowired
    private StaticContentStorageProperties staticContentStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler(staticContentStorageProperties.getImageUrlPattern(ImageCategory.USER))
                .addResourceLocations("file:" + staticContentStorageProperties.getImageDirectoryPath(ImageCategory.USER));
        registry
                .addResourceHandler(staticContentStorageProperties.getImageUrlPattern(ImageCategory.CAR))
                .addResourceLocations("file:" + staticContentStorageProperties.getImageDirectoryPath(ImageCategory.CAR));
        registry
                .addResourceHandler(staticContentStorageProperties.getImageUrlPattern(ImageCategory.ORGANISATION))
                .addResourceLocations("file:" + staticContentStorageProperties.getImageDirectoryPath(ImageCategory.ORGANISATION));
        registry
                .addResourceHandler(staticContentStorageProperties.getImageUrlPattern(ImageCategory.CHAMPIONSHIP_STAGE))
                .addResourceLocations("file:" + staticContentStorageProperties.getImageDirectoryPath(ImageCategory.CHAMPIONSHIP_STAGE));
        registry
                .addResourceHandler(staticContentStorageProperties.getImageUrlPattern(ImageCategory.TRAINING))
                .addResourceLocations("file:" + staticContentStorageProperties.getImageDirectoryPath(ImageCategory.TRAINING));
    }
}
