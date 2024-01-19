/**
 * -----------------------------------------------------------------------------
 * Main_BOT.java
 * by drazz
 * -----------------------------------------------------------------------------
 * Description: This class implements a Telegram bot using the TelegramBots API
 * for integration with a Bukkit server. It handles various commands and sends
 * notifications to administrators.
 * -----------------------------------------------------------------------------
 * Version: 1.0.0
 * Last Updated: January 20, 2024
 * -----------------------------------------------------------------------------
 */
package me.drazz.telegramremote.bot;

import com.github.t9t.minecraftrconclient.RconClient;
import me.drazz.telegramremote.TelegramRemote;
import me.drazz.telegramremote.events.Notifications_Event;
import org.bukkit.configuration.file.FileConfiguration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;



import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class Main_BOT extends TelegramLongPollingBot {

    private static final Map<Long, String> fsmContext = new HashMap<>();


    @Override
    public void onUpdateReceived(Update update) {


        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String currentState = fsmContext.getOrDefault(chatId, "DEFAULT");

            if (messageText.equalsIgnoreCase("/start")){
                handleStartCommand(chatId);
            }
            else if (messageText.equals(TelegramRemote.getMessage("messages.telegram.admin_menu.menu_admin"))) {
                sendAdminPanel(chatId);
            }
            else if (messageText.equals(TelegramRemote.getMessage("messages.telegram.admin_menu.console_menu"))) {
                handleConsoleCommand(chatId);
            }
            else if (messageText.equals("/quit")) {
                if (currentState.equals("CONSOLE_MODE")) {
                    sendMenu(chatId);
                    fsmContext.put(chatId, "DEFAULT");
                }
            }
            else if (currentState.equals("CONSOLE_MODE")) {
                if (!isAdmin(chatId)) {
                    getLogger().info("[TG-Console] Received from Telegram: " + messageText + ", " + "AdminID: " + chatId);
                    rcon(messageText, chatId);
                }
            }
            else if (currentState.equals("ADMIN_ADD")) {
                if (isAdmin(chatId)) {
                    return;
                }


                try {
                    Long adminId = Long.parseLong(messageText);
                    fsmContext.put(chatId, "DEFAULT");
                    getLogger().info("[TG REMOTE] Added admin from telegram. " + "Added admin: " + adminId + " " + "AdminID: " + chatId);
                    FileConfiguration config = TelegramRemote.getInstance().getConfig();

                    List<Long> adminIds = config.getLongList("telegram.admin_ids");
                    adminIds.add(adminId);

                    config.set("telegram.admin_ids", adminIds);
                    TelegramRemote.getInstance().saveConfig();

                    TelegramRemote.getInstance().reloadConfig();
                    TelegramRemote.getInstance().loadMessagesConfig();
                    Notifications_Event.getInstance().loadConfig();

                    sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.admin_added_text"));
                } catch (NumberFormatException e) {
                    sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.incorrect_id"));
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = update.getCallbackQuery().getData();
            User user = callbackQuery.getFrom();
            long chatId = user.getId();
            String currentState = fsmContext.getOrDefault(chatId, "DEFAULT");

            if ("bot_settings".equals(data) && currentState.equals("ADMIN_MENU")) {
                sendBotSettings(chatId);
            }
            else if ("enable_console_button".equals(data) && currentState.equals("BOT_SETTINGS")) {
                changeConsoleEnable(chatId);
            }
            else if ("check_update_button".equals(data) && currentState.equals("BOT_SETTINGS")) {
                changeCheckUpdate(chatId);
            }
            else if ("language_button".equals(data) && currentState.equals("BOT_SETTINGS")) {
                changeLanguage(chatId);
            }
            else if ("notifications_button".equals(data) && currentState.equals("BOT_SETTINGS")) {
                sendNotifMenu(chatId);
            }
            else if ("notification_op".equals(data) || "notification_started".equals(data) || "notification_update".equals(data) && currentState.equals("NOTIFICATIONS_MENU")) {
                changeNotif(chatId, data);
            }
            else if ("admin_add_button".equals(data) && currentState.equals("BOT_SETTINGS")) {
                fsmContext.put(chatId, "ADMIN_ADD");
                sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.chat_id_text"));
            }
            else if ("reload_plugin".equals(data)) {
                sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.admin_menu.reloading_plugin"));
                getLogger().info("Reload plugin from Telegram. By " + chatId);
                rcon("telegramremote reload", chatId);
                sendMenu(chatId);
            }
            else if ("reload".equals(data) && currentState.equals("ADMIN_MENU")) {
                sendConfirmReload(chatId);
            }
            else if ("restart".equals(data) && currentState.equals("ADMIN_MENU")) {
                sendConfirmRestart(chatId);
            }
            else if ("shutdown".equals(data) && currentState.equals("ADMIN_MENU")) {
                sendConfirmShutdown(chatId);
            }
            else if ("confirm_yes_reload".equals(data) && currentState.equals("CONFIRM_RELOAD")) {
                sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.reload"));
                getLogger().info("Reload from Telegram. By " + chatId);
                rcon("reload", chatId);
            }
            else if ("confirm_yes_restart".equals(data) && currentState.equals("CONFIRM_RESTART")) {
                sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.restart"));
                getLogger().info("Restart from Telegram. By " + chatId);
                rcon("restart", chatId);
            }
            else if ("confirm_yes_shutdown".equals(data) && currentState.equals("CONFIRM_SHUTDOWN")) {
                getLogger().info("Shutdown from Telegram. By " + chatId);
                sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.shutdown"));
                rcon("stop", chatId);
            }
            else if ("confirm_no_reload".equals(data) || "confirm_no_restart".equals(data) || "confirm_no_shutdown".equals(data) && currentState.equals("CONFIRM_RELOAD") || currentState.equals("CONFIRM_RESTART") || currentState.equals("CONFIRM_SHUTDOWN")) {
                sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.normal_mode"));
                sendAdminPanel(chatId);
            }

        }

    }

    @Override
    public String getBotUsername() {
        return "@" + TelegramRemote.getInstance().getConfig().getString("telegram.bot_username");
    }

    @Override
    public String getBotToken() {
        return TelegramRemote.getInstance().getConfig().getString("telegram.token");
    }

    private void handleStartCommand(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }
        fsmContext.put(chatId, "DEFAULT");

        ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
        replyMarkup.setSelective(true);
        replyMarkup.setResizeKeyboard(true);
        replyMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(TelegramRemote.getMessage("messages.telegram.admin_menu.menu_admin")));
        keyboard.add(row);

        row = new KeyboardRow();
        row.add(new KeyboardButton(TelegramRemote.getMessage("messages.telegram.admin_menu.console_menu")));
        keyboard.add(row);

        replyMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(TelegramRemote.getMessage("messages.telegram.start"));
        message.setReplyMarkup(replyMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            getLogger().info("Message not delivered! ChatID: " + chatId + " " + e.getMessage());
        }
    }


    private void sendBotSettings(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }
        TelegramRemote.getInstance().getConfig().getString("");
        fsmContext.put(chatId, "BOT_SETTINGS");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton languageButton = new InlineKeyboardButton();
        languageButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.language_button") + TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.lang"));
        languageButton.setCallbackData("language_button");
        row1.add(languageButton);
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton notificationsButton = new InlineKeyboardButton();
        notificationsButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.notifications_button"));
        notificationsButton.setCallbackData("notifications_button");
        row2.add(notificationsButton);
        rows.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton enableConsoleButton = new InlineKeyboardButton();
        if (TelegramRemote.getInstance().getConfig().getBoolean("telegram.rcon.enable")) {
            enableConsoleButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.enable_console_button") + "✅");
        } else {
            enableConsoleButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.enable_console_button") + "❌");
        }
        enableConsoleButton.setCallbackData("enable_console_button");
        row3.add(enableConsoleButton);
        rows.add(row3);

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton adminAddButton = new InlineKeyboardButton();
        adminAddButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.admin_add_button"));
        adminAddButton.setCallbackData("admin_add_button");
        row4.add(adminAddButton);
        rows.add(row4);

        List<InlineKeyboardButton> row5 = new ArrayList<>();
        InlineKeyboardButton checkUpdateButton = new InlineKeyboardButton();
        if (TelegramRemote.getInstance().getConfig().getBoolean("update.enable")) {
            checkUpdateButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.check_update_button") + "✅");
        }
        else {
            checkUpdateButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.check_update_button") + "❌");
        }
        checkUpdateButton.setCallbackData("check_update_button");
        row5.add(checkUpdateButton);
        rows.add(row5);

        inlineKeyboardMarkup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.settings_text"));
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
    private void changeConsoleEnable(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }

        if (TelegramRemote.getInstance().getConfig().getBoolean("telegram.rcon.enable")) {
            TelegramRemote.getInstance().getConfig().set("telegram.rcon.enable", false);
            TelegramRemote.getInstance().saveConfig();

            TelegramRemote.getInstance().reloadConfig();
            TelegramRemote.getInstance().loadMessagesConfig();
            Notifications_Event.getInstance().loadConfig();
        }
        else if (!TelegramRemote.getInstance().getConfig().getBoolean("telegram.rcon.enable")) {
            TelegramRemote.getInstance().getConfig().set("telegram.rcon.enable", true);
            TelegramRemote.getInstance().saveConfig();

            TelegramRemote.getInstance().reloadConfig();
            TelegramRemote.getInstance().loadMessagesConfig();
            Notifications_Event.getInstance().loadConfig();
        }
        else {
            sendMsg(chatId, "Check the config is correct.");
        }

        sendBotSettings(chatId);
    }

    private void changeLanguage(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }

        if (Objects.equals(TelegramRemote.getInstance().getConfig().getString("language"), "en")) {
            TelegramRemote.getInstance().getConfig().set("language", "ru");
            TelegramRemote.getInstance().saveConfig();

            TelegramRemote.getInstance().reloadConfig();
            TelegramRemote.getInstance().loadMessagesConfig();
            Notifications_Event.getInstance().loadConfig();
        }
        else if (Objects.equals(TelegramRemote.getInstance().getConfig().getString("language"), "ru")) {
            TelegramRemote.getInstance().getConfig().set("language", "en");
            TelegramRemote.getInstance().saveConfig();

            TelegramRemote.getInstance().reloadConfig();
            TelegramRemote.getInstance().loadMessagesConfig();
            Notifications_Event.getInstance().loadConfig();
        }
        else {
            sendMsg(chatId, "Check the config is correct.");
        }

        sendMenu(chatId);
    }

    private void changeCheckUpdate(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }

        if (TelegramRemote.getInstance().getConfig().getBoolean("update.enable")) {
            TelegramRemote.getInstance().getConfig().set("update.enable", false);
            TelegramRemote.getInstance().saveConfig();

            TelegramRemote.getInstance().reloadConfig();
            TelegramRemote.getInstance().loadMessagesConfig();
            Notifications_Event.getInstance().loadConfig();
        }
        else if (!TelegramRemote.getInstance().getConfig().getBoolean("update.enable")) {
            TelegramRemote.getInstance().getConfig().set("update.enable", true);
            TelegramRemote.getInstance().saveConfig();

            TelegramRemote.getInstance().reloadConfig();
            TelegramRemote.getInstance().loadMessagesConfig();
            Notifications_Event.getInstance().loadConfig();
        }
        else {
            sendMsg(chatId, "Check the config is correct.");
        }

        sendBotSettings(chatId);
    }

    private void sendAdminPanel(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }
        fsmContext.put(chatId, "ADMIN_MENU");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton bot_settingsButton = new InlineKeyboardButton();
        bot_settingsButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings"));
        bot_settingsButton.setCallbackData("bot_settings");
        row1.add(bot_settingsButton);
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton reload_pluginButton = new InlineKeyboardButton();
        reload_pluginButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.reload_plugin"));
        reload_pluginButton.setCallbackData("reload_plugin");
        row2.add(reload_pluginButton);
        rows.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton reloadButton = new InlineKeyboardButton();
        reloadButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.reload"));
        reloadButton.setCallbackData("reload");
        row3.add(reloadButton);
        rows.add(row3);

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton restartButton = new InlineKeyboardButton();
        restartButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.restart"));
        restartButton.setCallbackData("restart");
        row4.add(restartButton);
        rows.add(row4);

        List<InlineKeyboardButton> row5 = new ArrayList<>();
        InlineKeyboardButton shutdownButton = new InlineKeyboardButton();
        shutdownButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.shutdown"));
        shutdownButton.setCallbackData("shutdown");
        row5.add(shutdownButton);
        rows.add(row5);

        inlineKeyboardMarkup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.main_menu"));
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendConfirmReload(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }
        fsmContext.put(chatId, "CONFIRM_RELOAD");

        InlineKeyboardMarkup inlineKeyboardMarkupReload = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.yes_text"));
        yesButton.setCallbackData("confirm_yes_reload");

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.no_text"));
        noButton.setCallbackData("confirm_no_reload");

        row1.add(yesButton);
        row1.add(noButton);
        rows.add(row1);

        inlineKeyboardMarkupReload.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.confirm_text"));
        message.setReplyMarkup(inlineKeyboardMarkupReload);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendConfirmRestart(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }
        fsmContext.put(chatId, "CONFIRM_RESTART");

        InlineKeyboardMarkup inlineKeyboardMarkupRestart = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.yes_text"));
        yesButton.setCallbackData("confirm_yes_restart");

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.no_text"));
        noButton.setCallbackData("confirm_no_restart");

        row1.add(yesButton);
        row1.add(noButton);
        rows.add(row1);

        inlineKeyboardMarkupRestart.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.confirm_text"));
        message.setReplyMarkup(inlineKeyboardMarkupRestart);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendConfirmShutdown(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }

        fsmContext.put(chatId, "CONFIRM_SHUTDOWN");

        InlineKeyboardMarkup inlineKeyboardMarkupShutdown = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.yes_text"));
        yesButton.setCallbackData("confirm_yes_shutdown");

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.no_text"));
        noButton.setCallbackData("confirm_no_shutdown");

        row1.add(yesButton);
        row1.add(noButton);
        rows.add(row1);

        inlineKeyboardMarkupShutdown.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.confirm.confirm_text"));
        message.setReplyMarkup(inlineKeyboardMarkupShutdown);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void handleConsoleCommand(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }

        FileConfiguration config = TelegramRemote.getInstance().getConfig();
        if (config.getBoolean("telegram.rcon.enable")) {
            fsmContext.put(chatId, "CONSOLE_MODE");

            ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
            replyMarkup.setSelective(true);
            replyMarkup.setResizeKeyboard(true);
            replyMarkup.setOneTimeKeyboard(false);

            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton("/quit"));
            keyboard.add(row);

            replyMarkup.setKeyboard(keyboard);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(TelegramRemote.getMessage("messages.telegram.console"));
            message.setReplyMarkup(replyMarkup);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                getLogger().info("Message not delivered! ChatID: " + chatId + " " + e.getMessage());
            }
        }
        else if (!config.getBoolean("telegram.rcon.enable")){
            fsmContext.put(chatId, "DEFAULT");
            sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.console_disabled"));
        }
    }

    private void sendNotifMenu(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }
        fsmContext.put(chatId, "NOTIFICATIONS_MENU");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton notificationOpButton = new InlineKeyboardButton();
        if (TelegramRemote.getInstance().getConfig().getBoolean("telegram.notifications.enable_op")) {
            notificationOpButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.notification_op") + "✅");
        } else {
            notificationOpButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.notification_op") + "❌");
        }
        notificationOpButton.setCallbackData("notification_op");
        row1.add(notificationOpButton);
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton notificationStartedButton = new InlineKeyboardButton();
        if (TelegramRemote.getInstance().getConfig().getBoolean("telegram.notifications.enable_bot_started")) {
            notificationStartedButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.notification_bot_started") + "✅");
        } else {
            notificationStartedButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.notification_bot_started") + "❌");
        }
        notificationStartedButton.setCallbackData("notification_started");
        row2.add(notificationStartedButton);
        rows.add(row2);


        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton notificationUpdateButton = new InlineKeyboardButton();
        if (TelegramRemote.getInstance().getConfig().getBoolean("telegram.notifications.enable_update")) {
            notificationUpdateButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.notification_update") + "✅");
        } else {
            notificationUpdateButton.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.notification_update") + "❌");
        }
        notificationUpdateButton.setCallbackData("notification_update");
        row3.add(notificationUpdateButton);
        rows.add(row3);

        inlineKeyboardMarkup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(TelegramRemote.getMessage("messages.telegram.admin_menu.settings_bot.notifications_message"));
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void changeNotif(long chatId, String notif) {
        if (isAdmin(chatId)) {
            return;
        }
        switch (notif) {
            case "notification_op":
                if (TelegramRemote.getInstance().getConfig().getBoolean("telegram.notifications.enable_op")) {
                    TelegramRemote.getInstance().getConfig().set("telegram.notifications.enable_op", false);
                    TelegramRemote.getInstance().saveConfig();

                    TelegramRemote.getInstance().reloadConfig();
                    TelegramRemote.getInstance().loadMessagesConfig();
                    Notifications_Event.getInstance().loadConfig();
                } else if (!TelegramRemote.getInstance().getConfig().getBoolean("telegram.notifications.enable_op")) {
                    TelegramRemote.getInstance().getConfig().set("telegram.notifications.enable_op", true);
                    TelegramRemote.getInstance().saveConfig();

                    TelegramRemote.getInstance().reloadConfig();
                    TelegramRemote.getInstance().loadMessagesConfig();
                    Notifications_Event.getInstance().loadConfig();
                } else {
                    sendMsg(chatId, "Check the config is correct.");
                }

                sendNotifMenu(chatId);

                break;
            case "notification_started":
                if (TelegramRemote.getInstance().getConfig().getBoolean("telegram.notifications.enable_bot_started")) {
                    TelegramRemote.getInstance().getConfig().set("telegram.notifications.enable_bot_started", false);
                    TelegramRemote.getInstance().saveConfig();

                    TelegramRemote.getInstance().reloadConfig();
                    TelegramRemote.getInstance().loadMessagesConfig();
                    Notifications_Event.getInstance().loadConfig();
                } else if (!TelegramRemote.getInstance().getConfig().getBoolean("telegram.notifications.enable_bot_started")) {
                    TelegramRemote.getInstance().getConfig().set("telegram.notifications.enable_bot_started", true);
                    TelegramRemote.getInstance().saveConfig();

                    TelegramRemote.getInstance().reloadConfig();
                    TelegramRemote.getInstance().loadMessagesConfig();
                    Notifications_Event.getInstance().loadConfig();
                } else {
                    sendMsg(chatId, "Check the config is correct.");
                }

                sendNotifMenu(chatId);
                break;
            case "notification_update":
                if (TelegramRemote.getInstance().getConfig().getBoolean("telegram.notifications.enable_update")) {
                    TelegramRemote.getInstance().getConfig().set("telegram.notifications.enable_update", false);
                    TelegramRemote.getInstance().saveConfig();

                    TelegramRemote.getInstance().reloadConfig();
                    TelegramRemote.getInstance().loadMessagesConfig();
                    Notifications_Event.getInstance().loadConfig();
                } else if (!TelegramRemote.getInstance().getConfig().getBoolean("telegram.notifications.enable_update")) {
                    TelegramRemote.getInstance().getConfig().set("telegram.notifications.enable_update", true);
                    TelegramRemote.getInstance().saveConfig();

                    TelegramRemote.getInstance().reloadConfig();
                    TelegramRemote.getInstance().loadMessagesConfig();
                    Notifications_Event.getInstance().loadConfig();
                } else {
                    sendMsg(chatId, "Check the config is correct.");
                }

                sendNotifMenu(chatId);
                break;
        }
    }

    public void bot_started_notif() {
        FileConfiguration config = TelegramRemote.getInstance().getConfig();
        List<Long> adminChatIds = config.getLongList("telegram.admin_ids");
        String enableStartNotif = config.getString("telegram.notifications.enable_bot_started");

        if (enableStartNotif != null && enableStartNotif.equals("true")) {
            for (Long adminChatId : adminChatIds) {
                sendMsg(adminChatId, TelegramRemote.getMessage("messages.telegram.bot_started"));
            }
        }
    }

    public void rcon(String command, long chatId) {
        if (isAdmin(chatId)) {
            return;
        }
        String currentState = fsmContext.getOrDefault(chatId, "DEFAULT");
        FileConfiguration config = TelegramRemote.getInstance().getConfig();
        try (RconClient client = RconClient.open(Objects.requireNonNull(config.getString("telegram.rcon.rcon_host")), config.getInt("telegram.rcon.rcon_port"), Objects.requireNonNull(config.getString("telegram.rcon.rcon_pass")))) {
            String serverResponse = client.sendCommand(command);
            if (currentState.equals("CONSOLE_MODE")) {
                sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.cmd_successfully_send"));
                if (serverResponse != null && !serverResponse.isEmpty()) {
                    sendMsg(chatId, serverResponse);
                }
            }
        }
    }

    private void sendMenu(long chatId) {
        if (isAdmin(chatId)) {
            return;
        }

        String currentState = fsmContext.getOrDefault(chatId, "DEFAULT");
        ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
        replyMarkup.setSelective(true);
        replyMarkup.setResizeKeyboard(true);
        replyMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(TelegramRemote.getMessage("messages.telegram.admin_menu.menu_admin")));
        keyboard.add(row);

        row = new KeyboardRow();
        row.add(new KeyboardButton(TelegramRemote.getMessage("messages.telegram.admin_menu.console_menu")));
        keyboard.add(row);

        replyMarkup.setKeyboard(keyboard);

        if (currentState.equals("CONSOLE_MODE")) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(TelegramRemote.getMessage("messages.telegram.quit_console"));
            message.setReplyMarkup(replyMarkup);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                getLogger().info("Message not delivered! ChatID: " + chatId + " " + e.getMessage());
            }
        }
        else {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(TelegramRemote.getMessage("messages.telegram.back_menu"));
            message.setReplyMarkup(replyMarkup);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                getLogger().info("Message not delivered! ChatID: " + chatId + " " + e.getMessage());
            }
        }
    }

    private boolean isAdmin(long chatId) {
        FileConfiguration config = TelegramRemote.getInstance().getConfig();
        List<Long> adminChatIds = config.getLongList("telegram.admin_ids");
        if (!adminChatIds.contains(chatId)) {
            sendMsg(chatId, TelegramRemote.getMessage("messages.telegram.admin_not_found"));
            fsmContext.put(chatId, "DEFAULT");
            return true;
        }

        return false;
    }
    public void sendMsg(long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            getLogger().info("Message not delivered! ChatID: " + chatId + " " + e.getMessage());
        }
    }
}
