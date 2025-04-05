package me.ram.bedwarsitemaddon.event;

import io.github.bedwarsrel.game.Game;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsFishSpawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Game game;
    private final Location location;
    private Boolean cancelled = false;

    public BedwarsFishSpawnEvent(Game game, Location location) {
        this.game = game;
        this.location = location;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Game getGame() {
        return game;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
