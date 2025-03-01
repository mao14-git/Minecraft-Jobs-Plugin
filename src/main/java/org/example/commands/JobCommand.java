package org.example.commands;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.example.JobData;
import org.example.JobsPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class JobCommand implements CommandExecutor {

    private final JobsPlugin plugin;

    public JobCommand(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    // Job Commands
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Is the sender a player?
        if(!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        Player player = (Player) sender;
        JobData data = plugin.getJobData(player.getUniqueId());




        // Simple usage: /job -> shows your levels and XP
        if(args.length == 0){
            int miningExpNeeded = 100 * (data.getMiningLevel() + 1);
            int woodcuttingExpNeeded = 100 * (data.getWoodcuttingLevel() + 1);
            player.sendMessage(ChatColor.GOLD + "===== [ Your job Stats ] =====");
            player.sendMessage(ChatColor.YELLOW + "Mining level: "+ ChatColor.AQUA + data.getMiningLevel() + ChatColor.WHITE + " -> " + ChatColor.AQUA + data.getMiningExp() + "/" + miningExpNeeded + " EXP");
            player.sendMessage(ChatColor.YELLOW + "Woodcutting Level: " + ChatColor.AQUA + data.getWoodcuttingLevel() + ChatColor.WHITE + " -> " + ChatColor.AQUA + data.getWoodcuttingEXP() + "/" + woodcuttingExpNeeded + " EXP");
            return true;
        }

        // Add subcommands if you like: /job reset, /job top, etc.
        if(args[0].equalsIgnoreCase("help")){
            player.sendMessage(ChatColor.DARK_AQUA + "/job for own jobs stats");
            player.sendMessage(ChatColor.DARK_AQUA + "/job help: Show available commands");
            player.sendMessage(ChatColor.DARK_AQUA + "/job info: Shows information about the jobs");
            player.sendMessage(ChatColor.DARK_AQUA + "/job stats <player>: Shows the jobs stats of given player");
            player.sendMessage(ChatColor.DARK_AQUA + "/job show: Enables/Disables a jobs scoreboard");
            return true;
        }

        if(args[0].equalsIgnoreCase("show")){
            if(plugin.isScoreboardVisible(player)){
                plugin.hideScoreboard(player);
                plugin.showScoreboardOffByDefault.add(player.getUniqueId());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Job Scoreboard hidden"));
            }else{
                plugin.showScoreboard(player);
                plugin.showScoreboardOffByDefault.remove(player.getUniqueId());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Job Scoreboard shown"));
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("info")){
            showBlockXPInfo(player);
            return true;
        }

        if(args[0].equalsIgnoreCase("stats")){
            if(args.length == 1){
                player.sendMessage(ChatColor.DARK_AQUA + "Usage: /job stats <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if(target == null){
                player.sendMessage(ChatColor.DARK_AQUA + "Player not found.");
                return true;
            }
            JobData targetData = plugin.getJobData(target.getUniqueId());
            int targetMiningExpNeeded = 100 * (targetData.getMiningLevel() + 1);
            int targetWoodcuttingExpNeeded = 100 * (targetData.getWoodcuttingLevel() + 1);
            player.sendMessage(ChatColor.GOLD + "===== [ Your job Stats ] =====");
            player.sendMessage(ChatColor.YELLOW + "Mining level: "+ ChatColor.AQUA + targetData.getMiningLevel() + ChatColor.WHITE + " -> " + ChatColor.AQUA + targetData.getMiningExp() + ChatColor.DARK_AQUA + "/" + targetMiningExpNeeded + " EXP");
            player.sendMessage(ChatColor.YELLOW + "Woodcutting Level: " + ChatColor.AQUA + targetData.getWoodcuttingLevel() + ChatColor.WHITE + " -> " + ChatColor.AQUA + targetData.getWoodcuttingEXP() + ChatColor.DARK_AQUA + "/" + targetWoodcuttingExpNeeded + " EXP");
            return true;
        }

        if(args[0].equalsIgnoreCase("admin")){
            player.sendMessage(ChatColor.DARK_AQUA + "Nice try! xDD");
        }

        // Fallback message
        player.sendMessage(ChatColor.DARK_AQUA + "Unknown subcommand. Try /job help");
        return true;
    }

    // Helper Methode for /job info
    private void showBlockXPInfo(Player player){
        Map<Material, Integer> miningMap = plugin.getMiningEXPMap();
        Map<Material, Integer> woodcuttingMap = plugin.getWoodcuttingEXPMap();

        // HEADER
        player.sendMessage(ChatColor.DARK_AQUA + "===== [ Block EXP Info ] =====");

        // ----------------- MINING ----------------- //
        player.sendMessage(ChatColor.GOLD + "---- Mining Blocks ----");
        if (miningMap.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No mining blocks configured.");
        } else {
            for (Map.Entry<Material, Integer> entry : miningMap.entrySet()) {
                String blockName = entry.getKey().name();
                int expValue = entry.getValue();
                player.sendMessage(
                        ChatColor.YELLOW + blockName + ChatColor.WHITE + " -> " + ChatColor.AQUA + expValue + " EXP"
                );
            }
        }

        // SPACE
        player.sendMessage("");

        // ----------------- WOODCUTTING ----------------- //
        player.sendMessage(ChatColor.GOLD + "---- Woodcutting Blocks ----");
        if (woodcuttingMap.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No woodcutting blocks configured.");
        } else {
            for (Map.Entry<Material, Integer> entry : woodcuttingMap.entrySet()) {
                String blockName = entry.getKey().name();
                int xpValue = entry.getValue();
                player.sendMessage(
                        ChatColor.YELLOW + blockName + ChatColor.WHITE + " -> " + ChatColor.AQUA + xpValue + " XP"
                );
            }
        }

        // FOOT
        player.sendMessage(ChatColor.DARK_AQUA + "==============================");
    }

}
