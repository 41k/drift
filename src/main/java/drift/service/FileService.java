package drift.service;

import drift.configuration.properties.StaticContentStorageProperties;
import drift.model.ImageCategory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class FileService {

    private static final String FILE_PATH_FORMAT = "%s%s.%s";

    private final StaticContentStorageProperties properties;
    private final Tika fileMetadataProvider = new Tika();

    @SneakyThrows
    public String uploadImage(ImageCategory imageCategory, String fileName, MultipartFile file) {
        validateImage(file);
        var directoryPath = properties.getImageDirectoryPath(imageCategory);
        var fileExtension = getFileExtension(file);
        var filePath = String.format(FILE_PATH_FORMAT, directoryPath, fileName, fileExtension);
        Files.createDirectories(Paths.get(directoryPath));
        Files.write(Paths.get(filePath), file.getBytes());
        return filePath;
    }

    private String getFileExtension(MultipartFile file) {
        return file.getOriginalFilename().split("\\.")[1];
    }

    @SneakyThrows
    private void validateImage(MultipartFile file) {
        var contentType = fileMetadataProvider.detect(file.getInputStream());
        if (!contentType.startsWith("image")) {
            throw new IllegalArgumentException("File is not an image");
        }
    }
}
