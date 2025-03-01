package org.example;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.scoreboard.*;
import org.example.commands.JobAdminCommand;
import org.example.commands.JobCommand;

public class JobsPlugin extends JavaPlugin {

    // Save player Job data
    private File playerDataFile;
    private FileConfiguration playerDataConfig;

    // Which Blocks will be important for the jobs
    private File blockConfigFile;
    private FileConfiguration blockConfig;
    private HashMap<Material, Integer> miningEXPMap = new HashMap<>();
    private HashMap<Material, Integer> woodcuttingEXPMap = new HashMap<>();

    // Prevent "place and destroy"-Block Exp gain
    private Set<BlockKey> placedBlocks = new HashSet<>();
    private File placedBlocksFile;
    private FileConfiguration placedBlocksConfig;

    // Save UUID of players and their JobData
    private HashMap<UUID, JobData> jobDataMap = new HashMap<>();
    private Set<UUID> scoreboardPlayers = new HashSet<>();

    // Save UUID of players who doesn't want to keep the Scoreboard on
    public Set<UUID> showScoreboardOffByDefault = new HashSet<>();

    @Override
    public void onEnable() {
        // Create and load the YAML file
        createPlayerDataFile();
        loadPlayerData();

        // block-list
        loadBlockConfig();
        loadXPValues();
        // storedList for blocks which should not give exp (player placed blocks)
        loadPlacedBlocks();

        // Register the event listener
        getServer().getPluginManager().registerEvents(new JobListener(this), this);

        // Register commands
        getCommand("job").setExecutor(new JobCommand(this));
        getCommand("jobadmin").setExecutor(new JobAdminCommand(this));
    }

    @Override
    public void onDisable() {
        savePlayerData();
        savePlacedBlocks();
    }


    // Prevent "place and destroy"-Block Exp gain
    public Set<BlockKey> getPlacedBlocks(){
        return placedBlocks;
    }

    private void loadPlacedBlocks(){
        placedBlocksFile = new File(getDataFolder(), "placedblocks.yml");
        if(!placedBlocksFile.exists()){
            getDataFolder().mkdirs();
            try{
                placedBlocksFile.createNewFile();
            } catch (IOException e){
                getLogger().severe("Failed to create placedblocks.yml file");
            }
        }
        placedBlocksConfig = YamlConfiguration.loadConfiguration(placedBlocksFile);
        List<String> keys = placedBlocksConfig.getStringList("blocks");
        for(String s : keys){
            BlockKey key = BlockKey.fromString(s);
            if(key != null){
                placedBlocks.add(key);
            }
        }
    }

    private void savePlacedBlocks(){
        List<String> keys = new ArrayList<>();
        for(BlockKey key : placedBlocks){
            keys.add(key.toString());
        }
        placedBlocksConfig.set("blocks", keys);
        try{
            placedBlocksConfig.save(placedBlocksFile);
        } catch (IOException e){
            getLogger().severe("Failed to save placedblocks.yml file");
        }
    }


    // What gives Exp for everyjob
    private void loadBlockConfig(){
        blockConfigFile = new File(getDataFolder(), "blocks.yml");
        saveResource("blocks.yml", true);
        blockConfig = YamlConfiguration.loadConfiguration(blockConfigFile);
    }

    private void loadXPValues(){
        // Clear everything first
        miningEXPMap.clear();
        woodcuttingEXPMap.clear();
        if (blockConfig.contains("mining")){
            ConfigurationSection miningSection = blockConfig.getConfigurationSection("mining");
            for (String key : miningSection.getKeys(false)){
                Material mat = Material.matchMaterial(key);
                if (mat != null){
                    int xp = miningSection.getInt(key);
                    miningEXPMap.put(mat, xp);
                }
            }
        }
        if (blockConfig.contains("woodcutting")){
            ConfigurationSection woodSection = blockConfig.getConfigurationSection("woodcutting");
            for (String key : woodSection.getKeys(false)){
                Material mat = Material.matchMaterial(key);
                if (mat != null){
                    int xp = woodSection.getInt(key);
                    woodcuttingEXPMap.put(mat, xp);
                }
            }
        }
    }


    // returns the Exp value for one specific Material
    public int getMiningExp(Material mat){
        return miningEXPMap.getOrDefault(mat, 0);
    }

    public int getWoodcuttingExp(Material mat){
        return woodcuttingEXPMap.getOrDefault(mat, 0);
    }


    // returns the entire map of materials to Exp
    public HashMap<Material, Integer> getMiningEXPMap(){
        return miningEXPMap;
    }

    public HashMap<Material, Integer> getWoodcuttingEXPMap(){
        return woodcuttingEXPMap;
    }


    // Create plugin folder
    private void createPlayerDataFile() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Create the players.yml file
        playerDataFile = new File(getDataFolder(), "players.yml");
        if (!playerDataFile.exists()) {
            try{
                playerDataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create players.yml");
            }
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }


    // load saved Job data of players
    public void loadPlayerData() {
        for (String key : playerDataConfig.getKeys(false)){
            try {
                UUID uuid = UUID.fromString(key);

                // Load everything
                int miningLevel = playerDataConfig.getInt(key + ".miningLevel", 0);
                int miningXP = playerDataConfig.getInt(key + ".miningXP", 0);

                int woodcuttingLevel = playerDataConfig.getInt(key + ".woodcuttingLevel", 0);
                int woodcuttingXP = playerDataConfig.getInt(key + ".woodcuttingXP", 0);

                JobData data = new JobData(miningLevel, miningXP, woodcuttingLevel, woodcuttingXP);
                jobDataMap.put(uuid, data);
            } catch (IllegalArgumentException e) {
                getLogger().severe("Invalid UUID in players.yml");
            }
        }
    }
    // save Job data of players
    public void savePlayerData() {
        for (UUID uuid : jobDataMap.keySet()) {
            JobData data = jobDataMap.get(uuid);
            String path = uuid.toString();

            // Save fields
            playerDataConfig.set(path + ".miningLevel", data.getMiningLevel());
            playerDataConfig.set(path + ".miningXP", data.getMiningExp());

            playerDataConfig.set(path + ".woodcuttingLevel", data.getWoodcuttingLevel());
            playerDataConfig.set(path + ".woodcuttingXP", data.getWoodcuttingEXP());
        }
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save players.yml!");
        }
    }


    // Scoreboard Methods
    public void showScoreboard(Player player) {
        scoreboardPlayers.add(player.getUniqueId());
        updateScoreboard(player);
    }

    public void hideScoreboard(Player player) {
        if(player == null){
            getLogger().warning("hideScoreboard called with null player");
            return;
        }
        scoreboardPlayers.remove(player.getUniqueId());
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if(manager == null){
            getLogger().severe("ScoreboardManager is null");
            return;
        }
        Scoreboard empty = manager.getNewScoreboard();
        player.setScoreboard(empty);
    }

    public boolean isScoreboardVisible(Player player) {
        return scoreboardPlayers.contains(player.getUniqueId());
    }

    public void updateScoreboard(Player player) {
        if(!isScoreboardVisible(player)){
            return;
        }
        Scoreboard board = player.getScoreboard();

        if(board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()){
            board = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective objective = board.getObjective("jobstats");

        if (objective == null) {
            objective = board.registerNewObjective("jobstats", "dummy", ChatColor.YELLOW + "Job Stats");
        } else {
            objective.setDisplayName(ChatColor.YELLOW + "Job Stats");
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for(String entry: board.getEntries()){
            board.resetScores(entry);
        }

        JobData data = jobDataMap.get(player.getUniqueId());


        // Mining stats
        int miningLevel = data.getMiningLevel();
        int miningExp = data.getMiningExp();
        int miningExpNeeded = 100 * (miningLevel + 1);

        Score miningScore = objective.getScore(
                ChatColor.GREEN + "Mining: " + ChatColor.YELLOW + "Lv " +
                        miningLevel + " (" + miningExp + "/" + miningExpNeeded + ")");
        miningScore.setScore(miningLevel);

        // Woodcutting stats
        int woodcuttingLevel = data.getWoodcuttingLevel();
        int woodcuttingExp = data.getWoodcuttingEXP();
        int woodcuttingExpNeeded = 100 * (woodcuttingLevel + 1);

        Score woodcuttingScore = objective.getScore(
                ChatColor.GREEN + "Woodcutting: " + ChatColor.YELLOW + "Lv " +
                        woodcuttingLevel + " (" + woodcuttingExp + "/" + woodcuttingExpNeeded + ")");
        woodcuttingScore.setScore(woodcuttingLevel);

        player.setScoreboard(board);
    }


    // Get or create a player's JobData
    public JobData getJobData(UUID uuid) {
        if (!jobDataMap.containsKey(uuid)) {
            // Default everything to 0
            JobData data = new JobData(0, 0, 0, 0);
            jobDataMap.put(uuid, data);
        }
        return jobDataMap.get(uuid);
    }
}
