package dev.magadiflo.book.network.app.file;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtils {
    public static byte[] readFileFromLocation(String fileUrl) {
        if (StringUtils.isNotBlank(fileUrl)) {
            try {
                Path filePath = new File(fileUrl).toPath();
                return Files.readAllBytes(filePath);
            } catch (IOException e) {
                log.warn("No se encontró el archivo en la ruta {}", fileUrl);
            }
        }
        return null;
    }
}
