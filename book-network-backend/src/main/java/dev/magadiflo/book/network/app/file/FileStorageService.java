package dev.magadiflo.book.network.app.file;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileStorageService {

    @Value("${application.file.upload.photos-output-path}")
    private String fileUploadPath;

    public String saveFile(@Nonnull MultipartFile file, @Nonnull Long userId) {
        final String fileUploadSubPath = "users" + File.separator + userId;
        return this.uploadFile(file, fileUploadSubPath);
    }

    public void deleteImageIfExists(String bookCover) {
        if (Strings.isNotBlank(bookCover)) {
            try {
                Path path = Paths.get(bookCover);
                Files.deleteIfExists(path);
            } catch (IOException | InvalidPathException e) {
                log.error("Ocurrió un problema al eliminar la imagen: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String uploadFile(@Nonnull MultipartFile file, @Nonnull String fileUploadSubPath) {
        final String finalUploadPath = this.fileUploadPath + File.separator + fileUploadSubPath;
        File targetFolder = new File(finalUploadPath);
        if (!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs();

            if (!folderCreated) {
                log.warn("Falló al crear el folder de destino (target)");
                return null;
            }
        }

        final String fileExtension = this.getFileExtension(file.getOriginalFilename());
        String targetFilePath = finalUploadPath + File.separator + System.currentTimeMillis() + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);

        try {
            Files.write(targetPath, file.getBytes());
            log.info("Archivo guardado en {}", targetFilePath);
            return targetFilePath;
        } catch (IOException e) {
            log.error("El archivo no pudo ser guardado", e);
        }
        return null;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) return "";

        int lastDotIndex = filename.lastIndexOf(".");

        if (lastDotIndex == -1) return "";

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}
