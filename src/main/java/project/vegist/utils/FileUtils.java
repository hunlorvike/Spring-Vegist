package project.vegist.utils;

import org.springframework.util.ResourceUtils;
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
import java.util.List;

public class FileUtils {

    /**
     * Đọc nội dung của file thành một list các dòng.
     *
     * @param filePath Đường dẫn tuyệt đối của file.
     * @return List các dòng trong file.
     * @throws IOException Nếu có lỗi khi đọc file.
     */
    public static List<String> readLines(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }

    /**
     * Ghi list các dòng vào file.
     *
     * @param filePath Đường dẫn tuyệt đối của file.
     * @param lines    List các dòng cần ghi vào file.
     * @throws IOException Nếu có lỗi khi ghi vào file.
     */
    public static void writeLines(String filePath, List<String> lines) throws IOException {
        Files.write(Paths.get(filePath), lines);
    }

    /**
     * Kiểm tra sự tồn tại của file.
     *
     * @param filePath Đường dẫn tuyệt đối của file.
     * @return true nếu file tồn tại, false nếu không tồn tại.
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Xóa file.
     *
     * @param filePath Đường dẫn tuyệt đối của file.
     * @return true nếu xóa thành công, false nếu không xóa được hoặc file không tồn tại.
     * @throws IOException Nếu có lỗi khi xóa file.
     */
    public static boolean deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            return true;
        }
        return false;
    }

    /**
     * Lấy đường dẫn tuyệt đối của file trong thư mục classpath.
     *
     * @param relativePath Đường dẫn tương đối của file trong classpath.
     * @return Đường dẫn tuyệt đối của file.
     * @throws IOException Nếu có lỗi khi lấy đường dẫn.
     */
    public static String getClasspathFilePath(String relativePath) throws IOException {
        return ResourceUtils.getFile("classpath:" + relativePath).getAbsolutePath();
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

    public static boolean isImageFile(String fileExtension) {
        return Arrays.asList("jpg", "png", "gif").contains(fileExtension.toLowerCase());
    }

    public static boolean isVideoFile(String fileExtension) {
        return Arrays.asList("mp4", "avi", "mkv").contains(fileExtension.toLowerCase());
    }


}
