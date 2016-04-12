package me.lennartVH01;



import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.TrapDoor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;


public class AbbaGame {
	private Main plugin;
	public int taskId;
	public boolean open = false;
	public GameState state = GameState.WAITING;
	public String name;
	public Location spawn;
	public long endTime;
	public int duration;
	public int countDownTime;
	public int playerCap;
	public List<Player> players;
	public Map<UUID, CalculatedScore> endStats = new HashMap<UUID, CalculatedScore>();
	public Map<UUID, Chest> playerChests = new HashMap<UUID, Chest>();
	public List<Chest> chests = new ArrayList<Chest>();
	public List<Sign> signs = new ArrayList<Sign>();
	private Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	private Objective abbaObjective = scoreboard.registerNewObjective("AbbaStats", "dummy");
	private Score timer = abbaObjective.getScore("Time Remaining");
	
	
	public AbbaGame(Main plugin, String name, Location spawn, int duration, int playerCap, int countDownTime){
		this.plugin = plugin;
		this.name = name;
		this.spawn = spawn;
		this.duration = duration;
		this.playerCap = playerCap;
		this.countDownTime = countDownTime;
		if(playerCap == -1){
			players = new ArrayList<Player>();
		}else{
			players = new ArrayList<Player>(playerCap);
		}
		abbaObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	public void destroy(){
		for(Sign s:signs){
			s.setLine(0, "");
			s.setLine(1, "");
			s.update();
		}
		for(Player p:players){
			plugin.playerMap.remove(p.getUniqueId());
			p.sendMessage("�cGame ended!");
			
			//probably tp people to spawn or something
			
		}
	}
	
	public void start() {
		// TODO Add stuff like tp people to cave if neccecary
		endTime = System.currentTimeMillis() + 1000 * countDownTime;
		state = GameState.COUNTDOWN;
		
		for(Player p:players){
			p.sendMessage("�cGame starting!");
		}
		
		startClock(20);
	}
	
	
	private void startClock(long delay){
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				switch(state){
				case COUNTDOWN:
					if(endTime - System.currentTimeMillis() <= 0){
						state = GameState.RUNNING;
						endTime = System.currentTimeMillis() + duration * 1000;
						timer.setScore(duration);
						
						for(Player p:players){
							p.sendMessage("�cGOGOGO!");
							p.setScoreboard(scoreboard);
						}
						//do stuff when countdown is finished here
						
					}else{
						String message = "�c" + (endTime - System.currentTimeMillis())/1000;
						for(Player p:players){
							p.sendMessage(message);
						}
					}
					
					
					
					break;
				case RUNNING:
					int timeRemaining = (int) (endTime - System.currentTimeMillis())/1000;
					if(timeRemaining > 0){
						timer.setScore(timeRemaining);
					}else{
						//end game
						stopClock();
						state = GameState.FINISHED;
						scoreboard.resetScores("Time Remaining");
						
						for(Player p:players){
							p.sendMessage("Game ended!");
							p.teleport(spawn);
							
							
							Score score = abbaObjective.getScore(p.getName());
							CalculatedScore stat = AbbaTools.calcScore(p.getInventory());
							score.setScore(stat.getTotal());
							endStats.put(p.getUniqueId(), stat);
						}
						
					}
					
					break;
				default:
					break;
				
				}
				
			}
		}, delay, 20);
	}
	private void stopClock(){
		Bukkit.getScheduler().cancelTask(taskId);
	}
	
	public void open(){
		open = true;
	}
	public void close(){
		open = false;
	}
	public boolean addChest(Chest chest, Sign sign){
		if(!chests.contains(chest)){
			chests.add(chest);
			signs.add(sign);
			sign.setLine(0, "�9[" + name + "]");
			sign.update();
			
			return true;
		}
		return false;
	}
	
	
	
	public boolean isOpen(){
		return open;
	}
	public boolean hasRoom(){
		return (playerCap == -1 || players.size() < playerCap);
	}
	public int getPlayerCount(){
		return players.size();
	}
	public int getMaxPlayers(){
		return playerCap;
	}
	public String getName(){
		return name;
	}
	public Location getSpawn(){
		return spawn;
	}

	public void addPlayer(Player p) {
		players.add(p);
		int index = playerChests.size();
		Chest chest = chests.get(index);
		Sign sign = signs.get(index);
		sign.setLine(1, p.getName());
		sign.update();
		playerChests.put(p.getUniqueId(), chest);
		
		
	}
	public void removePlayer(Player p) {
		players.remove(p);
		int index = chests.indexOf(playerChests.remove(p.getUniqueId()));
		chests.remove(index);
		Sign sign = signs.remove(index);
		sign.setLine(1, "");
		sign.update();
	}
	

	public GameState getState(){
		return state;
	}

	public long getEndTime() {
		return endTime;
	}
	
	
	
	public void setDuration(long newDuration) {
		duration = (int) newDuration;
		
	}

	public void setEndTime(long newEndTime) {
		endTime = newEndTime;
		
	}
	
	
}
