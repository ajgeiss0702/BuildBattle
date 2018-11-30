/*
 * BuildBattle 4 - Ultimate building competition minigame
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer and Tigerpanzer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.buildbattle.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import pl.plajer.buildbattle.Main;
import pl.plajer.buildbattle.arena.Arena;
import pl.plajer.buildbattle.arena.ArenaRegistry;
import pl.plajer.buildbattle.arena.plots.ArenaPlot;
import pl.plajer.buildbattle.handlers.ChatManager;
import pl.plajer.buildbattle.handlers.PermissionManager;
import pl.plajer.buildbattle.menus.SetupInventory;
import pl.plajer.buildbattle.utils.Cuboid;
import pl.plajer.buildbattle.utils.Utils;
import pl.plajerlair.core.services.exception.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.LocationUtils;

/**
 * Created by Tom on 15/06/2015.
 */
//todo recodeee
public class SetupInventoryEvents implements Listener {

  private Main plugin;

  public SetupInventoryEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onGameTypeSetClick(InventoryClickEvent e) {
    try {
      if (e.getWhoClicked().getType() != EntityType.PLAYER)
        return;
      Player player = (Player) e.getWhoClicked();
      if (!player.hasPermission(PermissionManager.getEditGames()))
        return;
      if (!e.getInventory().getName().contains("Game type:"))
        return;
      if (e.getInventory().getHolder() != null)
        return;
      if (!Utils.isNamed(e.getCurrentItem())) {
        return;
      }
      String name = e.getCurrentItem().getItemMeta().getDisplayName();
      name = ChatColor.stripColor(name);
      Arena arena = ArenaRegistry.getArena(e.getInventory().getName().replace("Game type: ", ""));
      if (arena == null) return;
      e.setCancelled(true);
      if (name.contains("Solo")) {
        player.closeInventory();
        arena.setArenaType(Arena.ArenaType.SOLO);
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        config.set("instances." + arena.getID() + ".gametype", "SOLO");
        ConfigUtils.saveConfig(plugin, config, "arenas");
      } else if (name.contains("Team")) {
        player.closeInventory();
        arena.setArenaType(Arena.ArenaType.TEAM);
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        config.set("instances." + arena.getID() + ".gametype", "TEAM");
        ConfigUtils.saveConfig(plugin, config, "arenas");
      }
      player.sendMessage(ChatColor.GREEN + "Game type of arena set to " + ChatColor.GRAY + name);
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    try {
      if (e.getWhoClicked().getType() != EntityType.PLAYER)
        return;
      Player player = (Player) e.getWhoClicked();
      if (!player.hasPermission(PermissionManager.getEditGames()))
        return;
      if (!e.getInventory().getName().contains("BB Arena:"))
        return;
      if (e.getInventory().getHolder() != null)
        return;
      if (!Utils.isNamed(e.getCurrentItem())) {
        return;
      }

      String name = e.getCurrentItem().getItemMeta().getDisplayName();
      name = ChatColor.stripColor(name);

      Arena arena = ArenaRegistry.getArena(e.getInventory().getName().replace("BB Arena: ", ""));
      if (arena == null) return;
      if (e.getCurrentItem().getType() == Material.NAME_TAG && e.getCursor().getType() == Material.NAME_TAG) {
        e.setCancelled(true);
        if (!Utils.isNamed(e.getCursor())) {
          player.sendMessage(ChatColor.RED + "This item doesn't has a name!");
          return;
        }
        player.performCommand("bb " + arena.getID() + " set MAPNAME " + e.getCursor().getItemMeta().getDisplayName());
        arena.setMapName(e.getCursor().getItemMeta().getDisplayName());
        e.getCurrentItem().getItemMeta().setDisplayName(ChatColor.GOLD + "Set a mapname (currently: " + e.getCursor().getItemMeta().getDisplayName());
        return;
      }
      ClickType clickType = e.getClick();
      if (name.contains("ending location")) {
        e.setCancelled(true);
        player.closeInventory();
        player.performCommand("bb " + arena.getID() + " set ENDLOC");
        return;
      }
      if (name.contains("lobby location")) {
        e.setCancelled(true);
        player.closeInventory();
        player.performCommand("bb " + arena.getID() + " set LOBBYLOC");
        return;
      }
      if (name.contains("maximum players")) {
        e.setCancelled(true);
        if (clickType.isRightClick()) {
          e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
          player.performCommand("bb " + arena.getID() + " set MAXPLAYERS " + e.getCurrentItem().getAmount());
        }
        if (clickType.isLeftClick()) {
          e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
          player.performCommand("bb " + arena.getID() + " set MAXPLAYERS " + e.getCurrentItem().getAmount());
        }
        player.closeInventory();
        player.openInventory(new SetupInventory(arena).getInventory());
      }

      if (name.contains("minimum players")) {
        e.setCancelled(true);
        if (clickType.isRightClick()) {
          e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
          player.performCommand("bb " + arena.getID() + " set MINPLAYERS " + e.getCurrentItem().getAmount());
        }
        if (clickType.isLeftClick()) {
          e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
          player.performCommand("bb " + arena.getID() + " set MINPLAYERS " + e.getCurrentItem().getAmount());
        }
        player.closeInventory();
        player.openInventory(new SetupInventory(arena).getInventory());
      }
      if (name.contains("Add game sign")) {
        e.setCancelled(true);
        plugin.getMainCommand().getAdminCommands().addSign(player, arena.getID());
        return;
      }
      if (name.contains("View setup video")) {
        e.setCancelled(true);
        player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorRawMessage("&6Check out this video: " + SetupInventory.VIDEO_LINK));
        player.closeInventory();
        return;
      }
      if (e.getCurrentItem().getType() != Material.NAME_TAG) {
        e.setCancelled(true);
      }
      if (name.contains("Add game plot")) {
        player.performCommand("bba addplot " + arena.getID());
      }
      if (name.contains("Add floor changer NPC")) {
        player.performCommand("bba addnpc");
      }
      if (name.contains("Set game type")) {
        player.closeInventory();
        Inventory inv = Bukkit.createInventory(null, 9, "Game type: " + arena.getID());
        ItemStack solo = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta soloMeta = solo.getItemMeta();
        soloMeta.setDisplayName(ChatColor.GREEN + "Solo game mode");
        soloMeta.setLore(Collections.singletonList(ChatColor.GRAY + "1 player per plot"));
        solo.setItemMeta(soloMeta);
        inv.addItem(solo);

        ItemStack team = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta teamMeta = team.getItemMeta();
        teamMeta.setDisplayName(ChatColor.GREEN + "Team game mode");
        teamMeta.setLore(Collections.singletonList(ChatColor.GRAY + "2 players per plot"));
        team.setItemMeta(teamMeta);
        inv.addItem(team);

        player.openInventory(inv);
      }
      if (name.contains("Register arena")) {
        e.setCancelled(true);
        e.getWhoClicked().closeInventory();
        if (arena.isReady()) {
          e.getWhoClicked().sendMessage(ChatColor.GREEN + "This arena was already validated and is ready to use!");
          return;
        }
        String[] locations = new String[]{"lobbylocation", "Endlocation"};
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        for (String s : locations) {
          if (!config.isSet("instances." + arena.getID() + "." + s) || config.getString("instances." + arena.getID() + "." + s).equals(LocationUtils.locationToString(Bukkit.getWorlds().get(0).getSpawnLocation()))) {
            e.getWhoClicked().sendMessage(ChatColor.RED + "Arena validation failed! Please configure following spawn properly: " + s + " (cannot be world spawn location)");
            return;
          }
        }
        if (config.getConfigurationSection("instances." + arena.getID() + ".plots") == null) {
          e.getWhoClicked().sendMessage(ChatColor.RED + "Arena validation failed! Please configure plots properly");
          return;
        } else {
          for (String plotName : config.getConfigurationSection("instances." + arena.getID() + ".plots").getKeys(false)) {
            if (config.isSet("instances." + arena.getID() + ".plots." + plotName + ".maxpoint") && config.isSet("instances." + arena.getID() + ".plots." + plotName + ".minpoint")) {
              ArenaPlot buildPlot = new ArenaPlot();
              buildPlot.setCuboid(new Cuboid(LocationUtils.getLocation(config.getString("instances." + arena.getID() + ".plots." + plotName + ".minpoint")),
                      LocationUtils.getLocation(config.getString("instances." + arena.getID() + ".plots." + plotName + ".maxpoint"))));
              buildPlot.fullyResetPlot();
              arena.getPlotManager().addBuildPlot(buildPlot);
            } else {
              e.getWhoClicked().sendMessage(ChatColor.RED + "Arena validation failed! Plots are not configured properly! (missing selection values)");
              return;
            }
          }
        }
        e.getWhoClicked().sendMessage(ChatColor.GREEN + "Validation succeeded! Registering new arena instance: " + arena.getID());
        config.set("instances." + arena.getID() + ".isdone", true);
        ConfigUtils.saveConfig(plugin, config, "arenas");
        List<Sign> signsToUpdate = new ArrayList<>();
        ArenaRegistry.unregisterArena(arena);
        if (plugin.getSignManager().getLoadedSigns().containsValue(arena)) {
          for (Sign s : plugin.getSignManager().getLoadedSigns().keySet()) {
            if (plugin.getSignManager().getLoadedSigns().get(s).equals(arena)) {
              signsToUpdate.add(s);
            }
          }
        }
        arena = new Arena(arena.getID());
        arena.setReady(true);
        arena.setMinimumPlayers(config.getInt("instances." + arena.getID() + ".minimumplayers"));
        arena.setMaximumPlayers(config.getInt("instances." + arena.getID() + ".maximumplayers"));
        arena.setMapName(config.getString("instances." + arena.getID() + ".mapname"));
        arena.setLobbyLocation(LocationUtils.getLocation(config.getString("instances." + arena.getID() + ".lobbylocation")));
        arena.setEndLocation(LocationUtils.getLocation(config.getString("instances." + arena.getID() + ".Endlocation")));
        arena.setArenaType(Arena.ArenaType.valueOf(config.getString("instances." + arena.getID() + ".gametype").toUpperCase()));

        for (String plotName : config.getConfigurationSection("instances." + arena.getID() + ".plots").getKeys(false)) {
          ArenaPlot buildPlot = new ArenaPlot();
          buildPlot.setCuboid(new Cuboid(LocationUtils.getLocation(config.getString("instances." + arena.getID() + ".plots." + plotName + ".minpoint")),
                  LocationUtils.getLocation(config.getString("instances." + arena.getID() + ".plots." + plotName + ".maxpoint"))));
          buildPlot.fullyResetPlot();
          arena.getPlotManager().addBuildPlot(buildPlot);
        }
        arena.initPoll();
        ArenaRegistry.registerArena(arena);
        arena.start();
        for (Sign s : signsToUpdate) {
          plugin.getSignManager().getLoadedSigns().put(s, arena);
        }
      }
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

}
