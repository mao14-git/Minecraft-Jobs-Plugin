package org.example;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TreeFellerListener implements Listener {
    private final JobsPlugin plugin;
    // Cooldown Map for Tree Feller skill
    private final Map<UUID, Long> skillCooldowns = new HashMap<>();
    private static final Set<Material> NATURAL_GROUND = EnumSet.of(
            Material.DIRT,
            Material.MUDDY_MANGROVE_ROOTS,
            Material.MUD,
            Material.COARSE_DIRT,
            Material.ROOTED_DIRT,
            Material.PODZOL,
            Material.MOSS_BLOCK,
            Material.PALE_MOSS_BLOCK,
            Material.GRASS_BLOCK,
            Material.SHORT_GRASS,
            Material.TALL_GRASS,
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.TORCHFLOWER,
            Material.CLOSED_EYEBLOSSOM,
            Material.OPEN_EYEBLOSSOM,
            Material.WITHER_ROSE
    );

    public TreeFellerListener(JobsPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if(event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
//        if(!player.isOp()){
//            return;
//        }

        if(!player.isSneaking()){
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null){
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if(!tool.getType().name().endsWith("_AXE")) {
            return;
        }

        Material logType = clickedBlock.getType();
        if(!plugin.isWoodcuttingBlockConfigured(logType)) {
            return;
        }

        // Cancel the normal interaction earlier so that the Stripping log interaction doesn't happen
        event.setCancelled(true);

        JobData data = plugin.getJobData(player.getUniqueId());
        if(data.getWoodcuttingLevel() < 1){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "You need at least Woodcutting level 1 to use TreeFeller."));
            return;
        }

        // Root Check
        Block blockBelow = clickedBlock.getRelative(BlockFace.DOWN);
        //player.sendMessage(blockBelow.getType().name() + " = block below");
        //player.sendMessage(clickedBlock.getType().name() + " = clickedBlock");
        if(blockBelow.getType().name().equals(clickedBlock.getType().name())){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "Please use Tree Feller on the root block of the tree!"));
            return;
        }


        // the block below has to be natural ground
        if(!NATURAL_GROUND.contains(blockBelow.getType())) {
            //player.sendMessage(blockBelow.getType().name() + " = block 2222222");
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "Tree Feller can only be used on natural trees!"));
            return;
        }

        Set<Block> treeLogs = plugin.findConnectedBlock(clickedBlock, logType);
        int totalWoodcuttingExp = 0;
        for(Block log : treeLogs) {
            BlockKey key = BlockKey.fromLocation(log.getLocation());
            if (plugin.getPlacedBlocks().contains(key)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.RED + "You can't use Tree Feller on player-built structures!"));
                return;
            }
            totalWoodcuttingExp += plugin.getWoodcuttingExp(log.getType());
        }

        // Surroundings Check
        for(Block log : treeLogs){
            List<Block> adjacent = plugin.getAdjacentBlocks(log);
            for(Block adj : adjacent){
                Material adjType = adj.getType();
                if (adjType == Material.AIR) {
                    continue;
                }
                if(!(adjType == logType || (adjType.name().endsWith("_LEAVES")) || NATURAL_GROUND.contains(adjType))) {
                    //player.sendMessage(adjType + " is not a natural tree block!3333333");
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(ChatColor.RED + "Tree Feller can only be used on natural trees!"));
                    player.sendMessage(ChatColor.RED + "ERROR BLOCK: " + adjType + ". If its a normal Tree, tell Mao about this to fix it.");
                    return;
                }
            }
        }



        // Check for skill cooldown
        if(isSkillOnCooldown(player)){
            long remaining = getSkillCooldownRemaining(player) / 1000;
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "Tree Feller cooldown " + remaining + "s"));
            return;
        }

        // Set the cooldown to 10seconds
        setSkillCooldown(player, 10000L);

        player.swingMainHand();
        for(Block log : treeLogs){
            log.breakNaturally(tool);
            Sound breakSound = Sound.BLOCK_WOOD_BREAK;
            log.getWorld().playSound(log.getLocation(), breakSound, 1.0F, 1.0F);
            log.getWorld().spawnParticle(Particle.FALLING_DUST,
                    log.getLocation().add(0.5, 0.5, 0.5),
                    10, 0.2, 0.2, 0.2, log.getBlockData());
        }

        data.addWoodcuttingEXP(totalWoodcuttingExp, player);

        plugin.updateScoreboard(player);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.AQUA + "Tree Feller used! You gained " + totalWoodcuttingExp + " woodcutting Exp."));

    }

    // Tree Feller Cooldown Methods
    public boolean isSkillOnCooldown(Player player) {
        long currentTime = System.currentTimeMillis();
        Long availableTime = skillCooldowns.get(player.getUniqueId());
        return availableTime != null && currentTime < availableTime;
    }
    public void setSkillCooldown(Player player, long cooldownMillis) {
        long availableTime = System.currentTimeMillis() + cooldownMillis;
        skillCooldowns.put(player.getUniqueId(), availableTime);

        long delayTicks = cooldownMillis / 50L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()){
                plugin.playCooldownReadySound(player);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.AQUA + "Tree Feller is ready again"));
            }
        }, delayTicks);
    }
    public long getSkillCooldownRemaining(Player player) {
        Long availableTime = skillCooldowns.get(player.getUniqueId());
        if (availableTime == null) {
            return 0;
        }
        long remaining = availableTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    //-----------------------------------------------
}
