package diploma.ecommerce.backend.shopbase.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file, Long tenantId, String folder) throws IOException;

    boolean deleteFile(String fileUrl);
}
