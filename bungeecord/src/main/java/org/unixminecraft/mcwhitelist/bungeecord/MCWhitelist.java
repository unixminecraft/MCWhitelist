package org.unixminecraft.mcwhitelist.bungeecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class MCWhitelist extends Plugin implements Listener {

    private static final String ERROR_HEADER = "[MCWhitelist - BungeeCord]: ";
    
    private Set<UUID> whitelist;
    
    @Override
    public void onEnable() {
        
        File configurationFile = new File(getDataFolder(), "whitelist.yml");
        BufferedReader bufferedReader = null;
        String errorMessage = ERROR_HEADER + "FATAL ERROR: UNABLE TO LOAD WHITELIST FILE, COULD NOT START MCWhitelist PLUGIN!";
        
        try {
            bufferedReader = new BufferedReader(new FileReader(configurationFile));
        }
        catch(FileNotFoundException e) {
            System.out.print(errorMessage + "\n");
            throw new RuntimeException(e);
        }
        
        String line = null;
        
        try {
            line = bufferedReader.readLine();
        }
        catch(IOException e) {
            
            System.out.print(errorMessage + "\n");
            
            try {
                bufferedReader.close();
            }
            catch(IOException e1) {
                System.out.print(ERROR_HEADER + "Wow, can't even close the BufferedReader.\n");
            }
            
            throw new RuntimeException(e);
        }
        
        whitelist = new HashSet<UUID>();
        
        while(line != null) {
            
            UUID playerId = null;
            
            try {
                playerId = UUID.fromString(line);
            }
            catch(IllegalArgumentException e) {
                System.out.print(ERROR_HEADER + "Unable to decipher UUID " + line + ", skipping.\n");
                continue;
            }
            
            if(playerId == null) {
                System.out.print(ERROR_HEADER + "playerId variable null after supposedly being set to " + line + ", skipping.\n");
                continue;
            }
            
            whitelist.add(playerId);
            
            try {
                line = bufferedReader.readLine();
            }
            catch(IOException e) {
                System.out.print(ERROR_HEADER + "Unable to read another line, breaking from loop.\n");
                break;
            }
        }
        
        if(bufferedReader != null) {
            
            try {
                bufferedReader.close();
            }
            catch(IOException e) {
                System.out.print(ERROR_HEADER + "Wow, can't even close the BufferedReader.\n");
            }
        }
        
        getProxy().getPluginManager().registerListener(this, this);
    }
    
    @Override
    public void onDisable() {
        
        getProxy().getPluginManager().unregisterListener(this);
        whitelist.clear();
        whitelist = null;
    }
    
    @EventHandler
    public void onPreLogin(final PreLoginEvent preLoginEvent) {
        
        PendingConnection pendingConnection = preLoginEvent.getConnection();
        UUID playerId = pendingConnection.getUniqueId();
        
        while(playerId == null) {
            playerId = pendingConnection.getUniqueId();
        }
        
        if(!whitelist.contains(playerId)) {
            preLoginEvent.setCancelled(true);
        }
    }
}
