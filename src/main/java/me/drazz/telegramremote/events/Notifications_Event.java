/**
 * -----------------------------------------------------------------------------
 * Notifications_Event.java
 * by drazz
 * -----------------------------------------------------------------------------
 * Description: This class handles player command preprocessing events and
 * sends notifications to administrators through Telegram in case of
 * unauthorized OP usage by players.
 * -----------------------------------------------------------------------------
 * Version: 1.0.0
 * Last Updated: January 19, 2024
 * -----------------------------------------------------------------------------
 */
package me.drazz.telegramremote.events;

import me.drazz.telegramremote.TelegramRemote;
import me.drazz.telegramremote.bot.Main_BOT;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class Notifications_Event implements Listener {
    private static Notifications_Event instance;
    Main_BOT telegramBot = new Main_BOT();
    private List<Long> adminChatIds;
    String enable;

    public Notifications_Event() {
        instance = this;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = TelegramRemote.getInstance().getConfig();
        adminChatIds = config.getLongList("telegram.admin_ids");
        enable = config.getString("telegram.notifications.enable_op");
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (enable != null && enable.equals("true")) {
            Player player = event.getPlayer();
            if (player.isOp()) {
                getLogger().info(TelegramRemote.getMessage("messages.telegram.op_text") + player.getName());
                for (Long adminChatId : adminChatIds) {
                    telegramBot.sendMsg(adminChatId, TelegramRemote.getMessage("messages.telegram.op_text") + player.getName());
                }
            }
        }
    }

    public static Notifications_Event getInstance() {
        return instance;
    }
}
