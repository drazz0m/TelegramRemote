/**
 * -----------------------------------------------------------------------------
 * CheckUpdate.java
 * by drazz
 * -----------------------------------------------------------------------------
 * Description: This class handles checking for updates for the TelegramRemote
 * plugin by querying information from the SpigotMC API and notifying
 * administrators in case of a new version.
 * -----------------------------------------------------------------------------
 * Version: 1.0.1
 * Last Updated: January 19, 2024
 * -----------------------------------------------------------------------------
 */
package me.drazz.telegramremote;

import me.drazz.telegramremote.bot.Main_BOT;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class CheckUpdate {
    private static CheckUpdate instance;
    Main_BOT telegramBot = new Main_BOT();
    private JavaPlugin plugin;
    private String currentVersion;
    private List<Long> adminChatIds;
    String enable_check_upd;

    public CheckUpdate(JavaPlugin plugin, String currentVersion) {
        instance = this;
        loadConfig();

        this.plugin = plugin;
        this.currentVersion = currentVersion;
    }

    public void checkForUpdateAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForUpdate();
            }
        }.runTaskAsynchronously(plugin);
    }

    public void loadConfig() {
        FileConfiguration config = TelegramRemote.getInstance().getConfig();
        adminChatIds = config.getLongList("telegram.admin_ids");
        enable_check_upd = config.getString("telegram.notifications.enable_update");
    }

    private void checkForUpdate() {
        try {
            int idPlugin = 114605;
            URL url = new URL("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=" + idPlugin);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());

            JSONTokener tokener = new JSONTokener(reader);

            Object obj = tokener.nextValue();
            if (!(obj instanceof JSONObject)) {
                throw new JSONException("Expected JSON object, but received something else.");
            }

            JSONObject resource = (JSONObject) obj;

            String latestVersion = resource.getString("current_version");

            if (!currentVersion.equals(latestVersion)) {
                plugin.getLogger().info("New version of the plugin is available! Current version: " + currentVersion + ", A new version: " + latestVersion);
                if (enable_check_upd != null && enable_check_upd.equals("true")) {
                        for (Long adminChatId : adminChatIds) {
                            telegramBot.sendMsg(adminChatId, TelegramRemote.getMessage("messages.update.upd_text") + currentVersion + TelegramRemote.getMessage("messages.update.upd_text_two") + latestVersion);
                        }
                    }
            }
            else {
                plugin.getLogger().info("Current version plugin.");
            }

            reader.close();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to check for updates for the plugin.");
            e.printStackTrace();
        } catch (JSONException e) {
            plugin.getLogger().warning("Err JSON");
            e.printStackTrace();
        }
    }

    public static CheckUpdate getInstance() {
        return instance;
    }
}
