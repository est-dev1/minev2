package com.example.clienttracker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ClientTrackerPlugin extends JavaPlugin implements Listener, PluginMessageListener {

    private final Map<UUID, String> clientBrands = new HashMap<>();
    private final Map<UUID, String> packStatuses = new HashMap<>();

    private static final String ADMIN_PERMISSION = "clienttracker.notify";

    private static final Map<String, String> BRAND_MAP = new HashMap<>();

    static {
        BRAND_MAP.put("lunarclient",  "Lunar Client");
        BRAND_MAP.put("fabric",       "Fabric Launcher");
        BRAND_MAP.put("vanilla",      "Minecraft Launcher (Vanilla)");
        BRAND_MAP.put("forge",        "Forge");
        BRAND_MAP.put("fml",          "Forge (FML)");
    }

    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ClientTracker enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this, "minecraft:brand");
        clientBrands.clear();
        packStatuses.clear();
        getLogger().info("ClientTracker disabled.");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("minecraft:brand")) return;

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            int length = readVarInt(in);
            byte[] strBytes = new byte[length];
            in.readFully(strBytes);
            String rawBrand = new String(strBytes, StandardCharsets.UTF_8).toLowerCase();

            String friendlyName = resolveBrand(rawBrand);
            clientBrands.put(player.getUniqueId(), friendlyName);

            String msg = ChatColor.GOLD + "[ClientTracker] " + ChatColor.YELLOW
                    + player.getName() + ChatColor.WHITE + " is using "
                    + ChatColor.AQUA + friendlyName
                    + ChatColor.GRAY + " (raw: " + rawBrand + ")";

            notifyAdmins(msg);
            getLogger().info(player.getName() + " brand detected: " + friendlyName + " (raw: " + rawBrand + ")");

        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to read brand for " + player.getName(), e);
        }
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        String friendlyStatus = switch (status) {
            case SUCCESSFULLY_LOADED -> ChatColor.GREEN + "✔ Accepted & Loaded";
            case DECLINED           -> ChatColor.RED    + "✗ Declined";
            case FAILED_DOWNLOAD    -> ChatColor.RED    + "✗ Failed to Download";
            case ACCEPTED           -> ChatColor.YELLOW + "⌛ Downloading…";
            default                 -> ChatColor.GRAY   + status.name();
        };

        packStatuses.put(player.getUniqueId(), status.name());

        String msg = ChatColor.GOLD + "[ClientTracker] " + ChatColor.YELLOW
                + player.getName() + ChatColor.WHITE + " resource pack status: "
                + friendlyStatus;

        notifyAdmins(msg);
        getLogger().info(player.getName() + " resource pack status: " + status.name());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        clientBrands.remove(event.getPlayer().getUniqueId());
        packStatuses.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clientBrands.remove(event.getPlayer().getUniqueId());
        packStatuses.remove(event.getPlayer().getUniqueId());
    }

    private String resolveBrand(String rawBrand) {
        for (Map.Entry<String, String> entry : BRAND_MAP.entrySet()) {
            if (rawBrand.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return rawBrand.substring(0, 1).toUpperCase() + rawBrand.substring(1);
    }

    private void notifyAdmins(String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(ADMIN_PERMISSION)) {
                online.sendMessage(message);
            }
        }
        getLogger().info(ChatColor.stripColor(message));
    }

    private int readVarInt(DataInputStream in) throws Exception {
        int value = 0, position = 0;
        byte currentByte;
        do {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << position;
            position += 7;
            if (position >= 32) throw new RuntimeException("VarInt too big");
        } while ((currentByte & 0x80) != 0);
        return value;
    }
}
