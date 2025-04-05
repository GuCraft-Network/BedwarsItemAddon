package me.ram.bedwarsitemaddon.items;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.SoundMachine;
import me.ram.bedwarsitemaddon.Main;
import me.ram.bedwarsitemaddon.config.Config;
import me.ram.bedwarsitemaddon.event.BedwarsUseItemEvent;
import me.ram.bedwarsitemaddon.utils.TakeItemUtil;
import me.ram.bedwarsitemaddon.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompactTower implements Listener {

    private final Map<Player, Long> cooldown = new HashMap<>();
    private List<List<String>> blocks;

    public CompactTower() {
        blocks = new ArrayList<>();
        loadBlocks();
    }

    @EventHandler
    public void onStart(BedwarsGameStartEvent e) {
        for (Player player : e.getGame().getPlayers()) {
            cooldown.remove(player);
        }
    }

    private void loadBlocks() {
        blocks = new ArrayList<>();
        try {
            URL url = Main.getInstance().getClass().getResource("/Tower.blocks");
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferReader.readLine()) != null) {
                List<String> list = new ArrayList<>();
                for (String l : line.split(";")) {
                    try {
                        list.add(l);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                blocks.add(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!Config.items_compact_tower_enabled) {
            return;
        }
        Player player = e.getPlayer();
        ItemStack handItem = e.getItemInHand();
        if (handItem == null || handItem.getType() != Material.valueOf(Config.items_compact_tower_item)) {
            return;
        }
        Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
        if (game == null || game.getState() != GameState.RUNNING || game.isOverSet()) {
            return;
        }
        if (game.isSpectator(player) || !game.getPlayers().contains(player)) {
            return;
        }
        Team team = game.getPlayerTeam(player);
        if (team == null) {
            return;
        }

        if (!Utils.isCanPlace(game, e.getBlock().getLocation())) return;

        e.setCancelled(true);
        e.getBlock().setType(Material.AIR);


        if ((System.currentTimeMillis() - cooldown.getOrDefault(player, (long) 0)) <= Config.items_compact_tower_cooldown * 1000) {
            player.sendMessage(Config.message_cooling.replace("{time}", String.format("%.1f", (((Config.items_compact_tower_cooldown * 1000 - System.currentTimeMillis() + cooldown.getOrDefault(player, (long) 0)) / 1000)))));
            return;
        }

        BedwarsUseItemEvent bedwarsUseItemEvent = new BedwarsUseItemEvent(game, player, EnumItem.COMPACT_TOWER, handItem);
        Bukkit.getPluginManager().callEvent(bedwarsUseItemEvent);
        if (bedwarsUseItemEvent.isCancelled()) {
            return;
        }
        cooldown.put(player, System.currentTimeMillis());
        setblock(game, team, e.getBlock().getLocation(), player);
        TakeItemUtil.TakeItem(player, handItem);
    }

    public void setblock(Game game, Team team, Location location, Player player) {
        int face = getFace(player.getLocation());
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (!game.getState().equals(GameState.RUNNING) || game.isOverSet() || i >= blocks.size()) {
                    cancel();
                    return;
                }
                Location loc = null;
                for (String line : blocks.get(i)) {
                    String[] ary = line.split(",");
                    try {
                        int x = Integer.valueOf(ary[0]);
                        int y = Integer.valueOf(ary[1]);
                        int z = Integer.valueOf(ary[2]);
                        if (face == 0) {
                            loc = location.clone().add(x, y, z);
                        } else if (face == 1) {
                            loc = location.clone().add(-z, y, x);
                        } else if (face == 2) {
                            loc = location.clone().add(-x, y, -z);
                        } else if (face == 3) {
                            loc = location.clone().add(z, y, -x);
                        }
                        Block block = loc.getBlock();
                        if (loc.getBlock().getType() != Material.AIR || !Utils.isCanPlace(game, loc)) {
                            continue;
                        }
                        try {
                            Material type = Material.valueOf(ary[3]);
                            if (type == Material.WOOL) {
                                block.setType(Material.WOOL);
                                block.setData(team.getColor().getDyeColor().getWoolData());
                            } else if (type == Material.LADDER) {
                                block.setType(Material.LADDER);
                                if (face == 0) {
                                    block.setData((byte) 2);
                                } else if (face == 1) {
                                    block.setData((byte) 5);
                                } else if (face == 2) {
                                    block.setData((byte) 3);
                                } else if (face == 3) {
                                    block.setData((byte) 4);
                                }
                            } else {
                                block.setType(type);
                                try {
                                    block.setData(Byte.valueOf(ary[4]));
                                } catch (Exception ignored) {
                                }
                            }
                            game.getRegion().addPlacedBlock(block, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (loc != null) {
                    loc.getWorld().playSound(loc, SoundMachine.get("CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG"), 5, 1);
                }
                i++;
            }
        }.runTaskTimer(Main.getInstance(), 0, 3L);
    }

    private int getFace(Location location) {
        List<Integer> list = new ArrayList<>();
        for (int i = -360; i <= 360; i += 90) {
            list.add(i);
        }
        int yaw = (int) location.getYaw();
        int a = Math.abs(list.get(0) - yaw);
        int nyaw = list.get(0);
        for (int i : list) {
            int j = Math.abs(i - yaw);
            if (j < a) {
                a = j;
                nyaw = i;
            }
        }
        int face = 0;
        if (nyaw == -360 || nyaw == 0 || nyaw == 360) {
            face = 0;
        } else if (nyaw == 90 || nyaw == -270) {
            face = 1;
        } else if (nyaw == 180 || nyaw == -180) {
            face = 2;
        } else if (nyaw == 270 || nyaw == -90) {
            face = 3;
        }
        return face;
    }
}
