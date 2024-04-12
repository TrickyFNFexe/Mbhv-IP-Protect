package org.best.mbhvip;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public final class Mbhvip extends JavaPlugin implements Listener, CommandExecutor {

    private Map<String, String> playerIPs = new HashMap<>();
    private File logFile;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("ipwhitelist").setExecutor(this);
        loadPlayerIPs();
        File dataFolder = this.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        logFile = new File(dataFolder, "MbhvIP.log");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        savePlayerIPs();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ipwhitelist")) {
            if (!(sender instanceof Player) || ((Player) sender).isOp()) {
                if (args.length == 2) {
                    String playerName = args[0];
                    String playerIP = args[1];
                    if (isValidIP(playerIP)) {
                        playerIPs.put(playerName, playerIP);
                        sender.sendMessage("Player " + playerName + " has been whitelisted with IP " + playerIP);
                        return true;
                    } else {
                        sender.sendMessage("Invalid IP address format.");
                        return false;
                    }
                } else {
                    sender.sendMessage("Incorrect number of arguments. Usage: /ipwhitelist [player] [ip]");
                    return false;
                }
            } else {
                sender.sendMessage("You do not have permission to use this command.");
                return false;
            }
        }
        return false;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerIP = player.getAddress().getAddress().getHostAddress();

        if (playerIPs.containsValue(playerIP)) {
            String originalPlayer = getKeyByValue(playerIPs, playerIP);
            if (!playerName.equals(originalPlayer)) {
                player.kickPlayer("You are " + originalPlayer + ", Please don't log in through this account! If it's your account please ask an admin to whitelist you!");
                logToFile(playerName + "'s account was joined by " + originalPlayer + ". IP: " + playerIP);
            } else {
                player.sendMessage("Your IP was verified by MBHV IP, your ip is " + playerIP);
            }
        } else if (!playerIPs.containsKey(playerName)) {
            playerIPs.put(playerName, playerIP);
            player.sendMessage("Your IP was verified by MBHV IP, your ip is " + playerIP);
        }
    }


    private void loadPlayerIPs() {
        try {
            File dataFolder = this.getDataFolder();
            FileInputStream fis = new FileInputStream(new File(dataFolder, "playerIPs.ser"));
            ObjectInputStream ois = new ObjectInputStream(fis);
            playerIPs = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException ignored) {
        }
    }

    private void savePlayerIPs() {
        try {
            File dataFolder = this.getDataFolder();
            FileOutputStream fos = new FileOutputStream(new File(dataFolder, "playerIPs.ser"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(playerIPs);
            oos.close();
            fos.close();
        } catch (IOException ignored) {
        }
    }

    private boolean isValidIP(String ip) {
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private String getKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void logToFile(String message) {
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}