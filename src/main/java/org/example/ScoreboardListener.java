package org.example;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ScoreboardListener implements Listener {

    private final JobsPlugin plugin;

    public ScoreboardListener(JobsPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(!plugin.showScoreboardOffByDefault.contains(event.getPlayer().getUniqueId())){
            plugin.showScoreboard(event.getPlayer());
        }
    }
}
