package me.ram.bedwarsitemaddon.items;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class FireBall implements Listener {

    private final Map<Player, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        for (Player player : e.getGame().getPlayers()) {
            cooldown.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractFireball(PlayerInteractEvent e) {
        if (!Config.items_fireball_enabled) {
            return;
        }
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) {
            return;
        }
        ItemStack handItem = e.getItem();
        if (handItem == null || e.getItem().getType() != Material.FIREBALL) {
            return;
        }
        Player player = e.getPlayer();
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null || game.getState() != GameState.RUNNING || !game.getPlayers().contains(player)) {
            return;
        }
        e.setCancelled(true);
        if ((System.currentTimeMillis() - cooldown.getOrDefault(player, (long) 0)) <= Config.items_fireball_cooldown * 1000) {
            player.sendMessage(Config.message_cooling.replace("{time}", String.format("%.1f", (((Config.items_fireball_cooldown * 1000 - System.currentTimeMillis() + cooldown.getOrDefault(player, (long) 0)) / 1000)))));
            return;
        }
        BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.FIRE_BALL, handItem);
        Bukkit.getPluginManager().callEvent(bedwarsUseItemEvent);
        if (bedwarsUseItemEvent.isCancelled()) {
            return;
        }
        cooldown.put(player, System.currentTimeMillis());
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setVelocity(fireball.getDirection().multiply(Config.items_fireball_ejection_speed));
        fireball.setYield((float) Config.items_fireball_range);
        fireball.setBounce(false);
        fireball.setShooter(player);
        fireball.setMetadata("FireBall", new FixedMetadataValue(Main.getInstance(), game.getName() + "." + player.getName()));
        TakeItemUtil.TakeItem(player, handItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockIgnite(BlockIgniteEvent e) {
        Entity entity = e.getIgnitingEntity();
        if (entity instanceof Fireball && entity.hasMetadata("FireBall")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent e) {
        if (!Config.items_fireball_enabled) {
            return;
        }
        Entity entity = e.getEntity();
        Entity damager = e.getDamager();
        if (!damager.hasMetadata("FireBall")) {
            return;
        }
        if (!(entity instanceof Player && damager instanceof Fireball)) {
            return;
        }
        Player player = (Player) entity;
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null) {
            return;
        }
        if (game.getState() != GameState.RUNNING) {
            return;
        }
        e.setDamage(Config.items_fireball_damage);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (!Config.items_fireball_enabled) {
            return;
        }
        Entity entity = e.getEntity();
        if (!(entity instanceof Fireball) || !entity.hasMetadata("FireBall")) {
            return;
        }
        Fireball fireball = (Fireball) e.getEntity();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
            if (game == null || game.getState() != GameState.RUNNING) return;
            if (game.isSpectator(player) || player.getGameMode() != GameMode.SURVIVAL) return;
            if (e.getEntity().getWorld() != player.getWorld()) return;
            if (!(player.getLocation().distanceSquared((e.getEntity().getLocation())) <= Math.pow((fireball.getYield() + 1), 2)))
                return;

            if (fireball.getShooter() != null && ((Player) fireball.getShooter()).getUniqueId().equals(player.getUniqueId())) {
                player.damage(Config.items_fireball_damage, fireball);
            }
            if (Config.items_fireball_ejection_enabled) {
                Location location = e.getEntity().getLocation();
                Vector vector = location.toVector();
                Vector playerVector = player.getLocation().toVector();
                Vector normalizedVector = vector.subtract(playerVector).normalize();
                Vector horizontalVector = normalizedVector.multiply(Config.items_fireball_ejection_knockback_horizontal);
                double y = Config.items_fireball_ejection_knockback_vertical * 1.5;
                player.setVelocity(horizontalVector.setY(y));
                if (Config.items_fireball_ejection_no_fall) {
                    Main.getInstance().getNoFallManage().addPlayer(player);
                }
            }
        }
    }

//   尝试用其他事件处理击退, 结果跟EntityExplodeEvent差不多.
//    @EventHandler
//    public void onFireBallHit(ProjectileHitEvent e) {
//        if (!Config.items_fireball_enabled) {
//            return;
//        }
//        Entity entity = e.getEntity();
//        if (!(entity instanceof Fireball) || !entity.hasMetadata("FireBall")) {
//            return;
//        }
//        Fireball fireball = (Fireball) e.getEntity();
//        for (Player player : Bukkit.getOnlinePlayers()) {
//            Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
//            if (game == null || game.getState() != GameState.RUNNING) return;
//            if (game.isSpectator(player) || player.getGameMode() != GameMode.SURVIVAL) return;
//            if (e.getEntity().getWorld() != player.getWorld()) return;
//            if (!(player.getLocation().distanceSquared((e.getEntity().getLocation())) <= Math.pow((fireball.getYield() + 1), 2)))
//                return;
//            if (Config.items_fireball_ejection_enabled) {
//                player.setVelocity(LocationUtil.getPosition(player.getLocation(), fireball.getLocation(), 3).multiply(Config.items_fireball_ejection_velocity));
//                if (Config.items_fireball_ejection_no_fall) {
//                    Main.getInstance().getNoFallManage().addPlayer(player);
//                }
//            }
//        }
//    }
}
