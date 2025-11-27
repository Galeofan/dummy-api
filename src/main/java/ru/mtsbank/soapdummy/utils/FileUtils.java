package ru.mtsbank.soapdummy.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.mtsbank.soapdummy.mock.exception.ReadMockContendException;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Util class for work with files
 */
@Slf4j
@ParametersAreNonnullByDefault
public class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("This class is util only");
    }

    /**
     * Get file content in string format
     * @param filePath file path to content
     * @return content in string format
     */
    @NonNull
    public static String getFileContent(String filePath) {
        return getFileContentWithoutCheckError(filePath);
    }

    /**
     * Get file content in string format, and skip check Errors
     * @param filePath file path to content
     * @return content in string format
     */
    @NonNull
    public static String getFileContentWithoutCheckError(String filePath) {
        try {
            return new String(getFileContentBytes(filePath), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            log.error("Not found file by path: {}", filePath, e);
            throw new ReadMockContendException("Not found file by path " + filePath, e);
        } catch (IOException e) {
            log.error("Error while read file by path: {}", filePath, e);
            throw new ReadMockContendException("Error while read file by path " + filePath, e);
        }
    }

    /**
     * Get file content in bytes array, and skip check Errors
     * @param filePath file path to content
     * @return content in bytes array
     * @throws IOException error while work with files (IO)
     */
    public static byte[] getFileContentBytes(String filePath) throws IOException {
        Objects.requireNonNull(filePath, "filePath must be added");
        File file = new File(filePath);
        try (FileInputStream fio = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fio)) {
            return bis.readAllBytes();
        } catch (FileNotFoundException e) {
            log.error("Not found file by path: {}", filePath, e);
            throw e;
        } catch (IOException e) {
            log.error("Error while read file by path: {}", filePath, e);
            throw e;
        }
    }


}
