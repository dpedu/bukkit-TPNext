package com.dpedu.tpnext;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Vector;

public class TPNext extends JavaPlugin {
	// List of previous destination
	Vector<TpRequest> lastTpByPlayer = new Vector<TpRequest>();
	
	// Handle /tpn or /tpnext. This command takes no arguments and has no options, so just do it.
    public boolean onCommand( CommandSender sender, Command command, String label, String args[] ) {
    	// Find the sender's last target
    	TpRequest lastQ = getTpRequestByHost(sender.getName());
    	
    	// Determine the new target
    	String target = "";
    	if(lastQ==null) {
    		target = getNextTarget("", sender.getName());
    	} else {
    		target = getNextTarget(lastQ.target, sender.getName());
    	}
    	
    	// No target found
    	if(target.equals("")) {
    		sender.sendMessage( "Couldn't find anyone :(" );
    		return true;
    	}
    	// Target found, teleport em
    	Server s = this.getServer();
    	sender.sendMessage( "Teleported to "+target );
    	s.getPlayer(sender.getName()).teleport(s.getPlayer(target));
    	
    	// Update them in the list of last queries
    	updateLastTp(sender.getName(), target);
    	
    	return true;
    }
    
    // Updates or adds a record to remember the last teleport per person
    public void updateLastTp(String requestor, String target) {
    	for(int i=0;i<lastTpByPlayer.size();i++) {
    		if( ((TpRequest)lastTpByPlayer.get(i)).requestor.equals(requestor)) {
    			TpRequest last = (TpRequest)lastTpByPlayer.get(i);
    			last.target = target;
    			return;
    		}
    	}
    	// Still here? Requestor's first TP then, add a record
    	lastTpByPlayer.add(new TpRequest(requestor, target));
    }
    
    // Determine the next target based on the previous one
    public String getNextTarget(String lastTarget, String me) {
    	// Get a list of players online
    	Server s = this.getServer();
    	Player[] allPlayers = s.getOnlinePlayers();
    	
    	// Go through the list of players until we hit the one AFTER the previous target
    	boolean doNext = false;
    	for(int i=0;i<allPlayers.length;i++) {
    		Player targetPlayer = allPlayers[i];
    		// Skip self, derp
    		if(targetPlayer.getName().equals(me)) {
    			continue;
    		}
    		// If we found the last target, the next player in the list is the new target
    		if(targetPlayer.getName().equals(lastTarget)) {
    			doNext=true;
    			continue;
    		}
    		if(doNext) {
    			return targetPlayer.getName();
    		}
    		
    	}
    	// If we're still here, then the target wasn't found. So default to the first player that isn't the caller
    	for(int i=0;i<allPlayers.length;i++) {
    		Player targetPlayer = allPlayers[i];
    		// Skip self
    		if(!targetPlayer.getName().equals(me)) {
    			return targetPlayer.getName();
    		}
    	}
    	// If we're still here, then nobody besides the caller was found. No target.
    	return "";
    }
    
    
    // Find a person's previous TpRequest
    public TpRequest getTpRequestByHost(String requestor) {
    	for(int i=0;i<lastTpByPlayer.size();i++) {
    		if( ((TpRequest)lastTpByPlayer.get(i)).requestor.equals(requestor)) {
    			return ((TpRequest)lastTpByPlayer.get(i));
    		}
    	}
    	return null;
    }
    
    // Simple caller->target relation object
    class TpRequest {
    	String requestor = null;
    	String target = null;
    	public TpRequest(String _requestor, String _target) {
    		requestor = _requestor;
    		target = _target;
    	}
    }
}
