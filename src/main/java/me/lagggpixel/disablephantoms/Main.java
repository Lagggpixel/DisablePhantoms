package me.lagggpixel.disablephantoms;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class Main extends JavaPlugin implements CommandExecutor, Listener {

  private boolean isWhitelist;
  private List<String> worlds;

  @Override
  public void onEnable() {
    loadConfig();
    registerListeners();
    registerCommands();
  }

  @Override
  public void onDisable() {
  }

  private void loadConfig() {
    getConfig().options().copyDefaults(true);
    saveConfig();
    isWhitelist = getConfig().getBoolean("whitelist");
    worlds = getConfig().getStringList("worlds");
  }

  private void registerListeners() {
    this.getServer().getPluginManager().registerEvents(this, this);
  }

  private void registerCommands() {
    Objects.requireNonNull(this.getCommand("disablephantoms")).setExecutor(this);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
    World world = event.getLocation().getWorld();
    if (world == null) {
      return;
    }
    if (!canSpawnInWorld(world)) {
      event.setCancelled(true);
    }
  }

  public boolean canSpawnInWorld(@NotNull World world) {
    String worldName = world.getName();
    return (!isWhitelist || !worlds.contains(worldName))
        && (isWhitelist || worlds.contains(worldName));
  }

  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
    if (args.length == 0) {
      sendCommandHelpMessage(commandSender);
    }
    String mainArg = args[0];
    if (mainArg.equalsIgnoreCase("reload")) {
      if (!commandSender.hasPermission("disablephantoms.command.reload")) {
        commandSender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
        return true;
      }
      if (args.length > 1) {
        commandSender.sendMessage(ChatColor.RED + "Too many arguments!");
        commandSender.sendMessage(ChatColor.RED + "Usage: /disablephantoms reload");
        return true;
      }
      loadConfig();
      commandSender.sendMessage("Disable Phantoms: Config reloaded!");
      return true;
    }
    if (mainArg.equalsIgnoreCase("help")) {
      sendCommandHelpMessage(commandSender);
      return true;
    }
    if (mainArg.equalsIgnoreCase("whitelist")) {
      if (!commandSender.hasPermission("disablephantoms.command.whitelist")) {
        commandSender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
        return true;
      }
      if (args.length == 1) {
        commandSender.sendMessage("Disable Phantoms: Whitelist mode is " + isWhitelist);
        return true;
      }
      if (args.length == 2) {
        String targetStatus = args[1];
        if (targetStatus.equalsIgnoreCase("status")) {
          commandSender.sendMessage("Disable Phantoms: Whitelist mode is " + isWhitelist);
          return true;
        }
        boolean targetStatusBoolean;
        if (targetStatus.equalsIgnoreCase("on") || targetStatus.equalsIgnoreCase("true")) {
          targetStatusBoolean = true;
        } else if (targetStatus.equalsIgnoreCase("off") || targetStatus.equalsIgnoreCase("false")) {
          targetStatusBoolean = false;
        } else {
          commandSender.sendMessage(ChatColor.RED + "Invalid argument!");
          commandSender.sendMessage(ChatColor.RED + "Usage: /disablephantoms whitelist <on/off/status>");
          return true;
        }
        isWhitelist = targetStatusBoolean;
        getConfig().set("whitelist", targetStatusBoolean);
        saveConfig();
        commandSender.sendMessage("Disable Phantoms: Whitelist mode set to " + targetStatusBoolean);
        return true;
      }
      commandSender.sendMessage(ChatColor.RED + "Too many arguments!");
      commandSender.sendMessage(ChatColor.RED + "Usage: /disablephantoms whitelist <on/off/status>");
      return true;
    }
    if (mainArg.equalsIgnoreCase("worlds")) {
      if (!commandSender.hasPermission("disablephantoms.command.worlds")) {
        commandSender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
        return true;
      }
      if (args.length > 1) {
        commandSender.sendMessage(ChatColor.RED + "Too many arguments!");
        commandSender.sendMessage(ChatColor.RED + "Usage: /disablephantoms worlds");
        return true;
      }
      commandSender.sendMessage("Disable Phantoms: Whitelisted/Blacklisted worlds: ");
      for (String world : worlds) {
        commandSender.sendMessage(" §7- §e"+ world);
      }
      return true;
    }
    if (mainArg.equalsIgnoreCase("world")) {
      if (!commandSender.hasPermission("disablephantoms.command.world")) {
        commandSender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
        return true;
      }
      if (args.length < 3) {
        commandSender.sendMessage(ChatColor.RED + "Too few arguments!");
        commandSender.sendMessage(ChatColor.RED + "Usage: /disablephantoms world <add/remove> <world>");
        return true;
      }
      if (args.length > 3) {
        commandSender.sendMessage(ChatColor.RED + "Too many arguments!");
        commandSender.sendMessage(ChatColor.RED + "Usage: /disablephantoms world <add/remove> <world>");
        return true;
      }
      String targetAction = args[1];
      if (targetAction.equalsIgnoreCase("add")) {
        String targetWorld = args[2];
        if (worlds.contains(targetWorld)) {
          commandSender.sendMessage(ChatColor.RED + "World already whitelisted/blacklisted!");
          return true;
        }
        worlds.add(targetWorld);
        getConfig().set("worlds", worlds);
        saveConfig();
        commandSender.sendMessage(ChatColor.GREEN + "World " + targetWorld + " successfully whitelisted/blacklisted!");
        return true;
      }
      if (targetAction.equalsIgnoreCase("remove")) {
        String targetWorld = args[2];
        if (!worlds.contains(targetWorld)) {
          commandSender.sendMessage(ChatColor.RED + "World not whitelisted/blacklisted!");
          return true;
        }
        worlds.remove(targetWorld);
        getConfig().set("worlds", worlds);
        saveConfig();
        commandSender.sendMessage(ChatColor.GREEN + "World " + targetWorld + " successfully removed from whitelist/blacklist!");
        return true;
      }
      commandSender.sendMessage(ChatColor.RED + "Invalid argument!");
      commandSender.sendMessage(ChatColor.RED + "Usage: /disablephantoms world <add/remove> <world>");
      return true;
    }
    sendCommandHelpMessage(commandSender);
    return true;
  }

  private void sendCommandHelpMessage(CommandSender commandSender) {
    commandSender.sendMessage("§6Disable Phantom Commands\n\n" +
        "§6disablephantoms §ereload §7- §eReload config\n" +
        "§6disablephantoms §ehelp §7- §eShow this help message\n" +
        "§6disablephantoms §ewhitelist <on/off/status> §7- §eToggle whitelist mode\n" +
        "§6disablephantoms §eworlds §7- §eShow worlds that are whitelisted or blacklisted\n" +
        "§6disablephantoms §eworld <add/remove> §7- §eAdd or remove a world from the whitelist/blacklist\n");
  }
}
