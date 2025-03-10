package org.example;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class JobListener implements Listener {
    private final JobsPlugin plugin;

    public JobListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }



    // If a player places a block which would give exp, add it to the List
    @EventHandler
    private void onBlockPlaced(BlockPlaceEvent event){
        Material type = event.getBlock().getType();

        // boolean, is it on our blockList?
        boolean isTrackable = plugin.getMiningEXPMap().containsKey(type) || plugin.getWoodcuttingEXPMap().containsKey(type);
        if(isTrackable){
            BlockKey key = BlockKey.fromLocation(event.getBlock().getLocation());
            plugin.getPlacedBlocks().add(key);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        BlockKey key = BlockKey.fromLocation(event.getBlock().getLocation());
        // If the block was placed by player, remove it from the list and don't get exp
        if(plugin.getPlacedBlocks().contains(key)){
            plugin.getPlacedBlocks().remove(key);
            return;
        }
        // gain EXP part per mined Block
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        JobData data = plugin.getJobData(player.getUniqueId());

        // ----------------- MINING ----------------- //
        int miningEXP = plugin.getMiningExp(blockType);
        if (miningEXP > 0) {
            int miningLevel = data.getMiningLevel();
            data.addMiningEXP(miningEXP, player);
            plugin.updateScoreboard(player);
            int miningLevel2 = data.getMiningLevel();
            if (miningLevel2 != miningLevel) {
                int miningExpNeeded = 100 * (data.getMiningLevel() + 1);
                player.sendTitle("", "Mining Level up!", 20, 60, 40);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("Mining Level: " + miningLevel2 + " - " + data.getMiningExp() + "/" + miningExpNeeded + " EXP"));
            }
        }

        // ----------------- WOODCUTTING ----------------- //
        int woodcuttingEXP = plugin.getWoodcuttingExp(blockType);
        if (woodcuttingEXP > 0) {
            int woodcuttingLevel = data.getWoodcuttingLevel();
            data.addWoodcuttingEXP(woodcuttingEXP);
            plugin.updateScoreboard(player);
            int woodcuttingLevel2 = data.getWoodcuttingLevel();
            if (woodcuttingLevel2 != woodcuttingLevel) {
                int woodcuttingExpNeeded = 100 * (data.getWoodcuttingLevel() + 1);
                player.sendTitle("", "Woodcutting Level up!", 10, 60, 40);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("Woodcutting Level: " + woodcuttingLevel2 + " - " + data.getWoodcuttingExp() + "/" + woodcuttingExpNeeded + " EXP"));
            }
        }
            }
}
