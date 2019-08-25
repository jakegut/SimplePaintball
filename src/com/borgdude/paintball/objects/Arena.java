package com.borgdude.paintball.objects;

import com.borgdude.paintball.Main;
import com.borgdude.paintball.managers.PaintballManager;
import com.borgdude.paintball.utils.ColorUtil;
import com.borgdude.paintball.utils.PaintballPlayer;

import net.milkbowl.vault.economy.EconomyResponse;

import org.apache.commons.lang.StringUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

public class Arena {

    private String title;
    private Location endLocation;
    private Location lobbyLocation;
    private HashMap<String, Team> teams;
    private int minPlayers;
    private int maxPlayers;
    private ArrayList<Sign> signs;
    private ArenaState arenaState;
    private Set<UUID> players;
    private HashMap<UUID, PaintballPlayer> pbPlayers;
    private Set<UUID> spectators;
    private HashMap<UUID, Integer> kills;
    private int timer;
    private BossBar bossBar;
    private int totalTime;
    private Main plugin;
    private boolean activated;
    private Scoreboard board;
    private HashMap<UUID, Gun> gunKits;
    private HashMap<UUID, Integer> spawnTimer = null;

    private PaintballManager paintballManger;

    public Arena(String name, Main plugin) {
        this.title = name;
        players = new HashSet<>();
        setSpectators(new HashSet<>());
        kills = new HashMap<>();
        arenaState = ArenaState.WAITING_FOR_PLAYERS;
        signs = new ArrayList<>();
        setGunKits(new HashMap<>());
        setSpawnTimer(new HashMap<>());
        pbPlayers = new HashMap<>();
        this.plugin = plugin;
        this.paintballManger = plugin.getPaintballManager();
        initializeTeams();
    }

    private void initializeTeams() {
        teams = new HashMap<>();
        for (ChatColor cc : ChatColor.values()) {
            Color c = ColorUtil.translateChatColorToColor(cc);
            if (c != null) {
                Team t = new Team(cc, ColorUtil.ChatColorToString(cc));
                teams.put(t.getName(), t);
            }
        }
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    private void updateBossbar() {
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar("TEMP", BarColor.BLUE, BarStyle.SOLID, new BarFlag[0]);
            bossBar.setVisible(true);
        }

        bossBar.setVisible(true);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                String m = String.valueOf(getTimer() / 60);
                String s = String.valueOf(getTimer() % 60);
                if (Integer.valueOf(s) < 10) {
                    s = "0" + s;
                }

                double progress = (getTimer() / (double) getTotalTime());
                bossBar.setTitle(m + ":" + s);
                bossBar.setProgress(progress);

                for (UUID id : getPlayers()) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        bossBar.addPlayer(p);
                    }
                }
            }
        });
    }

    private void removeBossbar() {
        if (bossBar == null)
            return;

        for (UUID id : getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                bossBar.removePlayer(p);
            }
        }
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
        this.timer = totalTime;
    }

    public ArenaState getArenaState() {
        return arenaState;
    }

    public void setArenaState(ArenaState arenaState) {
        this.arenaState = arenaState;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public ArrayList<Sign> getSigns() {
        return signs;
    }

    public void setSigns(ArrayList<Sign> signs) {
        this.signs = signs;
    }

    public void updateSigns() {
        // int i = 0;
        for (Sign s : getSigns()) {
            s.setLine(0, ChatColor.BLUE + "[PaintBall]");
            s.setLine(1, ChatColor.GREEN + getTitle());
            s.setLine(2, plugin.getLanguageManager().getMessage("Arena.State." + getArenaState().getFormattedName()));
            s.setLine(3, String.valueOf(getPlayers().size()) + "/" + String.valueOf(getMaxPlayers()));
            updateBlock(s);
            s.update(true);
//            i++;
//            Bukkit.getConsoleSender().sendMessage("Updated sign number " + i + "for arena " + getTitle());
        }
    }

    private void updateBlock(Sign s) {
        BlockData d = s.getBlockData();
        if (d instanceof Directional) {
            Directional directional = (Directional) d;
            Block blockBehind = s.getBlock().getRelative(directional.getFacing().getOppositeFace());
            blockBehind.setType(getMaterial());
        }
    }

    private Material getMaterial() {
        switch (getArenaState()) {
        case ENDING:
            return Material.GRAY_STAINED_GLASS;
        case IN_GAME:
            return Material.RED_STAINED_GLASS;
        case RESTARTING:
            return Material.PURPLE_STAINED_GLASS;
        case STARTING:
            return Material.CYAN_STAINED_GLASS;
        case WAITING_FOR_PLAYERS:
            return Material.GREEN_STAINED_GLASS;
        default:
            return Material.GLASS;
        }
    }

    public void checkToStart() {
        if (arenaState.equals(ArenaState.STARTING))
            return;

        updateSigns();

        System.out.println(title.equalsIgnoreCase("title"));
        if (title.equalsIgnoreCase("title") || getPlayers().size() >= getMinPlayers()) {

            Bukkit.getConsoleSender().sendMessage("Starting arena " + getTitle());
            setArenaState(ArenaState.STARTING);
            setTotalTime(getLobbyTime());
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    updateSigns();

                    if (!title.equalsIgnoreCase("title") && getPlayers().size() < getMinPlayers()) {
                        setArenaState(ArenaState.WAITING_FOR_PLAYERS);
                        bossBar.setVisible(false);
                        cancel();
                    }

                    if (getTimer() % 5 == 0) {
                        String msg = plugin.getLanguageManager().getMessage("Arena.Start-Countdown").replace("%time%",
                                String.valueOf(getTimer()));
                        for (UUID id : getPlayers()) {
                            Player p = Bukkit.getPlayer(id);
                            if (p != null)
                                p.sendMessage(msg);
                        }
                    }

                    if (getTimer() <= 5)
                        for (UUID id : getPlayers()) {
                            Player p = Bukkit.getPlayer(id);
                            if (p != null)
                                p.playSound(p.getLocation(), Sound.BLOCK_METAL_STEP, 1, 1);
                        }

                    updateBossbar();
                    setTimer(getTimer() - 1);

                    if (getTimer() <= 0) {
                        startGame();
                        cancel();
                    }
                }
            };

            runnable.runTaskTimer(this.plugin, 20, 20);
        } else {
            String msg = plugin.getLanguageManager().getMessage("Arena.Not-Enough-Players");
            for (UUID id : getPlayers()) {
                Player p = Bukkit.getPlayer(id);
                if (p != null)
                    p.sendMessage(msg);
            }
        }
    }

    private int getLobbyTime() {
        int time = plugin.getConfig().getInt("lobby-time");
        if (time <= 5) {
            time = 60;
            System.out.println("Invalid lobby time, please configure an integer greater than 5");
        }
        return time;
    }

    public void startGame() {
        addMetrics();
        assignTeams();
        for (Team t : teams.values())
            t.giveArmor();
//        blueTeam.giveArmor();
//        redTeam.giveArmor();
        spawnPlayers();
        generateKills();
        setListNames();
        setGameScoreboard();
        setTotalTime(getGameTime());
        setArenaState(ArenaState.IN_GAME);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {

                updateSigns();

                if (getTimer() == getTotalTime()) {
                    String msg = plugin.getLanguageManager().getMessage("Arena.Game-Started");
                    for (UUID id : getPlayers()) {
                        Player p = Bukkit.getPlayer(id);
                        if (p != null)
                            p.sendMessage(msg);
                    }
                }

                setTimer(getTimer() - 1);

                if (getTimer() % 60 == 0 && getTimer() != 0) {
                    String msg = plugin.getLanguageManager().getMessage("Arena.End-Countdown")
                            .replace("%time%", String.valueOf(getTimer() / 60))
                            .replace("%unit%", plugin.getLanguageManager().getMessage("Arena.Minute-Unit"));

                    for (UUID id : getPlayers()) {
                        Player p = Bukkit.getPlayer(id);
                        if (p != null)
                            p.sendMessage(msg);
                    }
                } else if (getTimer() == 30 || getTimer() == 15 || getTimer() <= 5) {
                    String msg = plugin.getLanguageManager().getMessage("Arena.End-Countdown")
                            .replace("%time%", String.valueOf(getTimer()))
                            .replace("%unit%", plugin.getLanguageManager().getMessage("Arena.Second-Unit"));
                    for (UUID id : getPlayers()) {
                        Player p = Bukkit.getPlayer(id);
                        if (p != null)
                            p.sendMessage(msg);
                    }
                }

                decreaseSpawnTime();
                updateBossbar();

                Team checkWinningTeam = null;

                for (Team t : teams.values()) {
                    if (t.getMembers().size() > 0) {
                        if (checkWinningTeam == null)
                            checkWinningTeam = t;
                        else {
                            checkWinningTeam = null; // No Winning team as there are more than one team with 1 or more
                                                     // players
                            break;
                        }
                    }
                }

                if (checkWinningTeam != null) {
                    stopGame(checkWinningTeam);
                    cancel();
                }

//                if (blueTeam.getMembers().size() == 0) {
//                    stopGame(redTeam);
//                    cancel();
//                }
//
//                if (redTeam.getMembers().size() == 0) {
//                    stopGame(blueTeam);
//                    cancel();
//                }

                if (getTimer() <= 0) {
                    stopGame();
                    cancel();
                }
            }

        };

        runnable.runTaskTimer(this.plugin, 20, 20);
    }

    private void decreaseSpawnTime() {
        Set<Map.Entry<UUID, Integer>> entries = new HashSet<>(getSpawnTimer().entrySet());
        for (Map.Entry<UUID, Integer> entry : entries) {
            int time = entry.getValue() - 1;
            if (time < 0) {
                getSpawnTimer().remove(entry.getKey());
                Bukkit.getPlayer(entry.getKey()).sendMessage(ChatColor.YELLOW + "You're not protected anymore");
            } else
                getSpawnTimer().replace(entry.getKey(), time);
        }

    }

    private void addMetrics() {
        Main.metrics.addCustomChart(new Metrics.SingleLineChart("games_played", () -> {
            return 1;
        }));
    }

    private int getGameTime() {
        int time = plugin.getConfig().getInt("game-time");
        if (time < 30) {
            time = 240;
            System.out.println("Invalid game time, please configure an integer greater than 30");
        }
        return time;
    }

    private void setGameScoreboard() {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("Paintball", "dummy");
        obj.setDisplayName("Paintball");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        int score = 15;
        for (Team t : teams.values()) {
            if (t.getMembers().size() == 0)
                continue;
            org.bukkit.scoreboard.Team killCounter = board.registerNewTeam(t.getScoreboardID());
            killCounter.addEntry(ChatColor.BLACK + "" + t.getChatColor() + "");
            killCounter.setPrefix(t.getChatName() + " " + ChatColor.RESET
                    + plugin.getLanguageManager().getMessage("In-Game.Kills").toLowerCase() + ": " + "0");
            obj.getScore(ChatColor.BLACK + "" + t.getChatColor() + "").setScore(score--);
        }

//        org.bukkit.scoreboard.Team blueKillCounter = board.registerNewTeam("blueKillCounter");
//        blueKillCounter.addEntry(ChatColor.BLACK + "" + ChatColor.BLUE + "");
//        blueKillCounter.setPrefix(
//                "Blue " + plugin.getLanguageManager().getMessage("In-Game.Kills").toLowerCase() + ": " + "0");
//
//        obj.getScore(ChatColor.BLACK + "" + ChatColor.BLUE + "").setScore(15);
//
//        org.bukkit.scoreboard.Team redTeamCounter = board.registerNewTeam("redKillCounter");
//        redTeamCounter.addEntry(ChatColor.BLACK + "" + ChatColor.RED + "");
//        redTeamCounter.setPrefix(
//                "Red " + plugin.getLanguageManager().getMessage("In-Game.Kills").toLowerCase() + ": " + "0");
//
//        obj.getScore(ChatColor.BLACK + "" + ChatColor.RED + "").setScore(14);

        for (UUID id : getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                if(!plugin.getConfig().getBoolean("Nametag-Visible"))
                    setNameplate(p, board);
                p.setScoreboard(board);

            }
        }
    }

    private void setNameplate(Player p, Scoreboard board) {
        Team t = getPlayerTeam(p);
        org.bukkit.scoreboard.Team sTeam = board.getTeam(t.getScoreboardID());
        sTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OWN_TEAM);
        sTeam.addEntry(p.getName());
    }

    public void updateScoreboard() {
//        Bukkit.getConsoleSender().sendMessage("Updated scoreboard for arena...");
        HashMap<String, Integer> killsMap = new HashMap<>();
        HashMap<String, String> scoreboardIdToName = new HashMap<>();
        for (Team t : teams.values()) {
            if (t.getMembers().size() > 0) {
                killsMap.put(t.getScoreboardID(), getTotalTeamKills(t));
                scoreboardIdToName.put(t.getScoreboardID(), t.getChatName());
            }

        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (UUID id : getPlayers()) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) {
                        Scoreboard board = p.getScoreboard();
                        for (Entry<String, Integer> entry : killsMap.entrySet()) {
                            board.getTeam(entry.getKey())
                                    .setPrefix(scoreboardIdToName.get(entry.getKey()) + " " + ChatColor.RESET
                                            + plugin.getLanguageManager().getMessage("In-Game.Kills").toLowerCase()
                                            + ": " + String.valueOf(entry.getValue()));
                        }

//                        board.getTeam("blueKillCounter").setPrefix(
//                                "Blue " + plugin.getLanguageManager().getMessage("In-Game.Kills").toLowerCase() + ": "
//                                        + String.valueOf(blueKills));
//
//                        board.getTeam("redKillCounter").setPrefix(
//                                "Red " + plugin.getLanguageManager().getMessage("In-Game.Kills").toLowerCase() + ": "
//                                        + String.valueOf(redKills));
                    }
                }
            }
        });
    }

    public void assignTeams() {
        String team;
        Random random = new Random();
        // if both teams have the same amount of players....
        for (UUID id : getPlayers()) {
            Player p = Bukkit.getServer().getPlayer(id);
            if (p == null)
                continue;
            p.getInventory().clear();
            p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, plugin.getConfig().getInt("game-time") * 20,
                    5, true));
            p.setLevel(0);

            List<Team> teamsToPlaceOn = getLowestMemberTeams();
            System.out.println("Teams:");
            for (Team t : teamsToPlaceOn) {
                System.out.println("\t" + t.getName());
            }
            Collections.shuffle(teamsToPlaceOn);
            Team t = teamsToPlaceOn.get(random.nextInt(teamsToPlaceOn.size()));
            team = t.getName();
            t.addMember(p);

//            if (redTeam.getMembers().size() == blueTeam.getMembers().size()) {
//                // add to a random team
//                if (random.nextBoolean()) {
//                    // add to red
//                    redTeam.addMember(p);
//                    team = "&c&lRed";
//                } else {
//                    // add to blue
//                    blueTeam.addMember(p);
//                    team = "&b&lBlue";
//                }
//            } else {
//                // add to the team with the smaller amount of players
//                if (redTeam.getMembers().size() < blueTeam.getMembers().size()) {
//                    // add to red
//                    redTeam.addMember(p);
//                    team = "&c&lRed";
//                } else {
//                    // add to blue
//                    blueTeam.addMember(p);
//                    team = "&b&lBlue";
//                }
//            }
            p.sendMessage(plugin.getLanguageManager().getMessage("Arena.Join-Team").replace("%team%",
                    ChatColor.translateAlternateColorCodes('&', team)));
        }

    }

    private List<Team> getLowestMemberTeams() {
        int minTeam = getMaxPlayers();

        List<Team> listTeams = new ArrayList<>(teams.values());

        listTeams.removeIf(t -> t.getSpawnLocations().size() == 0);

        for (Team t : listTeams) {
            minTeam = Math.min(minTeam, t.getMembers().size());
        }

        final int min = minTeam;

        listTeams.removeIf(t -> t.getMembers().size() > min);
        return listTeams;
    }

    public void spawnPlayers() {
        for (UUID id : getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null)
                continue;
            Gun gun = getGunKits().get(p.getUniqueId());
            if (gun == null) {
                System.out.println(p.getName() + "'s kit was null");
                gun = paintballManger.getGunByName("Regular");
            }
            ItemStack is = gun.getInGameItem();
            p.getInventory().addItem(is);

            // Add leave bed
            ItemStack bed = new ItemStack(Material.WHITE_BED);
            ItemMeta im = bed.getItemMeta();
            im.setDisplayName(plugin.getLanguageManager().getMessage("In-Game.Leave-Bed"));
            bed.setItemMeta(im);
            p.getInventory().setItem(8, bed);

            Team team = getPlayerTeam(p);
            p.teleport(team.getRandomLocation());
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 0.5f);
        }
    }

    public void setListNames() {

        for (Team t : teams.values()) {
            for (UUID id : t.getMembers()) {
                Player p = Bukkit.getPlayer(id);
                if (p != null) {
                    p.setPlayerListName(t.getChatColor() + p.getName() + ChatColor.RESET);
                    p.setDisplayName(t.getChatColor() + p.getName() + ChatColor.RESET);
                }
            }
        }
    }

    public void generateKills() {
        getKills().clear();
        for (UUID id : getPlayers()) {
            getKills().put(id, 0);
        }
    }

    public void stopGame(Team team) {
        setArenaState(ArenaState.RESTARTING);
        updateSigns();
        announceWinner(team);
        setStats(team);
        kickPlayers();
        for (Team t : teams.values())
            t.getMembers().clear();
//        blueTeam.getMembers().clear();
//        redTeam.getMembers().clear();
//        removeScoreboard();
        setArenaState(ArenaState.WAITING_FOR_PLAYERS);
        updateSigns();
    }

    private void setStats(Team team) {
        if (plugin.getDb() == null)
            return;

        Set<UUID> playersCopy = new HashSet<>(players);
        HashMap<UUID, Integer> killsCopy = new HashMap<>(kills);
        ArrayList<UUID> teamMembers = (team != null) ? new ArrayList<>(team.getMembers()) : null;

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID id : playersCopy) {
                    int kills = killsCopy.get(id);
                    int oldKills = plugin.getDb().getKills(id);
                    int newKills = kills + oldKills;

                    int wins = 0;
                    int oldWins = plugin.getDb().getWins(id);
                    if (teamMembers != null && teamMembers.contains(id))
                        wins = 1;

                    int newWins = wins + oldWins;

                    plugin.getDb().setTokens(id, newKills, newWins);
                }
            }
        };

        runnable.runTaskAsynchronously(this.plugin);

    }

    public void stopGame() {
        setArenaState(ArenaState.RESTARTING);
        updateSigns();
        Team team = announceWinner();
        setStats(team);
        kickPlayers();
        for (Team t : teams.values())
            t.getMembers().clear();
//        blueTeam.getMembers().clear();
//        redTeam.getMembers().clear();
//        removeScoreboard();
        setArenaState(ArenaState.WAITING_FOR_PLAYERS);
        updateSigns();
    }

    public void awardWinners(Team team) {
        if (Main.econ == null)
            return;

        int amount = plugin.getConfig().getInt("Economy.Reward-Team");

        if (amount == 0)
            return;

        if (team == null) {
            for (UUID id : getPlayers()) {
                Player p = Bukkit.getPlayer(id);
                if (p == null)
                    continue;
                EconomyResponse r = Main.econ.depositPlayer(p, amount / 2);
                if (r.transactionSuccess()) {
                    p.sendMessage(plugin.getLanguageManager().getMessage("Arena.Reward-Player")
                            .replace("%amount%", Main.econ.format(r.amount))
                            .replace("%balance%", Main.econ.format(r.balance)));
                } else {
                    p.sendMessage(String.format("An error occured: %s", r.errorMessage));
                }
            }
        } else {
            for (UUID id : team.getMembers()) {
                Player p = Bukkit.getPlayer(id);
                if (p == null)
                    continue;
                EconomyResponse r = Main.econ.depositPlayer(p, amount);
                if (r.transactionSuccess()) {
                    p.sendMessage(plugin.getLanguageManager().getMessage("Arena.Reward-Player")
                            .replace("%amount%", Main.econ.format(r.amount))
                            .replace("%balance%", Main.econ.format(r.balance)));
                } else {
                    p.sendMessage(String.format("An error occured: %s", r.errorMessage));
                }
            }
        }
    }

    public void announceWinner(Team team) {
        String teamMsg = "";

        if (team == null)
            return;

        teamMsg = team.getChatName();

//        if (team.equals(blueTeam))
//            teamMsg = ChatColor.BLUE + "Blue";
//        else if (team.equals(redTeam))
//            teamMsg = ChatColor.RED + "Red";

        String msg = plugin.getLanguageManager().getMessage("Arena.Win-Default").replace("%team%", teamMsg);
        for (UUID id : getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            p.sendMessage(msg);
        }
    }

    public Team announceWinner() {
//        int bKills = getTotalTeamKills(blueTeam);
//        int rKills = getTotalTeamKills(redTeam);
        int winningKills = 0;
        Team winningTeam = null;
        String teamMsg = "";
        for (Team t : teams.values()) {
            int kills = getTotalTeamKills(t);
            if (kills > winningKills) {
                winningTeam = t;
                teamMsg = t.getChatName();
            }
        }
//        if (bKills > rKills) {
//            teamMsg = ChatColor.BLUE + "Blue";
//            winningKills = bKills;
//            winningTeam = blueTeam;
//        } else {
//            teamMsg = ChatColor.RED + "Red";
//            winningKills = rKills;
//            winningTeam = redTeam;
//        }

        String msg = "";

        if (winningTeam != null)
            msg = plugin.getLanguageManager().getMessage("Arena.Win-Kills").replace("%team%", teamMsg)
                    .replace("%kills%", String.valueOf(winningKills))
                    .replace("%verb%", plugin.getLanguageManager().getMessage("In-Game.Kills").toLowerCase());
        else
            msg = plugin.getLanguageManager().getMessage("Arena.Tie-Kills")
                    .replace("%kills%", String.valueOf(winningKills))
                    .replace("%verb%", plugin.getLanguageManager().getMessage("In-Game.Kills").toLowerCase());

        for (UUID id : getPlayers())
            Bukkit.getPlayer(id).sendMessage(msg);

        awardWinners(winningTeam);
        return winningTeam;
    }

//    private void removeScoreboard(){
//        for(UUID id : getPlayers()){
//            Player p = Bukkit.getPlayer(id);
//            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
//        }
//        Bukkit.getConsoleSender().sendMessage("Removed scoreboards");
//    }

    public void setGunKit(Player player, Gun gun) {
        UUID id = player.getUniqueId();
        if (getGunKits().containsKey(id)) {
            getGunKits().replace(id, gun);
        } else {
            getGunKits().put(id, gun);
        }
        player.sendMessage(plugin.getLanguageManager().getMessage("Arena.Set-Gun").replace("%name%", gun.getName()));
    }

    private void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void kickPlayers() {
        for (UUID id : getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null)
                continue;
            p.setGameMode(Bukkit.getDefaultGameMode());
            p.removePotionEffect(PotionEffectType.SATURATION);
            p.teleport(getEndLocation());
            restoreInventory(p);
            p.setPlayerListName(ChatColor.RESET + p.getName());
            p.setDisplayName(ChatColor.RESET + p.getName());
            removeScoreboard(p);
        }

        for (UUID id : getSpectators()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null)
                continue;
            p.teleport(getEndLocation());
            p.setGameMode(Bukkit.getDefaultGameMode());
        }

        removeBossbar();

        Main.metrics.addCustomChart(new Metrics.AdvancedPie("guns_used", new Callable<Map<String, Integer>>() {

            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                for (Gun gun : getGunKits().values()) {
                    String name = gun.getName();
                    if (valueMap.containsKey(name)) {
                        int v = valueMap.get(name);
                        valueMap.put(name, v + 1);
                    } else {
                        valueMap.put(name, 1);
                    }
                }
                return valueMap;
            }
        }));

        getPlayers().clear();
        getSpectators().clear();
        getKills().clear();
        getGunKits().clear();
    }

    public void removePlayerInGame(Player p) {
        Team team = getPlayerTeam(p);
        team.getMembers().remove(p.getUniqueId());
        p.teleport(getEndLocation());
        p.setGameMode(Bukkit.getDefaultGameMode());
        p.removePotionEffect(PotionEffectType.SATURATION);
        getPlayers().remove(p.getUniqueId());
        getKills().remove(p.getUniqueId());
        restoreInventory(p);
        p.sendMessage(plugin.getLanguageManager().getMessage("Arena.Leave"));
        p.setPlayerListName(ChatColor.RESET + p.getName());
        p.setDisplayName(ChatColor.RESET + p.getName());
        removeScoreboard(p);
        if (bossBar != null) {
            bossBar.removePlayer(p);
        }
        updateSigns();
    }

    private void restoreInventory(Player p) {
        p.getInventory().clear();
        if (pbPlayers != null && pbPlayers.get(p.getUniqueId()) != null) {
            pbPlayers.get(p.getUniqueId()).setInfo();
            pbPlayers.remove(p.getUniqueId());
        } else {
            p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD
                    + "Your inventory wasn't able to loaded back. Please contact Simple Paintball Support and check the console");
            if (pbPlayers == null) {
                Bukkit.getServer().getConsoleSender()
                        .sendMessage(ChatColor.RED + "The player's inventories weren't there in the first place...");
            } else if (pbPlayers.get(p.getUniqueId()) == null) {
                Bukkit.getServer().getConsoleSender()
                        .sendMessage(ChatColor.RED + p.getName() + " didnt't get their inventory back");
            } else {
                Bukkit.getServer().getConsoleSender()
                        .sendMessage(ChatColor.RED + "You shouldn't have gotten this message");
            }

        }

    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(Player player) {
        if (endLocation == null) {
            player.sendMessage(ChatColor.RED + "No end location set. Use: /pb set end");
            return;
        }

        if (lobbyLocation == null) {
            player.sendMessage(ChatColor.RED + "No lobby location set. Use: /pb set lobby");
            return;
        }

        int teamSpawnsSet = 0;

        for (Team t : teams.values()) {
            if (t.getSpawnLocations().size() > 0)
                teamSpawnsSet++;
            if (teamSpawnsSet >= 2)
                break;
        }

        if (teamSpawnsSet < 2) {
            player.sendMessage(
                    ChatColor.RED + "Not enough team spawns set, add more spawns for other teams to continue.");
            return;
        }

//        if (blueTeam.getSpawnLocations().size() == 0) {
//            player.sendMessage(ChatColor.RED + "No blue team spawn locations set. Use: /pb set blue");
//            return;
//        }
//
//        if (redTeam.getSpawnLocations().size() == 0) {
//            player.sendMessage(ChatColor.RED + "No red team spawn locations set. Use: /pb set red");
//            return;
//        }

        activated = true;
        player.sendMessage(
                ChatColor.GREEN + "Arena " + ChatColor.AQUA + title + ChatColor.GREEN + " has been activated!");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public void setPlayers(Set<UUID> players) {
        this.players = players;
    }

    public Set<UUID> getSpectators() {
        return spectators;
    }

    public void setSpectators(Set<UUID> spectators) {
        this.spectators = spectators;
    }

    public HashMap<UUID, Integer> getKills() {
        return kills;
    }

    public void setKills(HashMap<UUID, Integer> kills) {
        this.kills = kills;
    }

    public Integer getTotalTeamKills(Team team) {
        int total = 0;

        for (UUID id : team.getMembers()) {
            if (!kills.containsKey(id))
                continue;

            total += kills.get(id);
        }

        return total;
    }

    public Team getPlayerTeam(Player player) {

        for (Team t : teams.values())
            if (t.containsPlayer(player))
                return t;

        return null;
    }

    public Integer getPlayerKills(Player player) {
        UUID pUUID = player.getUniqueId();
        if (kills.containsKey(pUUID))
            return kills.get(pUUID);

        return null;
    }

    public void respawnPlayer(Player player) {
//        UUID pUUID = player.getUniqueId();
        Team team = getPlayerTeam(player);
        Location spawnLoc = team.getRandomLocation();
        player.teleport(spawnLoc);
    }

    public HashMap<UUID, Gun> getGunKits() {
        return gunKits;
    }

    public void setGunKits(HashMap<UUID, Gun> gunKits) {
        this.gunKits = gunKits;
    }

    public HashMap<UUID, Integer> getSpawnTimer() {
        return spawnTimer;
    }

    public void setSpawnTimer(HashMap<UUID, Integer> spawnTimer) {
        this.spawnTimer = spawnTimer;
    }

    public void addSpawnTime(Player p, int time) {
        if (getSpawnTimer().containsKey(p.getUniqueId()))
            return;

        getSpawnTimer().put(p.getUniqueId(), time);
        p.sendMessage(plugin.getLanguageManager().getMessage("Arena.Protected").replace("%time%", String.valueOf(time))
                .replace("%unit%", plugin.getLanguageManager().getMessage("Arena.Second-Unit")));
    }

    public void checkMinMax() {
        if (getMinPlayers() < 2 || getMinPlayers() > getMaxPlayers()) {
            Bukkit.getServer().getConsoleSender()
                    .sendMessage("Value for min players for " + getTitle() + " is invalid, setting to 2");
            setMinPlayers(2);
        }

        if (getMaxPlayers() < getMinPlayers()) {
            Bukkit.getServer().getConsoleSender()
                    .sendMessage("Value for max players for " + getTitle() + " is invalid, setting to 10");
            setMaxPlayers(10);
        }

    }

    public void addPlayer(Player player) {
        getPlayers().add(player.getUniqueId());
        pbPlayers.put(player.getUniqueId(), new PaintballPlayer(player));
    }

    public void removePlayer(Player player) {
        player.sendMessage(plugin.getLanguageManager().getMessage("Arena.Leave"));
        player.teleport(getEndLocation());
        player.getInventory().clear();
        restoreInventory(player);
        getPlayers().remove(player.getUniqueId());
        if (getBossBar() != null) {
            getBossBar().removePlayer(player);
        }
        updateSigns();
    }

    public void spawnPlayer(Player player) {
        new BukkitRunnable() {

            @Override
            public void run() {
                Team t = getPlayerTeam(player);
                if (t == null) {
                    player.getInventory().clear();
                    player.setExp(0);
                    player.setLevel(0);
                    addLobbyItems(player);
                    player.teleport(getLobbyLocation());
                } else {
                    addInGameItems(player, t);
                    respawnPlayer(player); // Teleport to team location
                }

            }
        }.runTaskLater(plugin, 10);
    }

    private void addInGameItems(Player player, Team t) {
        player.getInventory().setArmorContents(t.generateArmor());
        Gun gun = getGunKits().get(player.getUniqueId());

        if (gun == null) {
            System.out.println(player.getName() + "'s kit was null");
            gun = paintballManger.getGunByName("Regular");
        }
        ItemStack is = gun.getInGameItem();
        player.getInventory().addItem(is);

        // Add leave bed
        ItemStack bed = new ItemStack(Material.WHITE_BED);
        ItemMeta im = bed.getItemMeta();
        im.setDisplayName(plugin.getLanguageManager().getMessage("In-Game.Leave-Bed"));
        bed.setItemMeta(im);
        player.getInventory().setItem(8, bed);

    }

    public void addLobbyItems(Player player) {
        // This runnable is to place lobby items after 10 ticks as other plugins may
        // clear inventory 5 ticks after they TP to another world
        new BukkitRunnable() {
            public void run() {
                if (getArenaState().equals(ArenaState.IN_GAME))
                    return;

                ItemStack is = new ItemStack(Material.WHITE_BED);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName(plugin.getLanguageManager().getMessage("In-Game.Leave-Bed"));
                is.setItemMeta(im);
                player.getInventory().setHeldItemSlot(2);
                player.getInventory().setItem(2, is);

                int i = 3;
                for (Gun gun : plugin.getPaintballManager().getGuns()) {
                    ItemStack lobbyItem = gun.getLobbyItem();
                    if (lobbyItem == null)
                        continue;
                    player.getInventory().setItem(i++, lobbyItem);
                }
            }
        }.runTaskLater(plugin, 10);
    }

    public Location getRandomLocation() {
        List<Location> locations = new ArrayList<>();
        for (Team t : teams.values()) {
            for (Location l : t.getSpawnLocations())
                locations.add(l);
        }

        return locations.get(new Random().nextInt(locations.size()));
    }

    public HashMap<String, Team> getTeams() {
        return this.teams;
    }
}
