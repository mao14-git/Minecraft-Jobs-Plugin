package org.example;

import java.util.Set;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;




public class VeinMiningListener implements Listener {

    public final JobsPlugin plugin;

    public VeinMiningListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only continue if right-clicked
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        // Validate if player is holding pickaxe first then validate pickaxe is valid
        ItemStack tool = player.getInventory().getItemInMainHand();
        if(!tool.getType().toString().endsWith("_PICKAXE")){
            return;
        }

        if(tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "Vein Mining doesn't work with Silk Touch!"));
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null) {
            return;
        }

        Material oreType = clickedBlock.getType();
        // Only allow vein mining on ores out of Blocks.yml
        if(!plugin.isOreConfigured(oreType)) {
            return;
        }


        if(!plugin.isValidPickaxeForOre(tool, oreType)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "Your pickaxe is not strong enough for that ore."));
            return;
        }

        // Check that the player has reached the required Level
        JobData data = plugin.getJobData(player.getUniqueId());
        if(data.getMiningLevel() < 1){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "You need at least Mining level 1 to use vein Mining."));
            return;
        }

        // Check for skill cooldown
        if(plugin.isSkillOnCooldown(player)){
            long remaining = plugin.getSkillCooldownRemaining(player) / 1000;
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "Skill cooldown " + remaining + "s"));
            return;
        }

        // Set the cooldown to 10seconds
        plugin.setSkillCooldown(player, 10000L);

        // Find all Connected ore of the same type
        Set<Block> veinBlocks = plugin.findConnectedOres(clickedBlock, oreType);

        // Calculate the total Exp of every destroyed block
        int totalMiningExp = 0;
        for(Block ore : veinBlocks){
            BlockKey key = BlockKey.fromLocation(ore.getLocation());
            // If the block was placed by player, remove it from the list and don't get exp
            if(!plugin.getPlacedBlocks().contains(key)){
                totalMiningExp += plugin.getMiningExp(ore.getType());
            }
            plugin.getPlacedBlocks().remove(key);
        }

        // Destroy each ore block naturally (so that Enchantments on tool apply)

        for(Block ore : veinBlocks){
            int ExpDrop = plugin.getOreExpDrop(ore.getType());
            ore.breakNaturally(tool);
            ExperienceOrb orb = ore.getWorld().spawn(ore.getLocation().add(0.5, 0.5, 0.5), ExperienceOrb.class);
            orb.setExperience(ExpDrop);
            Sound breakSound = Sound.BLOCK_STONE_BREAK;
            ore.getWorld().playSound(ore.getLocation(), breakSound, 1.0F, 1.0F);
            ore.getWorld().spawnParticle(Particle.FALLING_DUST,
                    ore.getLocation().add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, ore.getBlockData());
        }

        // add Exp to job
        data.addMiningEXP(totalMiningExp, player);

        // Update Scoreboard
        plugin.updateScoreboard(player);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.AQUA + "Vein Mining used! You gained " + totalMiningExp + " mining Exp."));

        event.setCancelled(true);
    }
}
