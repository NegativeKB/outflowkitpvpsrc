package network.outflowkits;

import network.outflowkits.clans.ClansManager;
import network.outflowkits.clans.commands.ClanCMD;
import network.outflowkits.clans.listeners.ClanChatTag;
import network.outflowkits.clans.listeners.ClanStatisticsListener;
import network.outflowkits.data.ClansData;
import network.outflowkits.data.CooldownData;
import network.outflowkits.data.DonationData;
import network.outflowkits.data.PlayerData;
import network.outflowkits.kitpvp.commands.*;
import network.outflowkits.kitpvp.commands.KitUnlocker;
import network.outflowkits.kitpvp.kits.*;
import network.outflowkits.kitpvp.leaderboards.*;
import network.outflowkits.kitpvp.listeners.*;
import network.outflowkits.kitpvp.kits.selector.*;
import network.outflowkits.utils.FileUtils;
import network.outflowkits.utils.PlayerScoreboard;
import network.outflowkits.utils.runnables.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class KitPvP extends JavaPlugin {

    // -- KITPVP -- \\
    public HashMap<Player, Double> combat = new HashMap<>();
    public HashMap<Player, Player> combatwith = new HashMap<>();

    public HashMap<Player, Integer> repair_warmup = new HashMap<>();
    public HashMap<Player, Integer> repair_warmup_location_x = new HashMap<>();
    public HashMap<Player, Integer> repair_warmup_location_z = new HashMap<>();

    public HashMap<Player, Integer> refill_warmup = new HashMap<>();
    public HashMap<Player, Integer> refill_warmup_location_x = new HashMap<>();
    public HashMap<Player, Integer> refill_warmup_location_z = new HashMap<>();

    public HashMap<Player, Integer> spawn_warmup = new HashMap<>();
    public HashMap<Player, Integer> spawn_warmup_location_x = new HashMap<>();
    public HashMap<Player, Integer> spawn_warmup_location_z = new HashMap<>();
    public ArrayList<Player> soup = new ArrayList<>();

    public HashMap<Player, Player> gulagplayers = new HashMap<>();

    public HashMap<Player, String> recentKit = new HashMap<>();

    public ArrayList<Player> buildmode = new ArrayList<>();

    public HashMap<Player, Player> target = new HashMap<>();

    public PlayerScoreboard scoreboard;
    public PlayerData data;
    public CooldownData cooldowns;
    public DonationData donations;

    // -- Clans -- \\
    public ClansData clansData;

    @Override
    public void onEnable() {
        registerData();
        loadFile("settings.yml");

        registerScoreboard();
        runRunnalbes();

        registerListeners();
        registerCommands();

    }

    private void registerCommands() {
        this.getCommand("combattimer").setExecutor(new CombatTimer());
        this.getCommand("spawn").setExecutor(new SpawnCMD());
        this.getCommand("bal").setExecutor(new Coins());
        this.getCommand("kitunlocker").setExecutor(new KitUnlocker());

        // Leaderboards
        this.getCommand("topkills").setExecutor(new TopKills());
        this.getCommand("topdeaths").setExecutor(new TopDeaths());
        this.getCommand("topcoins").setExecutor(new TopCoins());
        this.getCommand("topks").setExecutor(new TopKillstreak());

        this.getCommand("stats").setExecutor(new StatsCMD());
        this.getCommand("xp").setExecutor(new CheckXP());
        this.getCommand("addxp").setExecutor(new AddXP());
        this.getCommand("resetstats").setExecutor(new ResetStatistics());
        this.getCommand("kit").setExecutor(new WhatKit());
        this.getCommand("convert").setExecutor(new ConvertCMD());
        this.getCommand("leaderboards").setExecutor(new LeaderboardCMD());
        this.getCommand("build").setExecutor(new BuildCMD());
        this.getCommand("donate").setExecutor(new DonationCMD());

        this.getCommand("repair").setExecutor(new RepairCMD());
        this.getCommand("refill").setExecutor(new RefillCMD());

        this.getCommand("target").setExecutor(new TargetCMD());

        // Clans
        this.getCommand("clans").setExecutor(new ClanCMD());

    }

    private void registerListeners() {
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new JoinListener(), this);
        manager.registerEvents(new EnderPearlLaunch(), this);
        manager.registerEvents(new CombatListener(), this);
        manager.registerEvents(new DisableHunger(), this);
        manager.registerEvents(new SoupSign(), this);
        manager.registerEvents(new KitSelector(), this);
        manager.registerEvents(new DeleteBowls(), this);
        manager.registerEvents(new SoupRegistration(), this);
        manager.registerEvents(new Death(), this);
        manager.registerEvents(new RecentKit(), this);
        manager.registerEvents(new KitShopItem(), this);
        manager.registerEvents(new StatsCMD(), this);
        manager.registerEvents(new NoExplosion(), this);
        manager.registerEvents(new ChatListener(), this);
        manager.registerEvents(new ItemPickup(), this);
        manager.registerEvents(new PlayerSettings(), this);
        manager.registerEvents(new ConvertCMD(), this);
        manager.registerEvents(new LeaderboardCMD(), this);
        manager.registerEvents(new CommandListener(), this);
        manager.registerEvents(new PreventLeaveSpawnWOKit(), this);
        manager.registerEvents(new SelectKit(), this);
        manager.registerEvents(new UnlockKit(), this);
        manager.registerEvents(new BuildListener(), this);
        manager.registerEvents(new LeaveEvent(), this);

        manager.registerEvents(new TargetCMD(), this);

        manager.registerEvents(new Barbarian(), this);
        manager.registerEvents(new Chemist(), this);
        manager.registerEvents(new Fisherman(), this);
        manager.registerEvents(new Avatar(), this);
        manager.registerEvents(new Kidnapper(), this);
        manager.registerEvents(new Mario(), this);
        manager.registerEvents(new Kangaroo(), this);
        manager.registerEvents(new Ninja(), this);
        manager.registerEvents(new Stomper(), this);
        manager.registerEvents(new Dwarf(), this);
        manager.registerEvents(new Switcher(), this);
        manager.registerEvents(new Teleporter(), this);
        manager.registerEvents(new ClearInvSign(), this);

        // Leaderboards
        manager.registerEvents(new TopKills(), this);
        manager.registerEvents(new TopDeaths(), this);
        manager.registerEvents(new TopCoins(), this);
        manager.registerEvents(new TopKillstreak(), this);

        // Clans
        manager.registerEvents(new ClanChatTag(), this);
        manager.registerEvents(new ClanStatisticsListener(), this);


    }

    private void runRunnalbes() {
        BukkitTask combatRunnable = new CombatRunnable().runTaskTimer(this, 0, 20); // Combat tag timer
        BukkitTask enderpearlRunnable = new EnderPearlRunnable().runTaskTimer(this, 0, 20); // Enderpearl cooldown
        BukkitTask scoreboardRunnable = new ScoreboardRunnable().runTaskTimer(this, 0, 5); // Scoreboard refresh
        BukkitTask spawnRunnalbe = new SpawnRunnable().runTaskTimer(this, 0, 20); // Spawn warmup
        BukkitTask lbRunnalbe = new LeaderboardRunnable().runTaskTimer(this, 0, 20); // Spawn warmup

        BukkitTask barbarianRunnable = new BarbarianAbilityRunnable().runTaskTimer(this, 0, 20); // Barbarian Ability
        BukkitTask fishermanRunnable = new FishermanAbilityRunnable().runTaskTimer(this, 0, 20); // Fisherman Ability
        BukkitTask avatarRunnable = new AvatarAbilityRunnable().runTaskTimer(this, 0, 20); // Avatar Ability
        BukkitTask kidnapperRunnable = new KidnapperAbilityRunnable().runTaskTimer(this, 0, 20); // Kidnapper Ability
        BukkitTask chemistRunnable = new ChemistAbilityRunnable().runTaskTimer(this, 0, 20); // Chemist Ability
        BukkitTask marioRunnable = new MarioAbilityRunnable().runTaskTimer(this, 0, 20); // Mario Ability
        BukkitTask kangarooRunnable = new KangarooAbilityRunnable().runTaskTimer(this, 0, 20); // Kangaroo Ability
        BukkitTask ninjaRunnable = new NinjaAbilityRunnable().runTaskTimer(this, 0, 20); // Ninja Ability
        BukkitTask stomperRunnable = new StomperAbilityRunnable().runTaskTimer(this, 0, 20); // Stomper Ability
        BukkitTask switcherRunnable = new SwitcherAbilityRunnable().runTaskTimer(this, 0, 20); // Archer Ability

        BukkitTask repairRunnable = new RepairRunnable().runTaskTimer(this, 0, 20); // Repair Warmup
        BukkitTask refillRunnable = new RefillRunnable().runTaskTimer(this, 0, 20); // Refill Warmup

        BukkitTask soupRunnable = new SoupRunnable().runTaskTimer(this, 0, 5); // Soup break fix

    }

    public void loadFile(String name){
        File file = new File(getDataFolder(), name);
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

        if (!file.exists()){ FileUtils.loadResource(this, name); }

        try { fileConfig.load(file); }
        catch (Exception e3) { e3.printStackTrace(); }

        for(String priceString : fileConfig.getKeys(false)) {
            fileConfig.set(priceString, fileConfig.getString(priceString));
        }
    }

    private void registerScoreboard(){
        scoreboard = new PlayerScoreboard();
    }

    private void registerData(){
        data = new PlayerData();
        data.setupData();
        data.saveData();
        data.reloadData();

        clansData = new ClansData();
        clansData.setupData();
        clansData.saveData();
        clansData.reloadData();

        cooldowns = new CooldownData();
        cooldowns.setupData();
        cooldowns.saveData();
        cooldowns.reloadData();

        donations = new DonationData();
        donations.setupData();
        donations.saveData();
        donations.reloadData();
    }

    @Override
    public void onDisable() {
        ClansManager clans = new ClansManager();
        clans.clearActiveInvites();
    }
}
