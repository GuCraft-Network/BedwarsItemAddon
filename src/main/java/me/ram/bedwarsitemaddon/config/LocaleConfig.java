package me.ram.bedwarsitemaddon.config;

import lombok.Getter;
import me.ram.bedwarsitemaddon.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocaleConfig {

    private final Map<String, Object> language;
    @Getter
    private EnumLocale pluginLocale;

    public LocaleConfig() {
        language = new HashMap<String, Object>();
    }

    private static void saveLocale() {
        File folder = new File(Main.getInstance().getDataFolder(), "/locale");
        if (!folder.exists()) {
            folder.mkdirs();
            for (EnumLocale locale : EnumLocale.values()) {
                File locale_folder = new File(folder.getPath(), "/" + locale.getName());
                if (!locale_folder.exists()) {
                    locale_folder.mkdirs();
                }
                for (String file : new String[]{"config.yml"}) {
                    try {
                        writeToLocal(folder.getPath() + "/" + locale.getName() + "/" + file, Main.getInstance().getResource("locale/" + locale.getName() + "/" + file));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void writeToLocal(String destination, InputStream input) throws IOException {
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        input.close();
    }

    private void loadLanguage() {
        switch (pluginLocale) {
            case ZH_CN:
                language.put("version", "版本");
                language.put("author", "作者");
                language.put("running_version", "当前版本");
                break;
            case EN_US:
                language.put("version", "Version");
                language.put("author", "Author");
                language.put("running_version", "Running version");
                break;
            default:
                break;
        }
    }

    public void loadLocaleConfig() {
        File folder = new File(Main.getInstance().getDataFolder(), "/");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder.getAbsolutePath() + "/config.yml");
        if (file.exists()) {
            pluginLocale = getLocaleByName(YamlConfiguration.loadConfiguration(file).getString("locale", "en_US"));
        } else {
            pluginLocale = getSystemLocale();
        }
        loadLanguage();
        saveLocale();
    }

    public Object getLanguage(String str) {
        return language.getOrDefault(str, "null");
    }

    public String getSystemLocaleName() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    public EnumLocale getSystemLocale() {
        return getLocaleByName(getSystemLocaleName());
    }

    private EnumLocale getLocaleByName(String name) {
        EnumLocale locale = EnumLocale.getByName(name);
        return locale == null ? EnumLocale.EN_US : locale;
    }

    public void saveResource(String resourcePath) {
        try {
            writeToLocal(Main.getInstance().getDataFolder().getPath() + "/" + resourcePath, Main.getInstance().getResource("locale/" + getPluginLocale().getName() + "/" + resourcePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
