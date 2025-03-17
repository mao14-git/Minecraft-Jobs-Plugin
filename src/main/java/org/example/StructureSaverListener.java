package org.example;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class StructureSaverListener implements Listener {
    private final JobsPlugin plugin;

    public StructureSaverListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if(item.getType() != Material.STICK) {
            return;
        }

        if(!event.getPlayer().isOp()){
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null) {
            return;
        }

        Material blockType = clickedBlock.getType();
        // Only save wood log out of Blocks.yml
        if(!plugin.isWoodcuttingBlockConfigured(blockType)) {
            return;
        }

        Player player = event.getPlayer();
        player.swingMainHand();
        Set<Block> blocksToMark = new HashSet<>();

        Queue<Block> queue = new LinkedList<>();

        queue.add(clickedBlock);

        while(!queue.isEmpty()){
            Block current = queue.poll();
            if(blocksToMark.contains(current)){
                continue;
            }
            if(current.getType() == blockType) {
                blocksToMark.add(current);
                for(Block adjacent : plugin.getAdjacentBlocks(current)){
                    if(!blocksToMark.contains(adjacent) && adjacent.getType() == blockType){
                        queue.add(adjacent);
                    }
                }
            }
        }

        int markedCount = 0;
        for(Block block : blocksToMark){
            BlockKey key = BlockKey.fromLocation(block.getLocation());
            if(plugin.getPlacedBlocks().add(key)){
                markedCount++;
            }
        }
        event.getPlayer().sendMessage("Marked " + markedCount + " blocks");
        event.setCancelled(true);
    }
}
