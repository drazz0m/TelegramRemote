/**
 * -----------------------------------------------------------------------------
 * TelegramRemote.java
 * by drazz
 * -----------------------------------------------------------------------------
 * Description: Main class for the TelegramRemote plugin, enabling integration
 * of Telegram with Bukkit servers for server administration.
 * Usage: /telegramremote <command>
 * -----------------------------------------------------------------------------
 * Version: 1.0.1
 * Last Updated: January 20, 2024
 * -----------------------------------------------------------------------------
 */
package me.drazz.telegramremote;

import me.drazz.telegramremote.bot.Main_BOT;
import me.drazz.telegramremote.commands.TR_CMD;
import me.drazz.telegramremote.commands.TR_TabCompleter;
import me.drazz.telegramremote.events.Notifications_Event;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.util.Objects;

public final class TelegramRemote extends JavaPlugin {

    Main_BOT telegramBot = new Main_BOT();
    private static TelegramRemote instance;
    private static YamlConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadMessagesConfig();

        Objects.requireNonNull(getCommand("telegramremote")).setExecutor(new TR_CMD());
        Objects.requireNonNull(getCommand("telegramremote")).setTabCompleter(new TR_TabCompleter());
        Bukkit.getPluginManager().registerEvents(new Notifications_Event(), this);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Main_BOT());
            getLogger().info("Bot launched successfully!");
        } catch (Exception e) {
            getLogger().info("Bot error!");
            e.printStackTrace();
        }
        telegramBot.bot_started_notif();

        if (getConfig().getBoolean("update.enable")) {
            String currentVersion = getDescription().getVersion();
            CheckUpdate checkUpdate = new CheckUpdate(this, currentVersion);
            checkUpdate.checkForUpdateAsync();
        }

        getLogger().info("Plugin launched successfully!");
    }

    @Override
    public void onDisable() {
    }

    public void loadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages_" + getConfig().getString("language") + ".yml");

        if (!messagesFile.exists()) {
            saveResource("messages_ru.yml", false);
            saveResource("messages_en.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static String getMessage(String key) {
        if (messagesConfig == null) {
            throw new IllegalStateException("Messages configuration not loaded.");
        }

        return messagesConfig.getString(key);
    }

    public static TelegramRemote getInstance() {
        return instance;
    }

}
