package org.example;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class OnJoinListener implements Listener {

    private final JobsPlugin plugin;

    public OnJoinListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){

        Player player = event.getPlayer();

        plugin.getJobData(player.getUniqueId());

        if(!plugin.showScoreboardOffByDefault.contains(event.getPlayer().getUniqueId())){
            plugin.showScoreboard(event.getPlayer());
        }
    }

}
