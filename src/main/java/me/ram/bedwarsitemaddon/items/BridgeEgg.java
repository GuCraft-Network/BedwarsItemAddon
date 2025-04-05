package me.ram.bedwarsitemaddon.items;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.LocationUtil;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;
import me.ram.bedwarsitemaddon.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BridgeEgg implements Listener {
    private final Map<Player, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        for (Player player : e.getGame().getPlayers()) {
            cooldown.remove(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!Config.items_bridge_egg_enabled) {
            return;
        }
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) {
            return;
        }
        Player player = e.getPlayer();
        ItemStack handItem = e.getItem();
        if (handItem == null || handItem.getType() != Material.EGG) {
            return;
        }
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null || game.getState() != GameState.RUNNING || !game.getPlayers().contains(player) || game.isOverSet()) {
            return;
        }
        e.setCancelled(true);

        if ((System.currentTimeMillis() - cooldown.getOrDefault(player, (long) 0)) <= Config.items_bridge_egg_cooldown * 1000) {
            e.setCancelled(true);
            player.sendMessage(Config.message_cooling.replace("{time}", String.format("%.1f", (((Config.items_bridge_egg_cooldown * 1000 - System.currentTimeMillis() + cooldown.getOrDefault(player, (long) 0)) / 1000)))));
            return;
        }

        BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.BRIDGE_EGG, handItem);
        Bukkit.getPluginManager().callEvent(bedwarsUseItemEvent);
        if (bedwarsUseItemEvent.isCancelled()) {
            return;
        }

        cooldown.put(player, System.currentTimeMillis());
        Egg egg = player.launchProjectile(Egg.class);
        egg.setBounce(false);
        egg.setShooter(player);
        this.setblock(game, egg, player);
        TakeItemUtil.TakeItem(player, handItem);
    }

    public void setblock(Game game, Egg egg, Player player) {
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (!egg.isDead()) {
                    new BukkitRunnable() {
                        final Location location = egg.getLocation().add(0, -1, 0);

                        @Override
                        public void run() {
                            if (game.isOverSet() || game.getState() != GameState.RUNNING) {
                                this.cancel();
                                return;
                            }
                            if (!Utils.isCanPlace(game, location)) {
                                this.cancel();
                                return;
                            }
                            location.setX((int) location.getX());
                            location.setY((int) location.getY());
                            location.setZ((int) location.getZ());
                            List<Location> blocklocation = new ArrayList<>();
                            blocklocation.add(location);
                            Vector vector = egg.getVelocity();
                            double x = vector.getX() > 0 ? vector.getX() : -vector.getX();
                            double y = vector.getY() > 0 ? vector.getY() : -vector.getY();
                            double z = vector.getZ() > 0 ? vector.getZ() : -vector.getZ();
                            if (y < x || y < z) {
                                blocklocation.add(LocationUtil.getLocation(location, -1, 0, -1));
                                blocklocation.add(LocationUtil.getLocation(location, -1, 0, 0));
                                blocklocation.add(LocationUtil.getLocation(location, 0, 0, -1));
                            } else {
                                blocklocation.add(LocationUtil.getLocation(location, 0, 1, 0));
                                blocklocation.add(LocationUtil.getLocation(location, -1, 1, -1));
                                blocklocation.add(LocationUtil.getLocation(location, -1, 1, 0));
                                blocklocation.add(LocationUtil.getLocation(location, 0, 1, -1));
                                blocklocation.add(LocationUtil.getLocation(location, -1, 0, -1));
                                blocklocation.add(LocationUtil.getLocation(location, -1, 0, 0));
                                blocklocation.add(LocationUtil.getLocation(location, 0, 0, -1));
                            }
                            for (Location loc : blocklocation) {
                                Block block = loc.getBlock();
                                if (block.getType() == new ItemStack(Material.AIR).getType() && !block.equals(player.getLocation().getBlock()) && !block.equals(player.getLocation().clone().add(0, 1, 0).getBlock()) && game.getRegion().isInRegion(loc) && i < Config.items_bridge_egg_maxblock) {
                                    loc.getBlock().setType(Material.WOOL);
                                    loc.getBlock().setData(game.getPlayerTeam(player).getColor().getDyeColor().getWoolData());
                                    i++;
                                    game.getRegion().addPlacedBlock(loc.getBlock(), null);
                                }
                            }
                        }
                    }.runTaskLater(Main.getInstance(), 5L);
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 0L);
    }
}
