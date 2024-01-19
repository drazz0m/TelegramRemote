/**
 * -----------------------------------------------------------------------------
 * TR_CMD.java
 * by drazz
 * -----------------------------------------------------------------------------
 * Description: This class implements the command executor for the /telegramremote
 * command, providing functionality for reloading configurations and displaying
 * help messages.
 * -----------------------------------------------------------------------------
 * Version: 1.0.0
 * Last Updated: January 20, 2024
 * -----------------------------------------------------------------------------
 */
package me.drazz.telegramremote.commands;

import me.drazz.telegramremote.TelegramRemote;
import me.drazz.telegramremote.events.Notifications_Event;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class TR_CMD implements CommandExecutor {
    //Main_BOT telegramBot = new Main_BOT();
    // /tr arg0 arg1 arg2 arg3 arg4 arg5
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0){
            sender.sendMessage(TelegramRemote.getMessage("messages.help"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload"))
            if (sender.hasPermission("telegramremote.reload")) {
                TelegramRemote.getInstance().reloadConfig();
                TelegramRemote.getInstance().loadMessagesConfig();
                Notifications_Event.getInstance().loadConfig();
                sender.sendMessage(TelegramRemote.getMessage("messages.reload_successful"));
            }
            else {
                sender.sendMessage(TelegramRemote.getMessage("messages.noPermission"));
            }
        else if (args[0].equalsIgnoreCase("help")) {
            if (sender.hasPermission("telegramremote.help")) {
                sender.sendMessage(TelegramRemote.getMessage("messages.help"));
            }
            else {
                sender.sendMessage(TelegramRemote.getMessage("messages.noPermission"));
            }
        }
        else {
            sender.sendMessage(TelegramRemote.getMessage("messages.help"));
        }

        return true;
    }
}
