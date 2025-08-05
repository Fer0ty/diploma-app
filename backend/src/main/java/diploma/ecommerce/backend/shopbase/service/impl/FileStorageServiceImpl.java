package diploma.ecommerce.backend.shopbase.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import diploma.ecommerce.backend.shopbase.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file, Long tenantId, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "file";
        }

        // Создаем безопасное имя файла с уникальным ID
        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex);
        }

        String filename = UUID.randomUUID() + fileExtension;

        // Создаем директории для tenant/folder если они не существуют
        Path tenantFolderPath = Paths.get(uploadDir, "tenant_" + tenantId, folder);
        Files.createDirectories(tenantFolderPath);

        // Сохраняем файл
        Path targetPath = tenantFolderPath.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File saved: {}", targetPath);

        // Формируем и возвращаем URL для доступа к файлу
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path("tenant_" + tenantId + "/")
                .path(folder + "/")
                .path(filename)
                .toUriString();
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        // Извлекаем путь к файлу из URL
        String relativePath = fileUrl;
        if (fileUrl.contains("/uploads/")) {
            relativePath = fileUrl.substring(fileUrl.indexOf("/uploads/") + 9);
        }

        try {
            Path filePath = Paths.get(uploadDir, relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error deleting file: {}", fileUrl, e);
            return false;
        }
    }
}
