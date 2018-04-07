package me.tomthedeveloper.buildbattle;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.tomthedeveloper.buildbattle.bungee.BungeeManager;
import me.tomthedeveloper.buildbattle.events.SetupInventoryEvents;
import me.tomthedeveloper.buildbattle.events.onBuild;
import me.tomthedeveloper.buildbattle.events.onChatEvent;
import me.tomthedeveloper.buildbattle.events.onJoin;
import me.tomthedeveloper.buildbattle.events.onQuit;
import me.tomthedeveloper.buildbattle.events.onSpectate;
import me.tomthedeveloper.buildbattle.game.GameInstance;
import me.tomthedeveloper.buildbattle.handlers.ChatManager;
import me.tomthedeveloper.buildbattle.handlers.ConfigurationManager;
import me.tomthedeveloper.buildbattle.handlers.GameInstanceManager;
import me.tomthedeveloper.buildbattle.handlers.InventoryManager;
import me.tomthedeveloper.buildbattle.handlers.JSONWriter;
import me.tomthedeveloper.buildbattle.handlers.SignManager;
import me.tomthedeveloper.buildbattle.utils.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

/**
 * Created by Tom on 25/07/2014.
 */
public class GameAPI {

    private GameInstanceManager gameInstanceManager;
    private String name;
    private String abreviation;
    private boolean kitsenabled = false;
    private InventoryManager inventoryManager;
    private boolean bar = false;
    private boolean bungee;
    private boolean inventorymanagerEnabled = false;
    private String version;
    private Main plugin;
    private boolean needsMapRestore = false;
    private boolean allowBuilding = false;
    private SignManager signManager;

    public Main getPlugin() {
        return plugin;
    }

    public boolean isBarEnabled() {
        return bar;
    }

    public boolean isBungeeActivated() {
        return bungee;
    }

    public String getVersion() {
        return version;
    }

    public boolean getAllowBuilding() {
        return allowBuilding;
    }

    public void setAllowBuilding(boolean b) {
        this.allowBuilding = b;
    }

    public boolean needsMapRestore() {
        return needsMapRestore;
    }

    public void setNeedsMapRestore(boolean b) {
        needsMapRestore = b;
    }

    public boolean is1_8_R3() {
        if(getVersion().equalsIgnoreCase("v1_8_R3")) return true;
        return false;
    }

    public boolean is1_12_R1() {
        if(getVersion().equalsIgnoreCase("v1_12_R1")) return true;
        return false;
    }

    public boolean is1_7_R4() {
        if(getVersion().equalsIgnoreCase("v1_7_R4")) return true;
        return false;
    }

    public boolean is1_9_R1() {
        if(getVersion().equalsIgnoreCase("v1_9_R1")) return true;
        return false;
    }

    public void onSetup(Main plugin, CommandsInterface commandsInterface) {
        Items.gameAPI = this;
        this.plugin = plugin;

        System.out.print("GAMEAPI LOADED!");
        version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        onPreStart();
        ConfigurationManager.plugin = plugin;
        inventoryManager = new InventoryManager(plugin);
        signManager = new SignManager(this);
        gameInstanceManager = new GameInstanceManager();

        if(!plugin.getConfig().contains("bar")) plugin.getConfig().set("bar", false);
        if(!plugin.getConfig().contains("BungeeActivated")) plugin.getConfig().set("BungeeActivated", false);
        bungee = plugin.getConfig().getBoolean("BungeeActivated");
        if(!plugin.getConfig().contains("InventoryManager")) {
            plugin.getConfig().set("InventoryManager", false);
            plugin.saveConfig();
        }
        inventorymanagerEnabled = plugin.getConfig().getBoolean("InventoryManager");
        bar = plugin.getConfig().getBoolean("bar");

       /* if(bar){
            BossBar.plugin = this;
        } */

        ConfigurationManager.plugin = plugin;
        GameInstance.plugin = this;
        plugin.getServer().getPluginManager().registerEvents(this.getSignManager(), JavaPlugin.getPlugin(Main.class));
        plugin.getServer().getPluginManager().registerEvents(new onBuild(this), JavaPlugin.getPlugin(Main.class));
        plugin.getServer().getPluginManager().registerEvents(new onQuit(this), JavaPlugin.getPlugin(Main.class));
        plugin.getServer().getPluginManager().registerEvents(new onSpectate(this), JavaPlugin.getPlugin(Main.class));
        plugin.getServer().getPluginManager().registerEvents(new onChatEvent(this), JavaPlugin.getPlugin(Main.class));


        User.plugin = this;
        JSONWriter.plugin = this;
        me.tomthedeveloper.buildbattle.handlers.JSONReader.plugin = getPlugin();

        plugin.getServer().getPluginManager().registerEvents(new onSpectate(this), plugin);
        // plugin.getServer().getPluginManager().registerEvents(new onDoubleJump(this), plugin);
        if(!this.getAllowBuilding()) {
            plugin.getServer().getPluginManager().registerEvents(new onBuild(this), plugin);
        }
        plugin.getServer().getPluginManager().registerEvents(new onQuit(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SetupInventoryEvents(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new onJoin(this), plugin);

        loadLanguageFile();
        loadInstanceConfig();
        loadSigns();

        onStart();
        plugin.saveConfig();
        ChatManager.getFromLanguageConfig("Unlocks-at-level", ChatColor.GREEN + "Unlocks at level %NUMBER% ");
        if(plugin.getConfig().getBoolean("BungeeActivated")) {

            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");

            setupBungee();
        }

        if(!plugin.getConfig().contains("Disable-Leave-Command")) {
            plugin.getConfig().set("Disable-Leave-Command", false);
            plugin.saveConfig();
        }
        if(!plugin.getConfig().getBoolean("Disable-Leave-Command")) {
            plugin.getCommand("leave").setExecutor(new me.tomthedeveloper.buildbattle.commands.leave(this));
        }
        plugin.getCommand(this.getGameName()).setExecutor(new me.tomthedeveloper.buildbattle.commands.InstanceCommands(this, commandsInterface));
        plugin.getCommand("addsigns").setExecutor(new me.tomthedeveloper.buildbattle.commands.SignCommands(this));
    }

    public void onPreStart() {}

    public boolean areKitsEnabled() {
        return kitsenabled;
    }

    ;

    public void onStart() {}

    public String getGameName() {
        return name;
    }

    public void setGameName(String newName) {
        name = newName;
    }

    public GameInstanceManager getGameInstanceManager() {
        return gameInstanceManager;
    }

    public SignManager getSignManager() {
        return signManager;
    }

    private void loadInstanceConfig() {
        if(!plugin.getConfig().contains("instances.default")) {
            this.saveLoc("instances.default.lobbylocation", plugin.getServer().getWorlds().get(0).getSpawnLocation());
            this.saveLoc("instances.default.Startlocation", plugin.getServer().getWorlds().get(0).getSpawnLocation());
            this.saveLoc("instances.default.Endlocation", plugin.getServer().getWorlds().get(0).getSpawnLocation());
            plugin.getConfig().set("instances.default.minimumplayers", 2);
            plugin.getConfig().set("instances.default.maximumplayers", 10);
            plugin.getConfig().set("instances.default.mapname", "mapname");
            plugin.getConfig().set("instances.default.world", "worldname");
            if(this.needsMapRestore()) plugin.getConfig().set("instances.default.schematic", "schematic file name (without .schematic!)");
            plugin.saveConfig();


        }

    }

    public void loadLanguageFile() {
        FileConfiguration config = ConfigurationManager.getConfig("language");
        try {
            config.save("language");
        } catch(IOException e) {
            e.printStackTrace();
        }


    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public boolean isInventoryManagerEnabled() {
        return inventorymanagerEnabled;
    }

    private void loadSigns() {
        if(!plugin.getConfig().contains("signs")) {
            saveLoc("signs.example", Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        for(String path : plugin.getConfig().getConfigurationSection("signs").getKeys(false)) {
            if(path.contains("example")) continue;
            path = "signs." + path;

            Location loc = getLocation(path);
            if(loc == null) System.out.print("LOCATION IS NNNNUUUUULLLL!!");
            if(loc.getBlock().getState() instanceof Sign) {
                getSignManager().registerSign((Sign) loc.getBlock().getState());
            } else {
                System.out.println("Block at given location " + path + " isn't a sign!");
            }
        }
    }

    public void saveLoc(String path, Location loc) {
        String location = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
        plugin.getConfig().set(path, location);
        plugin.saveConfig();
    }


    public Location getLocation(String path) {
        String[] loc = plugin.getConfig().getString(path).split("\\,");
        plugin.getServer().createWorld(new WorldCreator(loc[0]));
        World w = plugin.getServer().getWorld(loc[0]);
        Double x = Double.parseDouble(loc[1]);
        Double y = Double.parseDouble(loc[2]);
        Double z = Double.parseDouble(loc[3]);
        float yaw = Float.parseFloat(loc[4]);
        float pitch = Float.parseFloat(loc[5]);
        Location location = new Location(w, x, y, z, yaw, pitch);
        return location;
    }


    public WorldEditPlugin getWorldEditPlugin() {
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if(p instanceof WorldEditPlugin) return (WorldEditPlugin) p;
        return null;
    }

    public String getAbreviation() {
        return abreviation;
    }

    public void setAbreviation(String abreviation) {
        this.abreviation = abreviation;
    }

    public void setupBungee() {
        FileConfiguration fileConfiguration = ConfigurationManager.getConfig("Bungee");

        if(!fileConfiguration.contains("Hub")) {
            fileConfiguration.set("Hub", "Hub");
            try {
                fileConfiguration.save(ConfigurationManager.getFile("Bungee"));
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

    }


}
