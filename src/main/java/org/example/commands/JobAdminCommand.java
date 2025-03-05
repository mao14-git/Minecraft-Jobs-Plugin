package org.example.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.JobData;
import org.example.JobsPlugin;

public class JobAdminCommand implements CommandExecutor {

    private final JobsPlugin plugin;

    public JobAdminCommand(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    // Admin commands to manage Jobs
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // sender == player check
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        // no rights
        if(!player.isOp()){
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        // forgot almost everything xD
        if(args.length < 2){
            player.sendMessage(ChatColor.DARK_AQUA + "Usage: /jobadmin <add|remove|reset> <job> <player> [amount]");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        String job = args[1].toLowerCase();

        // forgot to mention the player
        if(args.length < 3){
            player.sendMessage(ChatColor.DARK_AQUA + "Please specify a player. /jobadmin <add|remove|reset> <job> <player> [amount]");
            return true;
        }

        // is target player online?
        Player target = Bukkit.getPlayer(args[2]);
        if(target == null){
            player.sendMessage(ChatColor.DARK_AQUA + "Player not found.");
            return true;
        }

        JobData data = plugin.getJobData(target.getUniqueId());

        if (subcommand.equals("add")) {// /jobadmin add <job> <player> <amount>
            if (args.length < 4) {
                player.sendMessage(ChatColor.DARK_AQUA + "Usage: /jobadmin add <job> <player> <amount>");
                return true;
            }
            try {
                int addAmount = Integer.parseInt(args[3]);
                addXPToJob(data, job, addAmount, player);
                plugin.updateScoreboard(player);
                player.sendMessage(ChatColor.DARK_AQUA + "Added " + addAmount + " XP to " + target.getName() + "'s " + job + " job.");
            } catch (NumberFormatException e) {
                player.sendMessage("Amount must be be a whole number.");
            }
        } else if (subcommand.equals("remove")) {
            if (args.length < 4) {
                player.sendMessage("Usage: /jobadmin remove <job> <player> [amount]");
                return true;
            }
            try {
                int removeAmount = Integer.parseInt(args[3]);
                removeXPFromJob(data, job, removeAmount);
                plugin.updateScoreboard(player);
                player.sendMessage(ChatColor.DARK_AQUA + "Removed " + removeAmount + " XP to " + target.getName() + "'s " + job + " job.");
            } catch (NumberFormatException e) {
                player.sendMessage("Amount must be be a whole number.");
            }
        } else if (subcommand.equals("reset")) {
            resetJob(data, job);
            plugin.updateScoreboard(player);
            player.sendMessage(ChatColor.DARK_AQUA + target.getName() + "'s" + job + " has been reset.");
        } else {
            player.sendMessage(ChatColor.DARK_AQUA + "Invalid subcommand. Use /jobadmin add <job> <player> [amount]");
        }
        return true;

    }

    // Helper method add EXP for a specific job.
    private void addXPToJob(JobData data, String job, int amount, Player player){
        switch(job){
            case "mining":
                data.addMiningEXP(amount, player);
                break;
            case "woodcutting":
                data.addWoodcuttingEXP(amount);
                break;
            default:
                break;
        }
    }
    // Helper method remove EXP for a specific Job.
    private void removeXPFromJob(JobData data, String job, int amount){
        switch(job) {
            case "mining":
                int currentMiningExp = data.getMiningExp();
                int miningLevel = data.getMiningLevel();
                currentMiningExp -= amount;
                while(currentMiningExp < 0 && miningLevel > 0){
                    miningLevel--;
                    int expForLevel = 100 * (miningLevel + 1);
                    currentMiningExp += expForLevel;
                }
                data.setMiningLevel(miningLevel);
                data.setMiningXP(currentMiningExp);
                break;
            case "woodcutting":
                int currentWoodcuttingExp = data.getWoodcuttingExp();
                int woodcuttingLevel = data.getWoodcuttingLevel();
                currentWoodcuttingExp -= amount;
                while(currentWoodcuttingExp < 0 && woodcuttingLevel > 0){
                    woodcuttingLevel--;
                    int expForLevel = 100 * (woodcuttingLevel + 1);
                    currentWoodcuttingExp += expForLevel;
                }
                data.setMiningLevel(woodcuttingLevel);
                data.setMiningXP(currentWoodcuttingExp);
                break;
            default:
                break;

        }

    }
    // Helper methode reset job state
    private void resetJob(JobData data, String job){
        switch(job){
            case "mining":
                data.setMiningLevel(0);
                data.setMiningXP(0);
                break;
            case "woodcutting":
                data.setWoodcuttingLevel(0);
                data.setWoodcuttingXP(0);
                break;
            default:
                break;
        }
    }

}
