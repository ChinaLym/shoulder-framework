package org.shoulder.core.i18;

import jakarta.annotation.Nonnull;
import org.shoulder.core.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Locale → Charset
 *
 * @author lym
 */
public class CharsetMap {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * The name for charset mapper resources.
     */
    public static final String CHARSET_RESOURCE = "charset.properties";

    private final ConcurrentMap<String, Charset> charsetMapper = getDefaultCharsetMap();

    // ----------- load -------------

    protected static ConcurrentMap<String, String> loadStream(InputStream input) throws IOException {
        Properties props = new Properties();

        props.load(input);
        return new ConcurrentHashMap(props);
    }

    protected static ConcurrentMap<String, String> loadFile(File file) throws IOException {
        return loadStream(new FileInputStream(file));
    }

    protected static ConcurrentMap<String, String> loadPath(String path) throws IOException {
        return loadFile(new File(path));
    }

    protected static ConcurrentMap<String, String> loadResource(String name) {
        InputStream input = CharsetMap.class.getResourceAsStream(name);

        if (input != null) {
            try {
                return loadStream(input);
            } catch (IOException ignored) {
            }
        }
        return new ConcurrentHashMap<>(0);
    }

    public CharsetMap() {

        // current class jar
        addMapping(loadResource("/META-INF/" + CHARSET_RESOURCE));

        // java_home
        try {
            String path = System.getProperty("java.home") + File.separator + "lib" + File.separator + CHARSET_RESOURCE;
            addMapping(loadPath(path));
        } catch (Exception ignored) {
        }

        // user home
        try {
            String path = System.getProperty("user.home") + File.separator + CHARSET_RESOURCE;
            addMapping(loadPath(path));
        } catch (Exception ignored) {
        }

    }

    private void addMapping(Map<String, String> map) {
        map.forEach((k, v) -> {
            if (StringUtils.isBlank(k)) {
                return;
            }
            try {
                // 将 _ 转换成 -
                if (LocaleUtils.isSupportedLocale(Locale.forLanguageTag(k.replace("_", "-")))) {
                    charsetMapper.put(k, Charset.forName(v));
                }
            } catch (Exception ignored) {
            }
        });
    }


    private static ConcurrentMap<String, Charset> getDefaultCharsetMap() {
        ConcurrentMap<String, Charset> commonMapper = new ConcurrentHashMap<>();
        commonMapper.put("ar", Charset.forName("ISO-8859-6"));
        commonMapper.put("be", Charset.forName("ISO-8859-5"));
        commonMapper.put("bg", Charset.forName("ISO-8859-5"));
        commonMapper.put("ca", StandardCharsets.ISO_8859_1);
        commonMapper.put("cs", Charset.forName("ISO-8859-2"));
        commonMapper.put("da", StandardCharsets.ISO_8859_1);
        commonMapper.put("de", StandardCharsets.ISO_8859_1);
        commonMapper.put("el", Charset.forName("ISO-8859-7"));
        commonMapper.put("en", StandardCharsets.ISO_8859_1);
        commonMapper.put("es", StandardCharsets.ISO_8859_1);
        commonMapper.put("et", StandardCharsets.ISO_8859_1);
        commonMapper.put("fi", StandardCharsets.ISO_8859_1);
        commonMapper.put("fr", StandardCharsets.ISO_8859_1);
        commonMapper.put("hr", Charset.forName("ISO-8859-2"));
        commonMapper.put("hu", Charset.forName("ISO-8859-2"));
        commonMapper.put("is", StandardCharsets.ISO_8859_1);
        commonMapper.put("it", StandardCharsets.ISO_8859_1);
        commonMapper.put("iw", Charset.forName("ISO-8859-8"));
        commonMapper.put("ja", Charset.forName("Shift_JIS"));
        commonMapper.put("ko", Charset.forName("EUC-KR"));
        commonMapper.put("lt", Charset.forName("ISO-8859-2"));
        commonMapper.put("lv", Charset.forName("ISO-8859-2"));
        commonMapper.put("mk", Charset.forName("ISO-8859-5"));
        commonMapper.put("nl", StandardCharsets.ISO_8859_1);
        commonMapper.put("no", StandardCharsets.ISO_8859_1);
        commonMapper.put("pl", Charset.forName("ISO-8859-2"));
        commonMapper.put("pt", StandardCharsets.ISO_8859_1);
        commonMapper.put("ro", Charset.forName("ISO-8859-2"));
        commonMapper.put("ru", Charset.forName("ISO-8859-5"));
        commonMapper.put("sh", Charset.forName("ISO-8859-5"));
        commonMapper.put("sk", Charset.forName("ISO-8859-2"));
        commonMapper.put("sl", Charset.forName("ISO-8859-2"));
        commonMapper.put("sq", Charset.forName("ISO-8859-2"));
        commonMapper.put("sr", Charset.forName("ISO-8859-5"));
        commonMapper.put("sv", StandardCharsets.ISO_8859_1);
        commonMapper.put("tr", Charset.forName("ISO-8859-9"));
        commonMapper.put("uk", Charset.forName("ISO-8859-5"));
        commonMapper.put("zh", Charset.forName("GB18030"));
        commonMapper.put("zh_TW", Charset.forName("Big5"));
        return commonMapper;
    }

    /**
     * Sets a locale-charset mapping.
     *
     * @param key     the key for the charset.
     * @param charset the corresponding charset.
     */
    public void setCharSet(String key, Charset charset) {
        charsetMapper.put(key, charset);
    }

    /**
     * 优先全匹配
     * lang_country
     * lang
     * else return DEFAULT
     */
    public Charset getCharSet(@Nonnull Locale locale) {
        // Check the cache first.
        String key = locale.toString();

        if (key.length() == 0) {
            return DEFAULT_CHARSET;
        }

        return getCharSet(key);
    }

    public Charset getCharSet(String localeString) {
        return charsetMapper.getOrDefault(localeString, DEFAULT_CHARSET);
    }

    protected void setCharSetIfAbsent(String key, Charset charset) {
        charsetMapper.putIfAbsent(key, charset);
    }
}
