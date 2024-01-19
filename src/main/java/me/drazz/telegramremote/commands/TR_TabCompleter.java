/**
 * -----------------------------------------------------------------------------
 * TR_TabCompleter.java
 * by drazz
 * -----------------------------------------------------------------------------
 * Description: This class implements tab completion for the /telegramremote command,
 * providing suggestions for sub-commands.
 * -----------------------------------------------------------------------------
 * Version: 1.0.0
 * Last Updated: January 19, 2024
 * -----------------------------------------------------------------------------
 */
package me.drazz.telegramremote.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TR_TabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1){
            List<String> tab = new ArrayList<>();
            tab.add("help");
            tab.add("reload");
            return tab;
        }
        return null;
    }
}
