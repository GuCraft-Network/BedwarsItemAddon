package me.ram.bedwarsitemaddon.command;

import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§f===========================================================");
            sender.sendMessage("");
            sender.sendMessage("§b                     BedwarsItemAddon");
            sender.sendMessage("");
            sender.sendMessage("§f  " + Main.getInstance().getLocaleConfig().getLanguage("version") + ": §a" + Main.getInstance().getVersion());
            sender.sendMessage("");
            sender.sendMessage("§f  " + Main.getInstance().getLocaleConfig().getLanguage("author") + ": §aRam §7| §fModifed By LinMoyu_.");
            sender.sendMessage("");
            sender.sendMessage("§f===========================================================");
            return true;
        }
        String arg = args[0].toLowerCase();
        if (arg.equals("help")) {
            sender.sendMessage("§f=====================================================");
            sender.sendMessage("");
            sender.sendMessage("§b§l BedwarsItemAddon §fv" + Main.getInstance().getVersion() + "  §7by Ram");
            sender.sendMessage("");
            Config.getLanguageList("commands.help").forEach(sender::sendMessage);
            sender.sendMessage("");
            sender.sendMessage("§f=====================================================");
            return true;
        }
        if (arg.equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bedwarsitemaddon.reload")) {
                sender.sendMessage(Config.getLanguage("commands.message.prefix") + Config.getLanguage("commands.message.no_permission"));
                return true;
            }
            Config.loadConfig();
            sender.sendMessage(Config.getLanguage("commands.message.prefix") + Config.getLanguage("commands.message.reloaded"));
            return true;
        }
        return true;
    }
}
