package project.vegist.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import static project.vegist.common.AppConstants.BASE_URL;

@Component
public class FileUtils {
    private String uploadDirectory;

    @Value("${upload-path}")
    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public static String generateUniqueFileName(String originalFileName) {
        try {
            int lastDotIndex = originalFileName.lastIndexOf(".");
            String baseName;
            String extension = "";

            if (lastDotIndex >= 0) {
                baseName = originalFileName.substring(0, lastDotIndex);
                extension = originalFileName.substring(lastDotIndex);
            } else {
                baseName = originalFileName;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(baseName.getBytes());

            String encodedHash = Base64.getEncoder().encodeToString(hash);

            return (encodedHash.replaceAll("[^a-zA-Z0-9]", "") + extension).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return originalFileName;
        }
    }

    public static void saveFile(MultipartFile file, String filePath) throws IOException {
        Path destination = Path.of(filePath);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    public static String joinPaths(String path1, String path2) {
        return Path.of(path1, path2).toString();
    }

    public static String getFileExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "";
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex >= 0) {
            return originalFilename.substring(lastDotIndex + 1).toLowerCase();
        } else {
            return "";
        }
    }

    public static String reverseSHA256(String encodedHash) {
        try {
            byte[] decodedHash = Base64.getDecoder().decode(encodedHash);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] originalBytes = sha256.digest(decodedHash);

            StringBuilder result = new StringBuilder();
            for (byte b : originalBytes) {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String fileExtension = FileUtils.getFileExtension(originalFileName);

        String subFolder;
        if (isImageFile(fileExtension)) {
            subFolder = "images";
        } else if (isVideoFile(fileExtension)) {
            subFolder = "videos";
        } else {
            subFolder = "other";
        }

        String folderPath = FileUtils.joinPaths(uploadDirectory, subFolder);
        Files.createDirectories(Paths.get(folderPath));

        String uniqueFileName = FileUtils.generateUniqueFileName(originalFileName);
        String filePath = FileUtils.joinPaths(folderPath, uniqueFileName);

        FileUtils.saveFile(file, filePath);

        // Tạo URL dựa trên subFolder và uniqueFileName

        return BASE_URL + subFolder + "/" + uniqueFileName;
    }

    public static String getFileNameFromUrl(String fileUrl) {
        if (!fileUrl.startsWith(BASE_URL)) {
            return fileUrl; // Trả về nguyên fileUrl nếu không hợp lệ
        }

        // Cắt bỏ baseUrl
        String pathWithoutBaseUrl = fileUrl.substring(BASE_URL.length());

        // Kiểm tra subFolder và cắt bỏ nếu có
        String subFolder = "";
        if (pathWithoutBaseUrl.startsWith(subFolder + "/")) {
            int lastSlashIndex = pathWithoutBaseUrl.lastIndexOf("/");
            if (lastSlashIndex != -1) {
                return pathWithoutBaseUrl.substring(lastSlashIndex + 1);
            }
        }

        return pathWithoutBaseUrl;
    }

    public static String getOriginalFileNameFromUrl(String fileUrl) {
        if (!fileUrl.startsWith(BASE_URL)) {
            return fileUrl; // Trả về nguyên fileUrl nếu không hợp lệ
        }

        // Cắt bỏ baseUrl
        String pathWithoutBaseUrl = fileUrl.substring(BASE_URL.length());

        String subFolder = "";
        if (pathWithoutBaseUrl.startsWith(subFolder + "/")) {
            int lastSlashIndex = pathWithoutBaseUrl.lastIndexOf("/");
            if (lastSlashIndex != -1) {
                String encodedFileName = pathWithoutBaseUrl.substring(lastSlashIndex + 1);
                return reverseSHA256(encodedFileName);
            }
        }

        return pathWithoutBaseUrl;
    }


    public static boolean isImageFile(String fileExtension) {
        return Arrays.asList("jpg", "png", "gif").contains(fileExtension.toLowerCase());
    }

    public static boolean isVideoFile(String fileExtension) {
        return Arrays.asList("mp4", "avi", "mkv").contains(fileExtension.toLowerCase());
    }


}
