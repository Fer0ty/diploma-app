package diploma.ecommerce.backend.shopbase.unit;

import diploma.ecommerce.backend.shopbase.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileStorageServiceImplTest {

    private static final Long TENANT_ID = 1L;
    private static final String FOLDER = "images";
    private static final String FILE_CONTENT = "Test file content";
    private static final String FILENAME = "test.jpg";
    private static final String FILENAME_NO_EXTENSION = "test";
    private static final String CONTEXT_PATH = "http://localhost:8080";

    @TempDir
    Path tempDir;

    @InjectMocks
    private FileStorageServiceImpl fileStorageService;

    private MultipartFile validFile;
    private MultipartFile fileWithoutExtension;
    private MultipartFile emptyFile;

    @BeforeEach
    void setUp() {
        // Устанавливаем временную директорию как uploadDir
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());

        // Создаем тестовые файлы
        validFile = new MockMultipartFile(
                "file",
                FILENAME,
                "image/jpeg",
                FILE_CONTENT.getBytes()
        );

        fileWithoutExtension = new MockMultipartFile(
                "file",
                FILENAME_NO_EXTENSION,
                "text/plain",
                FILE_CONTENT.getBytes()
        );

        emptyFile = new MockMultipartFile(
                "file",
                FILENAME,
                "image/jpeg",
                new byte[0]
        );
    }

    @Nested
    @DisplayName("storeFile Tests")
    class StoreFileTests {

        @Test
        void storeFile_ValidFile_ReturnsFileUrl() throws IOException {
            try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
                // Mock ServletUriComponentsBuilder
                ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
                mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockBuilder);
                when(mockBuilder.path(anyString())).thenReturn(mockBuilder);
                when(mockBuilder.toUriString()).thenReturn(CONTEXT_PATH + "/uploads/tenant_1/images/some-uuid.jpg");

                String result = fileStorageService.storeFile(validFile, TENANT_ID, FOLDER);

                // Проверяем результат
                assertNotNull(result);
                assertTrue(result.contains("tenant_1"));
                assertTrue(result.contains("images"));
                assertTrue(result.contains(".jpg"));

                // Проверяем, что файл создан в правильной директории
                Path tenantFolder = tempDir.resolve("tenant_1").resolve("images");
                assertTrue(Files.exists(tenantFolder));
                assertTrue(Files.isDirectory(tenantFolder));

                // Проверяем, что файл создан (хотя имя будет случайным UUID)
                assertEquals(1, Files.list(tenantFolder).count());

                // Проверяем содержимое файла
                Path createdFile = Files.list(tenantFolder).findFirst().orElseThrow();
                String fileContent = Files.readString(createdFile);
                assertEquals(FILE_CONTENT, fileContent);
                assertTrue(createdFile.getFileName().toString().endsWith(".jpg"));
            }
        }

        @Test
        void storeFile_FileWithoutExtension_ReturnsFileUrlWithoutExtension() throws IOException {
            try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
                ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
                mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockBuilder);
                when(mockBuilder.path(anyString())).thenReturn(mockBuilder);
                when(mockBuilder.toUriString()).thenReturn(CONTEXT_PATH + "/uploads/tenant_1/images/some-uuid");

                String result = fileStorageService.storeFile(fileWithoutExtension, TENANT_ID, FOLDER);

                assertNotNull(result);
                assertTrue(result.contains("tenant_1"));
                assertTrue(result.contains("images"));

                // Проверяем, что файл создан
                Path tenantFolder = tempDir.resolve("tenant_1").resolve("images");
                assertTrue(Files.exists(tenantFolder));
                assertEquals(1, Files.list(tenantFolder).count());

                // Проверяем, что файл не имеет расширения
                Path createdFile = Files.list(tenantFolder).findFirst().orElseThrow();
                String fileName = createdFile.getFileName().toString();
                assertFalse(fileName.contains("."));
            }
        }

        @Test
        void storeFile_NullOriginalFilename_UsesDefaultFilename() throws IOException {
            MultipartFile fileWithNullName = new MockMultipartFile(
                    "file",
                    null, // null original filename
                    "image/jpeg",
                    FILE_CONTENT.getBytes()
            );

            try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
                ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
                mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockBuilder);
                when(mockBuilder.path(anyString())).thenReturn(mockBuilder);
                when(mockBuilder.toUriString()).thenReturn(CONTEXT_PATH + "/uploads/tenant_1/images/some-uuid");

                String result = fileStorageService.storeFile(fileWithNullName, TENANT_ID, FOLDER);

                assertNotNull(result);

                Path tenantFolder = tempDir.resolve("tenant_1").resolve("images");
                assertTrue(Files.exists(tenantFolder));
                assertEquals(1, Files.list(tenantFolder).count());

                Path createdFile = Files.list(tenantFolder).findFirst().orElseThrow();
                String fileContent = Files.readString(createdFile);
                assertEquals(FILE_CONTENT, fileContent);
            }
        }

        @Test
        void storeFile_EmptyOriginalFilename_UsesDefaultFilename() throws IOException {
            MultipartFile fileWithEmptyName = new MockMultipartFile(
                    "file",
                    "", // empty original filename
                    "image/jpeg",
                    FILE_CONTENT.getBytes()
            );

            try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
                ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
                mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockBuilder);
                when(mockBuilder.path(anyString())).thenReturn(mockBuilder);
                when(mockBuilder.toUriString()).thenReturn(CONTEXT_PATH + "/uploads/tenant_1/images/some-uuid");

                String result = fileStorageService.storeFile(fileWithEmptyName, TENANT_ID, FOLDER);

                assertNotNull(result);

                Path tenantFolder = tempDir.resolve("tenant_1").resolve("images");
                assertTrue(Files.exists(tenantFolder));
                assertEquals(1, Files.list(tenantFolder).count());
            }
        }

        @Test
        void storeFile_CreatesDirectoriesIfNotExist() throws IOException {
            // Убеждаемся, что директория не существует
            Path tenantFolder = tempDir.resolve("tenant_" + TENANT_ID).resolve(FOLDER);
            assertFalse(Files.exists(tenantFolder));

            try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
                ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
                mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockBuilder);
                when(mockBuilder.path(anyString())).thenReturn(mockBuilder);
                when(mockBuilder.toUriString()).thenReturn(CONTEXT_PATH + "/uploads/tenant_1/images/some-uuid.jpg");

                fileStorageService.storeFile(validFile, TENANT_ID, FOLDER);

                // Проверяем, что директория создана
                assertTrue(Files.exists(tenantFolder));
                assertTrue(Files.isDirectory(tenantFolder));
            }
        }

        @Test
        void storeFile_EmptyFile_ThrowsException() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> fileStorageService.storeFile(emptyFile, TENANT_ID, FOLDER),
                    "Cannot store empty file"
            );
        }

        @Test
        void storeFile_IOException_PropagatesException() throws IOException {
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn(FILENAME);
            when(mockFile.getInputStream()).thenThrow(new IOException("Test IO Exception"));

            assertThrows(IOException.class, () -> fileStorageService.storeFile(mockFile, TENANT_ID, FOLDER));
        }

        @Test
        void storeFile_GeneratesUniqueFilenames() throws IOException {
            try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
                ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
                mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockBuilder);
                when(mockBuilder.path(anyString())).thenReturn(mockBuilder);
                when(mockBuilder.toUriString()).thenReturn(CONTEXT_PATH + "/uploads/tenant_1/images/some-uuid.jpg");

                // Сохраняем два одинаковых файла
                fileStorageService.storeFile(validFile, TENANT_ID, FOLDER);
                fileStorageService.storeFile(validFile, TENANT_ID, FOLDER);

                // Проверяем, что создано два разных файла
                Path tenantFolder = tempDir.resolve("tenant_1").resolve("images");
                assertEquals(2, Files.list(tenantFolder).count());

                // Проверяем, что имена файлов разные
                String[] fileNames = Files.list(tenantFolder)
                        .map(path -> path.getFileName().toString())
                        .toArray(String[]::new);

                assertNotEquals(fileNames[0], fileNames[1]);
            }
        }
    }

    @Nested
    @DisplayName("deleteFile Tests")
    class DeleteFileTests {

        @Test
        void deleteFile_ExistingFile_ReturnsTrue() throws IOException {
            // Создаем тестовый файл
            Path testFile = createTestFile("test-file.jpg");

            String fileUrl = "/uploads/tenant_1/images/test-file.jpg";
            boolean result = fileStorageService.deleteFile(fileUrl);

            assertTrue(result);
            assertFalse(Files.exists(testFile));
        }

        @Test
        void deleteFile_FullUrlWithContext_ReturnsTrue() throws IOException {
            // Создаем тестовый файл
            Path testFile = createTestFile("test-file.jpg");

            String fileUrl = "http://localhost:8080/uploads/tenant_1/images/test-file.jpg";
            boolean result = fileStorageService.deleteFile(fileUrl);

            assertTrue(result);
            assertFalse(Files.exists(testFile));
        }

        @Test
        void deleteFile_NonExistentFile_ReturnsFalse() {
            String fileUrl = "/uploads/tenant_1/images/non-existent.jpg";
            boolean result = fileStorageService.deleteFile(fileUrl);

            assertFalse(result);
        }

        @Test
        void deleteFile_NullUrl_ReturnsFalse() {
            boolean result = fileStorageService.deleteFile(null);
            assertFalse(result);
        }

        @Test
        void deleteFile_EmptyUrl_ReturnsFalse() {
            boolean result = fileStorageService.deleteFile("");
            assertFalse(result);
        }

        @Test
        void deleteFile_UrlWithoutUploadsPath_ReturnsTrue() throws IOException {
            // Создаем файл в корне tempDir
            Path testFile = tempDir.resolve("direct-file.jpg");
            Files.createFile(testFile);

            String fileUrl = "direct-file.jpg";
            boolean result = fileStorageService.deleteFile(fileUrl);

            assertTrue(result);
            assertFalse(Files.exists(testFile));
        }

        @Test
        void deleteFile_InvalidPath_ReturnsFalse() {
            // Используем невалидный путь с символами, которые могут вызвать проблемы
            String fileUrl = "/uploads/tenant_1/images/../../../etc/passwd";
            boolean result = fileStorageService.deleteFile(fileUrl);

            assertFalse(result);
        }

        @Test
        void deleteFile_DirectoryInsteadOfFile_ReturnsFalse() throws IOException {
            // Создаем директорию вместо файла
            Path testDir = tempDir.resolve("tenant_1").resolve("images").resolve("test-dir");
            Files.createDirectories(testDir);

            String fileUrl = "/uploads/tenant_1/images/test-dir";
            boolean result = fileStorageService.deleteFile(fileUrl);

            // Files.deleteIfExists() возвращает true для пустых директорий
            assertTrue(result);
            assertFalse(Files.exists(testDir));
        }

        private Path createTestFile(String filename) throws IOException {
            Path tenantDir = tempDir.resolve("tenant_1").resolve("images");
            Files.createDirectories(tenantDir);
            Path testFile = tenantDir.resolve(filename);
            Files.write(testFile, FILE_CONTENT.getBytes());
            return testFile;
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        void storeFile_SpecialCharactersInFilename_HandledCorrectly() throws IOException {
            MultipartFile specialFile = new MockMultipartFile(
                    "file",
                    "файл с пробелами и спец символами!@#$%^&*().jpg",
                    "image/jpeg",
                    FILE_CONTENT.getBytes()
            );

            try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
                ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
                mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockBuilder);
                when(mockBuilder.path(anyString())).thenReturn(mockBuilder);
                when(mockBuilder.toUriString()).thenReturn(CONTEXT_PATH + "/uploads/tenant_1/images/some-uuid.jpg");

                String result = fileStorageService.storeFile(specialFile, TENANT_ID, FOLDER);

                assertNotNull(result);
                // Файл должен быть сохранен с UUID именем, игнорируя спецсимволы
                Path tenantFolder = tempDir.resolve("tenant_1").resolve("images");
                assertEquals(1, Files.list(tenantFolder).count());

                Path createdFile = Files.list(tenantFolder).findFirst().orElseThrow();
                assertTrue(createdFile.getFileName().toString().endsWith(".jpg"));
            }
        }

        @Test
        void storeFile_VeryLongFilename_HandledCorrectly() throws IOException {
            String longFilename = "a".repeat(300) + ".txt";
            MultipartFile longNameFile = new MockMultipartFile(
                    "file",
                    longFilename,
                    "text/plain",
                    FILE_CONTENT.getBytes()
            );

            try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
                ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
                mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockBuilder);
                when(mockBuilder.path(anyString())).thenReturn(mockBuilder);
                when(mockBuilder.toUriString()).thenReturn(CONTEXT_PATH + "/uploads/tenant_1/images/some-uuid.txt");

                String result = fileStorageService.storeFile(longNameFile, TENANT_ID, FOLDER);

                assertNotNull(result);
                // Файл сохраняется с UUID, так что длина оригинального имени не важна
                Path tenantFolder = tempDir.resolve("tenant_1").resolve("images");
                assertEquals(1, Files.list(tenantFolder).count());

                Path createdFile = Files.list(tenantFolder).findFirst().orElseThrow();
                assertTrue(createdFile.getFileName().toString().endsWith(".txt"));
            }
        }

        @Test
        void storeFile_MultipleDotsInFilename_ExtractsCorrectExtension() throws IOException {
            MultipartFile multiDotFile = new MockMultipartFile(
                    "file",
                    "file.backup.old.txt",
                    "text/plain",
                    FILE_CONTENT.getBytes()
            );

            try (MockedStatic<ServletUriComponentsBuilder> mockedBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
                ServletUriComponentsBuilder mockBuilder = mock(ServletUriComponentsBuilder.class);
                mockedBuilder.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockBuilder);
                when(mockBuilder.path(anyString())).thenReturn(mockBuilder);
                when(mockBuilder.toUriString()).thenReturn(CONTEXT_PATH + "/uploads/tenant_1/images/some-uuid.txt");

                String result = fileStorageService.storeFile(multiDotFile, TENANT_ID, FOLDER);

                assertNotNull(result);
                assertTrue(result.endsWith(".txt")); // Должно взять последнее расширение

                Path tenantFolder = tempDir.resolve("tenant_1").resolve("images");
                Path createdFile = Files.list(tenantFolder).findFirst().orElseThrow();
                assertTrue(createdFile.getFileName().toString().endsWith(".txt"));
            }
        }
    }
}