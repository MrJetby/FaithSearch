package me.jetby.faithsearch;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class FaithSearch extends JavaPlugin implements CommandExecutor, Listener {
    private JDA jda;

    private final List<String> players = new ArrayList<>();
    private FileConfiguration config;



    @Override
    public void onEnable() {

        saveDefaultConfig();
        config = getConfig();
        jda = JDABuilder.createDefault(config.getString("discord.token")).build();

        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("search")).setExecutor(this);

    }

    @Override
    public void onDisable() {

    }



    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (players.contains(player.getName())) {
            List<String> msgList = config.getStringList("msg");
            for (String msg : msgList) {
                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("faithsearch.notify")) {
                        admin.sendMessage(msg.replace("{player}", player.getName()));
                    }
                }
            }
            List<String> commandsList = config.getStringList("command");

            sendToDiscord(Objects.requireNonNull(config.getString("discord.text")).replace("{player}", player.getName()));
            for (String command : commandsList) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
            }
            players.remove(player.getName());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            config = getConfig();
            sender.sendMessage("Reloaded config");
            return true;
        } else {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length != 1) {
                    p.sendMessage(config.getString("usage", "Usage: /search <player>"));
                    return true;
                }

                String targetName = args[0];



                if (!targetName.isEmpty()) {
                    players.add(targetName);

                    p.sendMessage(config.getString("search", "§a{player} обявлен в розыск").replace("{player}", targetName));

                }
                return true;
            }
        }


        return true;
    }
    private void sendToDiscord(String text) {
        assert config.getString("discord.channelId") != null;
        TextChannel channel = jda.getTextChannelById(Objects.requireNonNull(config.getString("discord.channelId")));

        if (channel != null) {
            channel.sendMessage(text).queue();
        }
    }

}
