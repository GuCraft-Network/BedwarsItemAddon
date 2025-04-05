package me.ram.bedwarsitemaddon;

import lombok.Getter;
import me.ram.bedwarsitemaddon.command.CommandTabCompleter;
import me.ram.bedwarsitemaddon.command.Commands;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.config.LocaleConfig;
import me.ram.bedwarsitemaddon.items.*;
import me.ram.bedwarsitemaddon.listener.EventListener;
import me.ram.bedwarsitemaddon.manage.NoFallManage;
import org.bstats.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Ram
 * @version 1.7.0
 */
public class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    @Getter
    private NoFallManage noFallManage;
    @Getter
    private LocaleConfig localeConfig;

    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public FileConfiguration getConfig() {
        FileConfiguration config = Config.getConfig();
        return config == null ? super.getConfig() : config;
    }

    public void onEnable() {
        // 实在懒得改版本号 ==
//        if (!getDescription().getName().equals("BedwarsItemAddon") || !getDescription().getVersion().equals(getVersion()) || !getDescription().getAuthors().contains("Ram")) {
//            try {
//                new Exception("Please don't edit plugin.yml!").printStackTrace();
//            } catch (Exception ignored) {
//            }
//            Bukkit.getPluginManager().disablePlugin(this);
//            return;
//        }
        instance = this;
        noFallManage = new NoFallManage();
        localeConfig = new LocaleConfig();
        getLocaleConfig().loadLocaleConfig();
        Bukkit.getConsoleSender().sendMessage("§f========================================");
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage("            §bBedwarsItemAddon");
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage(" §f" + getLocaleConfig().getLanguage("version") + ": §a" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage(" §f" + getLocaleConfig().getLanguage("author") + ": §aRam");
        Bukkit.getConsoleSender().sendMessage("§7");
        Bukkit.getConsoleSender().sendMessage("§f========================================");
        Config.loadConfig();
        Bukkit.getPluginCommand("bedwarsitemaddon").setExecutor(new Commands());
        Bukkit.getPluginCommand("bedwarsitemaddon").setTabCompleter(new CommandTabCompleter());
        if (Bukkit.getPluginManager().isPluginEnabled("BedwarsRel")) {
            registerEvents();
        } else {
            Bukkit.getPluginManager().disablePlugin(this);
        }
        try {
            new Metrics(this).addCustomChart(new Metrics.SimplePie("language", () -> localeConfig.getPluginLocale().getName()));
        } catch (Exception ignored) {
        }
    }

    //不要允许飞行了 反作弊被绕的时候我都畏惧了
//    @Override
//    public void onLoad() {
//        try {
//            // 允许飞行，防止使用道具时踢出服务器
//            Path path = Paths.get(getDataFolder().getParentFile().getAbsolutePath()).getParent().resolve("server.properties");
//            boolean reboot = false;
//            List<String> lines = Files.readAllLines(path);
//            if (lines.contains("allow-flight=false")) {
//                lines.remove("allow-flight=false");
//                lines.add("allow-flight=true");
//                reboot = true;
//            }
//            Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
//            if (reboot) {
//                Bukkit.shutdown();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
//        Bukkit.getPluginManager().registerEvents(new UpdateCheck(), this); // 服务器倒闭了 用不了更新了
        Bukkit.getPluginManager().registerEvents(new FireBall(), this);
        Bukkit.getPluginManager().registerEvents(new LightTNT(), this);
        Bukkit.getPluginManager().registerEvents(new BridgeEgg(), this);
        Bukkit.getPluginManager().registerEvents(new Parachute(), this);
        Bukkit.getPluginManager().registerEvents(new TNTLaunch(), this);
        Bukkit.getPluginManager().registerEvents(new MagicMilk(), this);
        Bukkit.getPluginManager().registerEvents(new Trampoline(), this);
        Bukkit.getPluginManager().registerEvents(new CompactTower(), this);
        Bukkit.getPluginManager().registerEvents(new WalkPlatform(), this);
        Bukkit.getPluginManager().registerEvents(new TeamIronGolem(), this);
        Bukkit.getPluginManager().registerEvents(new TeamSilverFish(), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionProof(), this);
        Bukkit.getPluginManager().registerEvents(new EnderPearlChair(), this);
    }
}
