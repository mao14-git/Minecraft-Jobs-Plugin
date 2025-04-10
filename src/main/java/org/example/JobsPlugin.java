package org.example;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.scoreboard.*;
import org.example.commands.JobAdminCommand;
import org.example.commands.JobCommand;

public class JobsPlugin extends JavaPlugin {

    // Save player Job data
    private File playerDataFile;
    private FileConfiguration playerDataConfig;

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
        getServer().getPluginManager().registerEvents(new OnJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new VeinMiningListener(this), this);
        getServer().getPluginManager().registerEvents(new TreeFellerListener(this), this);
        getServer().getPluginManager().registerEvents(new StructureSaverListener(this), this);

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
    public Set<BlockKey> getPlacedBlocks() {
        return placedBlocks;
    }
    private void loadPlacedBlocks() {
        placedBlocksFile = new File(getDataFolder(), "placedblocks.yml");
        if (!placedBlocksFile.exists()) {
            getDataFolder().mkdirs();
            try {
                placedBlocksFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Failed to create placedblocks.yml file");
            }
        }
        placedBlocksConfig = YamlConfiguration.loadConfiguration(placedBlocksFile);
        List<String> keys = placedBlocksConfig.getStringList("blocks");
        for (String s : keys) {
            BlockKey key = BlockKey.fromString(s);
            if (key != null) {
                placedBlocks.add(key);
            }
        }
    }
    private void savePlacedBlocks() {
        List<String> keys = new ArrayList<>();
        for (BlockKey key : placedBlocks) {
            keys.add(key.toString());
        }
        placedBlocksConfig.set("blocks", keys);
        try {
            placedBlocksConfig.save(placedBlocksFile);
        } catch (IOException e) {
            getLogger().severe("Failed to save placedblocks.yml file");
        }
    }
    //-----------------------------------------------

    // What gives Exp for everyjob
    private void loadBlockConfig() {
        // Which Blocks will be important for the jobs
        File blockConfigFile = new File(getDataFolder(), "blocks.yml");
        saveResource("blocks.yml", true);
        blockConfig = YamlConfiguration.loadConfiguration(blockConfigFile);
    }
    private void loadXPValues() {
        // Clear everything first
        miningEXPMap.clear();
        woodcuttingEXPMap.clear();
        if (blockConfig.contains("mining")) {
            ConfigurationSection miningSection = blockConfig.getConfigurationSection("mining");
            for (String key : miningSection.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    int xp = miningSection.getInt(key);
                    miningEXPMap.put(mat, xp);
                }
            }
        }
        if (blockConfig.contains("woodcutting")) {
            ConfigurationSection woodSection = blockConfig.getConfigurationSection("woodcutting");
            for (String key : woodSection.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    int xp = woodSection.getInt(key);
                    woodcuttingEXPMap.put(mat, xp);
                }
            }
        }
    }
    //-----------------------------------------------

    // returns the Exp value for one specific Material
    public int getMiningExp(Material mat) {
        return miningEXPMap.getOrDefault(mat, 0);
    }
    public int getWoodcuttingExp(Material mat) {
        return woodcuttingEXPMap.getOrDefault(mat, 0);
    }
    //-----------------------------------------------

    // returns the entire map of materials to Exp
    public HashMap<Material, Integer> getMiningEXPMap() {
        return miningEXPMap;
    }
    public HashMap<Material, Integer> getWoodcuttingEXPMap() {
        return woodcuttingEXPMap;
    }
    //-----------------------------------------------

    // Create plugin folder
    private void createPlayerDataFile() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Create the players.yml file
        playerDataFile = new File(getDataFolder(), "players.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create players.yml");
            }
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
    }
    //-----------------------------------------------

    // load saved Job data of players
    public void loadPlayerData() {
        for (String key : playerDataConfig.getKeys(false)) {
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
            playerDataConfig.set(path + ".woodcuttingXP", data.getWoodcuttingExp());
        }
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save players.yml!");
        }
    }
    //-----------------------------------------------

    // Scoreboard Methods
    public void showScoreboard(Player player) {
        scoreboardPlayers.add(player.getUniqueId());
        updateScoreboard(player);
    }
    public void hideScoreboard(Player player) {
        if (player == null) {
            getLogger().warning("hideScoreboard called with null player");
            return;
        }
        scoreboardPlayers.remove(player.getUniqueId());
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
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
        if (!isScoreboardVisible(player)) {
            return;
        }
        Scoreboard board = player.getScoreboard();

        if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective objective = board.getObjective("jobstats");

        if (objective == null) {
            objective = board.registerNewObjective("jobstats", "dummy", ChatColor.YELLOW + "Job Stats");
        } else {
            objective.setDisplayName(ChatColor.YELLOW + "Job Stats");
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (String entry : board.getEntries()) {
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
        int woodcuttingExp = data.getWoodcuttingExp();
        int woodcuttingExpNeeded = 100 * (woodcuttingLevel + 1);

        Score woodcuttingScore = objective.getScore(
                ChatColor.GREEN + "Woodcutting: " + ChatColor.YELLOW + "Lv " +
                        woodcuttingLevel + " (" + woodcuttingExp + "/" + woodcuttingExpNeeded + ")");
        woodcuttingScore.setScore(woodcuttingLevel);

        player.setScoreboard(board);
    }
    //-----------------------------------------------

    public void playCooldownReadySound(Player player) {
        // plays a sound after the skill is ready. 3 chimes each after 0.1 seconds (2 ticks)
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 1.2F);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 1.6F);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 1.4F);
            }, 2L);
        }, 2L);


    }

    // Check if the ore is in Blocks.yml
    public boolean isMiningOreConfigured(Material oreType) {
        return miningEXPMap.containsKey(oreType);
    }
    public boolean isWoodcuttingBlockConfigured(Material oreType) {
        return woodcuttingEXPMap.containsKey(oreType);
    }
    // Validate if Pickaxe in hand is valid for each ore
    public boolean isValidPickaxeForOre(ItemStack tool, Material oreType) {
        if (tool == null || !tool.getType().toString().endsWith("_PICKAXE")) {
            return false;
        }

        Material pickType = tool.getType();
        return switch (oreType) {
            case NETHER_QUARTZ_ORE, DEEPSLATE_COAL_ORE, COAL_ORE -> (pickType == Material.WOODEN_PICKAXE ||
                    pickType == Material.STONE_PICKAXE ||
                    pickType == Material.IRON_PICKAXE ||
                    pickType == Material.DIAMOND_PICKAXE ||
                    pickType == Material.NETHERITE_PICKAXE);
            case DEEPSLATE_COPPER_ORE, COPPER_ORE, DEEPSLATE_IRON_ORE, IRON_ORE, DEEPSLATE_LAPIS_ORE, LAPIS_ORE ->
                    (pickType == Material.STONE_PICKAXE ||
                            pickType == Material.IRON_PICKAXE ||
                            pickType == Material.DIAMOND_PICKAXE ||
                            pickType == Material.NETHERITE_PICKAXE);
            case NETHER_GOLD_ORE, DEEPSLATE_GOLD_ORE, GOLD_ORE, DEEPSLATE_REDSTONE_ORE, REDSTONE_ORE,
                 DEEPSLATE_DIAMOND_ORE, RAW_IRON_BLOCK, DIAMOND_ORE, DEEPSLATE_EMERALD_ORE, EMERALD_ORE ->
                    (pickType == Material.IRON_PICKAXE ||
                            pickType == Material.DIAMOND_PICKAXE ||
                            pickType == Material.NETHERITE_PICKAXE);
            case ANCIENT_DEBRIS -> (pickType == Material.DIAMOND_PICKAXE ||
                    pickType == Material.NETHERITE_PICKAXE);
            default -> false;
        };

    }
    // Flood-fill to find all connected ore blocks of the same type
    public Set<Block> findConnectedBlock(Block start, Material oreType) {
        Set<Block> found = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);
        found.add(start);

        while (!queue.isEmpty()) {
            Block current = queue.poll();

            for (Block face : getAdjacentBlocks(current)) {
                if (face.getType() == oreType && !found.contains(face)) {
                    found.add(face);
                    queue.add(face);
                }
            }
        }
        return found;
    }
    // Returns list of blocks adjacent
    public List<Block> getAdjacentBlocks(Block block) {
        List<Block> adj = new ArrayList<>();
        adj.add(block.getRelative(BlockFace.UP));
        adj.add(block.getRelative(BlockFace.DOWN));
        adj.add(block.getRelative(BlockFace.NORTH));
        adj.add(block.getRelative(BlockFace.SOUTH));
        adj.add(block.getRelative(BlockFace.EAST));
        adj.add(block.getRelative(BlockFace.WEST));
        return adj;
    }
    public int getOreExpDrop(Material mat) {
        Random random = new Random();
        return switch (mat) {
            case COAL_ORE, DEEPSLATE_COAL_ORE -> random.nextInt(3);
            case NETHER_GOLD_ORE -> random.nextInt(2);
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, EMERALD_ORE -> 3 + random.nextInt(5);
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE, NETHER_QUARTZ_ORE -> 2 + random.nextInt(4);
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> 1 + random.nextInt(5);
            default -> 0;
        };
    }
    //-----------------------------------------------

    // Get or create a player's JobData
    public JobData getJobData (UUID uuid){
        if (!jobDataMap.containsKey(uuid)) {
            // Default everything to 0
            JobData data = new JobData(0, 0, 0, 0);
            jobDataMap.put(uuid, data);
        }
        return jobDataMap.get(uuid);
    }
    //-----------------------------------------------
    }

