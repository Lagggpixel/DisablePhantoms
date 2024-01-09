package me.lagggpixel.disablephantoms;

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

  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {
    if (args.length != 1) {
      sendCommandHelpMessage(commandSender);
    }
    if (args.length == 1) {
      String mainArg = args[0];
      if (mainArg.equalsIgnoreCase("reload")) {
        loadConfig();
        commandSender.sendMessage("Disable Phantoms: Config reloaded!");
        return true;
      }
      if (mainArg.equalsIgnoreCase("help")) {
        sendCommandHelpMessage(commandSender);
        return true;
      }
    }
    return true;
  }

  private void sendCommandHelpMessage(CommandSender commandSender) {
    commandSender.sendMessage("Usage: /disablephantoms reload");
  }
}
