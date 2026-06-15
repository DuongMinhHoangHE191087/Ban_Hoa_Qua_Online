package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * DotEnvLoader — Đọc file .env và load vào System properties.
 *
 * Java/Tomcat không tự đọc .env, nên class này được gọi sớm nhất
 * trong AppStartupListener.contextInitialized() TRƯỚC khi AppConfig
 * được class-loaded, để các static final field trong AppConfig có thể
 * đọc được giá trị từ .env qua System.getProperty().
 *
 * Thứ tự ưu tiên: OS env var > .env > hardcoded default.
 */
public final class DotEnvLoader {

    private static final Logger log = Logger.getLogger(DotEnvLoader.class.getName());

    private DotEnvLoader() {}

    /**
     * Tìm và load .env từ:
     *   1. Parent của webappRealPath (project root trong dev)
     *   2. webappRealPath chính nó (fallback)
     *
     * @param webappRealPath  đường dẫn thực của thư mục webapp
     *                        (từ ServletContext.getRealPath(""))
     */
    public static void load(String webappRealPath) {
        if (webappRealPath == null) {
            log.warning("[DotEnvLoader] webappRealPath is null, skipping .env load");
            return;
        }

        Path envFile = resolveEnvFile(webappRealPath);
        if (envFile == null) {
            log.info("[DotEnvLoader] Không tìm thấy .env — dùng OS env vars hoặc default");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(envFile.toFile()))) {
            String line;
            int loaded = 0;
            int skipped = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;

                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                value = stripQuotes(value);

                if (key.isEmpty()) continue;

                // OS env var có quyền cao hơn .env — không override
                if (System.getenv(key) != null) {
                    skipped++;
                    continue;
                }
                System.setProperty(key, value);
                loaded++;
            }
            log.info(String.format("[DotEnvLoader] Loaded %d properties from %s (%d skipped — already in OS env)",
                    loaded, envFile, skipped));
        } catch (IOException e) {
            log.warning("[DotEnvLoader] Lỗi đọc .env: " + e.getMessage());
        }
    }

    private static Path resolveEnvFile(String webappRealPath) {
        // Thử project root (parent của webapp deploy dir)
        Path parent = Paths.get(webappRealPath).getParent();
        if (parent != null) {
            Path candidate = parent.resolve(".env");
            if (Files.exists(candidate)) return candidate;
        }
        // Fallback: trong chính webapp dir
        Path fallback = Paths.get(webappRealPath, ".env");
        if (Files.exists(fallback)) return fallback;
        return null;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
