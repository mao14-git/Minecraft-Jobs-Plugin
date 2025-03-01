package org.example;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JobListener implements Listener {
    private JobsPlugin plugin;

    public JobListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(!plugin.showScoreboardOffByDefault.contains(event.getPlayer().getUniqueId())){
            plugin.showScoreboard(event.getPlayer());
        }
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
            data.addMiningEXP(miningEXP);
            plugin.updateScoreboard(player);
            int miningLevel2 = data.getMiningLevel();
            if (miningLevel2 != miningLevel) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 12000, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 0));
                int miningExpNeeded = 100 * (data.getMiningLevel() + 1);
                player.sendTitle("", "Mining Level up! (Mining Level: " + miningLevel2 + " - " + data.getMiningExp() + "/" + miningExpNeeded + " EXP).", 20, 60, 40);
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
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 12000, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 0));
                int woodcuttingExpNeeded = 100 * (data.getWoodcuttingLevel() + 1);
                player.sendTitle("", "Woodcutting Level up! (Woodcutting Level: " + woodcuttingLevel2 + " - " + data.getMiningExp() + "/" + woodcuttingExpNeeded + " EXP).", 20, 60, 40);
            }
        }
            }
}
