/*
 * BuildBattle - Ultimate building competition minigame
 * Copyright (C) 2020 Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
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

package plugily.projects.buildbattle.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajerlair.commonsbox.minecraft.compat.ServerVersion;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import pl.plajerlair.commonsbox.minecraft.compat.ServerVersion.Version;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;
import plugily.projects.buildbattle.Main;

/**
 * Created by Tom on 29/07/2014.
 */
public class Utils {

    private static Main plugin = JavaPlugin.getPlugin(Main.class);
    public static final ItemStack PLAYER_HEAD_ITEM = XMaterial.PLAYER_HEAD.parseItem();

    private Utils() {
    }

    /**
     * Checks whether itemstack is named (not null, has meta and display name)
     *
     * @param stack item stack to check
     * @return true if named, false otherwise
     */
    public static boolean isNamed(ItemStack stack) {
        return stack != null && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName();
    }

    /**
     * Serialize int to use it in Inventories size
     * ex. you have 38 kits and it will serialize it to 45 (9*5)
     * because it is valid inventory size
     * next ex. you have 55 items and it will serialize it to 63 (9*7) not 54 because it's too less
     *
     * @param i integer to serialize
     * @return serialized number
     */
    public static int serializeInt(Integer i) {
        if (i == 0) return 9; //The function bellow doesn't work if i == 0, so return 9 in case that happens.
        return (i % 9) == 0 ? i : (i + 9 - 1) / 9 * 9;
    }

    public static ItemStack getGoBackItem() {
        return new ItemBuilder(XMaterial.STONE_BUTTON.parseItem())
                .name(plugin.getChatManager().colorMessage("Menus.Option-Menu.Go-Back-Button.Item-Name"))
                .lore(plugin.getChatManager().colorMessage("Menus.Option-Menu.Go-Back-Button.Item-Lore")).build();
    }

    public static ItemStack getSkull(String url) {
        ItemStack head = PLAYER_HEAD_ITEM.clone();
        if (url.isEmpty()) {
            return head;
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));
        if (ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_15_R1)) {
            try {
                Method mtd = headMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                mtd.setAccessible(true);
                mtd.invoke(headMeta, profile);
            } catch (Exception ignored) {
            }
        } else {
            try {
                Field profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);
            } catch (Exception ignored) {
            }
        }
        head.setItemMeta(headMeta);
        return head;
    }

    public static ItemStack setItemNameAndLore(ItemStack item, String name, List<String> lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(lore);
        item.setItemMeta(im);
        return item;
    }

    public static String matchColorRegex(String s) {
        String regex = "&?#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})";
        Matcher matcher = Pattern.compile(regex).matcher(s);
        while (matcher.find()) {
          String group = matcher.group(0);
          String group2 = matcher.group(1);

          try {
            s = s.replace(group, net.md_5.bungee.api.ChatColor.of("#" + group2) + "");
          } catch (Exception e) {
            Debugger.debug("Bad hex color match: " + group);
          }
        }

        return s;
    }

    @SuppressWarnings("deprecation")
    public static SkullMeta setPlayerHead(Player player, SkullMeta meta) {
        if (Bukkit.getServer().getVersion().contains("Paper") && player.getPlayerProfile().hasTextures()) {
          return CompletableFuture.supplyAsync(() -> {
            meta.setPlayerProfile(player.getPlayerProfile());
            return meta;
          }).exceptionally(e -> {
            Debugger.debug(plugily.projects.buildbattle.utils.Debugger.Level.WARN,
                "Retrieving player profile of " + player.getName() + " failed!");
            return meta;
          }).join();
        }

        if (Version.isCurrentHigher(Version.v1_12_R1)) {
          meta.setOwningPlayer(player);
        } else {
          meta.setOwner(player.getName());
        }
        return meta;
      }

    public static void sendActionBar(Player player, String message) {
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(message).create());
    }

    // https://www.spigotmc.org/threads/comprehensive-particle-spawning-guide-1-13.343001/
    public static void spawnParticle(Particle particle, Location loc, int count, double offsetX, double offsetY, double offsetZ, double extra) {
      if (particle == Particle.REDSTONE) {
        DustOptions dustOptions = new DustOptions(Color.RED, 2);
        loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra, dustOptions);
      } else if (particle == Particle.ITEM_CRACK) {
        ItemStack itemCrackData = new ItemStack(loc.getBlock().getType());
        loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra, itemCrackData);
      } else if (particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST || particle == Particle.FALLING_DUST) {
        loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra, loc.getBlock().getType().createBlockData());
      } else {
        loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra);
      }
    }

}
