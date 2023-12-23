package project.vegist.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import static project.vegist.common.AppConstants.BASE_URL;

@Component
public class FileUtils {
    // Remove the static keyword
    @Value("${upload-path}")
    private String uploadDirectory;

    public static String generateUniqueFileName(String originalFileName) {
        try {
            int lastDotIndex = originalFileName.lastIndexOf(".");
            String baseName = (lastDotIndex >= 0) ? originalFileName.substring(0, lastDotIndex) : originalFileName;
            String extension = (lastDotIndex >= 0) ? originalFileName.substring(lastDotIndex) : "";

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(baseName.getBytes(StandardCharsets.UTF_8));

            // Use Base64.getUrlEncoder() for URL-safe encoding
            String encodedHash = Base64.getUrlEncoder().encodeToString(hash);

            return (encodedHash.replaceAll("[^a-zA-Z0-9]", "") + extension).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating unique file name", e);
        }
    }

    public static void saveFileAsBase64(String base64Data, String filePath) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

        // Create parent directories if they don't exist
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        // Write the decoded bytes to the file
        try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE)) {
            outputStream.write(decodedBytes);
        }
    }

    public String uploadFile(MultipartFile file, boolean checkDuplicate) throws IOException {
        try {
            String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName);
            String subFolder = determineSubFolder(fileExtension);

            // Sử dụng đường dẫn tương đối từ thư mục làm việc hiện tại
            String relativePath = subFolder + "/" + generateUniqueFileName(originalFileName, file);

            Path filePath = Paths.get(uploadDirectory, relativePath).normalize();
            Files.createDirectories(filePath.getParent());

            if (checkDuplicate) {
                String existingFilePath = findExistingFilePath(filePath, file);
                if (existingFilePath != null) {
                    return existingFilePath;
                }
            }

            byte[] fileBytes = file.getBytes();
            String base64File = encodeFileToBase64(fileBytes);

            saveFileAsBase64(base64File, filePath.toString());

            return BASE_URL + relativePath;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private String findExistingFilePath(Path filePath, MultipartFile file) {
        try {
            if (Files.exists(filePath)) {
                // Check if the content of the existing file matches the new file
                byte[] existingFileBytes = Files.readAllBytes(filePath);
                byte[] newFileBytes = file.getBytes();

                if (Arrays.equals(existingFileBytes, newFileBytes)) {
                    // Extract the relative path from the absolute path
                    String relativePath = filePath.toString().replace(uploadDirectory, "").replace("\\", "/");
                    int startIndex = relativePath.indexOf("src/main/resources/static/");

                    if (startIndex != -1) {
                        return BASE_URL + relativePath.substring(startIndex + "src/main/resources/static/".length());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String encodeFileToBase64(byte[] fileBytes) {
        return Base64.getEncoder().encodeToString(fileBytes);
    }


    // Modify this method to generate a unique filename based on content
    public static String generateUniqueFileName(String originalFileName, MultipartFile file) {
        try {
            int lastDotIndex = originalFileName.lastIndexOf(".");
            String baseName = (lastDotIndex >= 0) ? originalFileName.substring(0, lastDotIndex) : originalFileName;
            String extension = (lastDotIndex >= 0) ? originalFileName.substring(lastDotIndex) : "";

            // Thêm mã băm của nội dung file vào tên để đảm bảo tính duy nhất
            byte[] contentBytes = file.getBytes();
            MessageDigest contentDigest = MessageDigest.getInstance("SHA-256");
            byte[] contentHash = contentDigest.digest(contentBytes);
            String contentHashString = Base64.getUrlEncoder().encodeToString(contentHash).replaceAll("[^a-zA-Z0-9]", "");

            return (baseName + "_" + contentHashString + extension).toLowerCase();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating unique file name", e);
        }
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

    public static String getOriginalFileNameFromBase64(String base64Image) {
        byte[] decodedBytes = decodeBase64Image(base64Image);
        String originalFileName = "defaultFileName"; // Tên mặc định nếu không thể lấy được tên file

        try {
            originalFileName = new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return originalFileName;
    }

    public static String getOriginalFileNameFromMultipartFile(MultipartFile file) {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);

        String subFolder = determineSubFolder(fileExtension);
        String uniqueFileName = generateUniqueFileName(originalFileName);
        String fileNameWithoutExtension = uniqueFileName.replaceFirst("[.][^.]+$", ""); // Remove the file extension

        return subFolder + "/" + reverseSHA256(fileNameWithoutExtension);
    }

    public static String decodeAndSaveFile(MultipartFile file, String filePath) throws IOException {
        byte[] imageBytes = file.getBytes();
        String base64Image = encodeImageToBase64(imageBytes);

        return decodeAndSaveFile(base64Image, filePath);
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


    public static boolean isImageFile(String fileExtension) {
        return Arrays.asList("jpg", "png", "gif").contains(fileExtension.toLowerCase());
    }

    public static boolean isVideoFile(String fileExtension) {
        return Arrays.asList("mp4", "avi", "mkv").contains(fileExtension.toLowerCase());
    }

    private static String determineSubFolder(String fileExtension) {
        if (isImageFile(fileExtension)) {
            return "images";
        } else if (isVideoFile(fileExtension)) {
            return "videos";
        } else {
            return "other";
        }
    }

    public static String encodeImageToBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public static String encodeImageToBase64(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        return encodeImageToBase64(imageBytes);
    }

    public static String encodeImageToBase64(MultipartFile imageFile) throws IOException {
        byte[] imageBytes = imageFile.getBytes();
        return encodeImageToBase64(imageBytes);
    }

    public static byte[] decodeBase64Image(String base64Image) {
        return Base64.getDecoder().decode(base64Image);
    }

    public static void saveDecodedFile(byte[] decodedBytes, String filePath) throws IOException {
        Files.write(Paths.get(filePath), decodedBytes);
    }

    public static String decodeAndSaveFile(String base64Data, String filePath) throws IOException {
        byte[] decodedBytes = decodeBase64Image(base64Data);
        saveDecodedFile(decodedBytes, filePath);

        return filePath;
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
}
