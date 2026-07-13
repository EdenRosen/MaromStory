package team.control;

import java.util.Iterator;
import my_base.App;
import shared.ui_ports.UiPort;
import team.model.ArmorSet;
import team.model.Canvas;
import team.model.Enemy;
import team.model.EnemyType;
import team.model.GameMode;
import team.model.HeroType;
import team.model.MainPlayer;
import team.model.MapType;
import team.model.PlayerStats;
import team.model.ShopItem;
import team.model.Sword;

/**
 * Coordinates game setup combat menus progression and periodic updates
 */
public class Backend {

    private static final double MP_REGEN_PER_TICK = 0.05;
    private static final int MAX_ENEMIES         = 3;
    private static final int RESPAWN_DELAY_TICKS = 90;

    private static final int SOLO_ENEMIES_TO_CLEAR_STAGE = 20;
    private static final int MULTIPLAYER_ENEMIES_TO_CLEAR_STAGE = 30;
    private static final double MP_GAIN_PER_LEVEL = 15.0;

    private GameState state = GameState.GAME_MODE_SELECT;
    private int respawnTimer    = RESPAWN_DELAY_TICKS;
    private int nextEnemyId     = 100;
    private int pendingUpgrades = 0;
    private int shopPage        = 0;

    private int pvpWinner = 0;
    private int highestUnlockedMapIndex = 0;
    private final int[] stageKills = new int[MapType.values().length];

    private UiPort uiPort() {
        return UiPort.getInstance();
    }

    // Exposes the current screen state to routers and views
    public GameState getState()    { return state; }
    public boolean isGameStarted() { return state == GameState.PLAYING; }
    public boolean isGameOver()    { return state == GameState.GAME_OVER; }
    public boolean isUpgrade()     { return state == GameState.UPGRADE; }
    public boolean isMapSelect()   { return state == GameState.MAP_SELECT; }
    public boolean isShop()        { return state == GameState.SHOP; }
    public int     getShopPage()   { return shopPage; }
    public int getPendingUpgrades(){ return pendingUpgrades; }
    public int getPvpWinner() { return pvpWinner; }
    public boolean isMapUnlocked(int index) {
        return index >= 0 && index <= highestUnlockedMapIndex;
    }




    // Prepares the initial canvas and sends it to the user interface
    public void initializeApp() {
        Canvas canvas = App.content().canvas();
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());
        uiPort().log("App initialized — waiting for player to press Enter");
    }


    // Advances the setup flow from mode selection through hero selection into play
    public void startScenario() {
        if (state == GameState.GAME_OVER) {
            state = GameState.GAME_MODE_SELECT;
            pvpWinner = 0;
            Canvas canvas = App.content().canvas();
            resetStageProgression();
            canvas.setCurrentMap(MapType.MEADOW);
            canvas.initCanvas();
            uiPort().setMap(canvas.getMap());
            syncPlayers(canvas);
            return;
        }
        if (state == GameState.GAME_MODE_SELECT) {
            state = GameState.HERO_PLAYER1_SELECT;
            uiPort().log("Player 1: choose your hero.");
            return;
        }
        if (state == GameState.HERO_PLAYER1_SELECT) {
            if (App.content().canvas().getSelectedGameMode() == GameMode.SOLO) {
                startAdventureMatch();
            } else {
                state = GameState.HERO_PLAYER2_SELECT;
                uiPort().log("Player 2: choose your hero.");
            }
            return;
        }
        if (state == GameState.HERO_PLAYER2_SELECT) {
            if (App.content().canvas().getSelectedGameMode() == GameMode.PVP) {
                state = GameState.PVP_MAP_SELECT;
                uiPort().log("PvP: choose an arena.");
            } else {
                startAdventureMatch();
            }
            return;
        }
        if (state == GameState.PLAYING) return;
    }

    // Starts an adventure game and resets stage progression
    private void startAdventureMatch() {
        Canvas canvas = App.content().canvas();
        canvas.setCurrentMap(MapType.MEADOW);
        resetStageProgression();
        pendingUpgrades = 0;
        pvpWinner = 0;
        canvas.initCanvas();
        uiPort().addImage(99, "resources/canvaBackround.jpg", 0, 0, 1200, 800, 0, true);
        uiPort().setMap(canvas.getMap());
        syncPlayers(canvas);
        state = GameState.PLAYING;
        uiPort().renderInitials();
        uiPort().log("Adventure started on Meadow.");
    }


    public void cycleShopPage() {
        if (state != GameState.SHOP) return;
        shopPage = 1 - shopPage;
        refreshPlayer();
    }



    // Cycles the selected hero for the active hero selection screen
    public void cycleHero() {
        Canvas canvas = App.content().canvas();
        HeroType[] allHeroes = HeroType.values();
        if (state == GameState.HERO_PLAYER1_SELECT) {
            HeroType next = allHeroes[(canvas.getSelectedHero().ordinal() + 1) % allHeroes.length];
            canvas.setSelectedHero(next);
            uiPort().log("Player 1 hero selected: " + next);
        } else if (state == GameState.HERO_PLAYER2_SELECT) {
            HeroType next = allHeroes[(canvas.getSelectedHero2().ordinal() + 1) % allHeroes.length];
            canvas.setSelectedHero2(next);
            uiPort().log("Player 2 hero selected: " + next);
        }
    }


    public void cycleGameMode() {
        if (state != GameState.GAME_MODE_SELECT) return;
        Canvas canvas = App.content().canvas();
        GameMode[] modes = GameMode.values();
        GameMode next = modes[(canvas.getSelectedGameMode().ordinal() + 1) % modes.length];
        canvas.setSelectedGameMode(next);
        uiPort().log("Game mode selected: " + next);
    }

    // Starts a duel on the selected arena
    private void startPvpMatch(int mapIndex) {
        if (state != GameState.PVP_MAP_SELECT) return;
        MapType[] maps = MapType.values();
        if (mapIndex < 0 || mapIndex >= maps.length) return;

        Canvas canvas = App.content().canvas();
        canvas.setCurrentMap(maps[mapIndex]);
        pendingUpgrades = 0;
        canvas.initCanvas();
        pvpWinner = 0;
        uiPort().addImage(99, "resources/canvaBackround.jpg", 0, 0, 1200, 800, 0, true);
        uiPort().setMap(canvas.getMap());
        syncPlayers(canvas);
        state = GameState.PLAYING;
        uiPort().renderInitials();
        uiPort().log("PvP started on " + maps[mapIndex].displayName + ".");
    }

    public void resetScenario() {
        Canvas canvas = App.content().canvas();
        pvpWinner = 0;
        if (canvas.getSelectedGameMode() != GameMode.PVP) {
            resetStageProgression();
            canvas.setCurrentMap(MapType.MEADOW);
        }
        canvas.initCanvas();
        uiPort().setMap(canvas.getMap());
        syncPlayers(canvas);
        state = GameState.PLAYING;
        uiPort().log("Scenario reset");
    }

    private void syncPlayers(Canvas canvas) {
        uiPort().setMainPlayer(canvas.getMainPlayer());
        uiPort().setMainPlayer2(canvas.getMainPlayer2());
    }



    public void startMoveLeft() {
        MainPlayer player1 = App.content().canvas().getMainPlayer();
        if (player1.getStats().isDead()) return;
        player1.setVelocityX(-player1.getMoveSpeed());
    }

    public void startMoveRight() {
        MainPlayer player1 = App.content().canvas().getMainPlayer();
        if (player1.getStats().isDead()) return;
        player1.setVelocityX(player1.getMoveSpeed());
    }

    public void stopMove() {
        MainPlayer player = App.content().canvas().getMainPlayer();
        if (player != null) player.setVelocityX(0);
    }

    public void playerJump() {
        MainPlayer player1 = App.content().canvas().getMainPlayer();
        if (player1.getStats().isDead()) return;
        player1.jump();
    }



    public void startMoveLeft_p2() {
        MainPlayer player2 = App.content().canvas().getMainPlayer2();
        if (player2 != null && !player2.getStats().isDead()) player2.setVelocityX(-player2.getMoveSpeed());
    }

    public void startMoveRight_p2() {
        MainPlayer player2 = App.content().canvas().getMainPlayer2();
        if (player2 != null && !player2.getStats().isDead()) player2.setVelocityX(player2.getMoveSpeed());
    }

    public void stopMove_p2() {
        MainPlayer player2 = App.content().canvas().getMainPlayer2();
        if (player2 != null) player2.setVelocityX(0);
    }

    public void playerJump_p2() {
        MainPlayer player2 = App.content().canvas().getMainPlayer2();
        if (player2 != null && !player2.getStats().isDead()) player2.jump();
    }

    // Handles player two attack logic for cooperative and duel modes
    public void attackEnemy_p2() {
        if (state != GameState.PLAYING) return;
        Canvas canvas = App.content().canvas();
        MainPlayer player2 = canvas.getMainPlayer2();
        if (player2 == null) return;

        if (player2.getStats().isDead()) return;

        if (!player2.getStats().hasEnergy(player2.getActiveAttack().getMpCost())) {
            uiPort().log("[P2] Not enough MP for " + player2.getActiveAttackName());
            return;
        }

        if (player2.isOnAttackCooldown()) return;

        player2.startAttackAnimation(player2.getActiveAttackName());

        if (canvas.getSelectedGameMode() == GameMode.PVP) {
            MainPlayer player1 = canvas.getMainPlayer();
            if (!player1.getStats().isDead() && player2.useActiveAttack(player1)) {
                uiPort().log("[P2] Hit Player 1 | HP: "
                        + (int) player1.getStats().getHealth() + " / "
                        + (int) player1.getStats().getMaxHealth());
            }
            uiPort().updatePlayer2Position(player2.getX(), player2.getY());
            return;
        }


        for (Enemy enemy : canvas.getEnemies()) {
            if (player2.useActiveAttack(enemy)) {
                uiPort().log("[P2] Attacked " + enemy.getDisplayName() + " | HP: " +
                        (int) enemy.getStats().getHealth() + " / " +
                        (int) enemy.getStats().getMaxHealth());
                uiPort().updatePlayer2Position(player2.getX(), player2.getY());
                return;
            }
        }

        uiPort().updatePlayer2Position(player2.getX(), player2.getY());
    }

    public void attackOrThrow_p2() {
        attackEnemy_p2();
    }

    public void switchAttack_p2(int index) {
        MainPlayer player2 = App.content().canvas().getMainPlayer2();
        if (player2 != null) {
            player2.setActiveAttack(index);
            uiPort().log("[P2] Active attack: " + player2.getActiveAttackName());
        }
    }

    public void cycleAttack_p2() {
        MainPlayer player2 = App.content().canvas().getMainPlayer2();
        if (player2 != null) {
            player2.selectNextAttack();
            uiPort().log("[P2] Active attack: " + player2.getActiveAttackName());
        }
    }

    // Attempts to pick up the closest sword for player two
    public void attemptPickup_p2() {
        Canvas canvas = App.content().canvas();
        MainPlayer player2 = canvas.getMainPlayer2();
        if (player2 == null || player2.hasSword()) return;
        if (player2.getHeroType() != HeroType.WARRIOR) {
            uiPort().log("[P2] Only the warrior can pick up swords.");
            return;
        }
        Sword sword = canvas.getSword();

        if (sword != null && sword.isOnGround()) {
            double distanceToSword = Math.sqrt(Math.pow(player2.getX() - sword.getX(), 2) +
                                    Math.pow(player2.getY() - sword.getY(), 2));
            if (distanceToSword <= MainPlayer.PICKUP_RANGE) {
                player2.pickupSword(sword);
                uiPort().log("[P2] Picked up: " + sword.getName() + " | STR now: " + player2.getStats().getStrength());
                uiPort().updatePlayer2Position(player2.getX(), player2.getY());
                return;
            }
        }

        for (Sword nearbySword : canvas.getExtraSwords()) {
            if (!nearbySword.isOnGround()) continue;
            double distanceToSword = Math.sqrt(Math.pow(player2.getX() - nearbySword.getX(), 2) +
                                    Math.pow(player2.getY() - nearbySword.getY(), 2));
            if (distanceToSword <= MainPlayer.PICKUP_RANGE) {
                player2.pickupSword(nearbySword);
                uiPort().log("[P2] Picked up: " + nearbySword.getName() + " | STR now: " + player2.getStats().getStrength());
                uiPort().updatePlayer2Position(player2.getX(), player2.getY());
                return;
            }
        }
    }

    public void throwSword_p2() {
        MainPlayer player2 = App.content().canvas().getMainPlayer2();
        if (player2 == null || !player2.hasSword()) return;

        Sword dropped = player2.dropSword();
        uiPort().log("[P2] Dropped: " + dropped.getName() + " | STR now: " + player2.getStats().getStrength());
        uiPort().updatePlayer2Position(player2.getX(), player2.getY());
    }




    // Attempts to pick up the closest sword for player one
    public void attemptPickup() {
        Canvas canvas = App.content().canvas();
        MainPlayer player = canvas.getMainPlayer();
        if (player.hasSword()) return;
        if (player.getHeroType() != HeroType.WARRIOR) {
            uiPort().log("Only the warrior can pick up swords.");
            return;
        }
        Sword sword = canvas.getSword();

        if (sword != null && sword.isOnGround()) {
            double distanceToSword = Math.sqrt(Math.pow(player.getX() - sword.getX(), 2) +
                                    Math.pow(player.getY() - sword.getY(), 2));
            if (distanceToSword <= MainPlayer.PICKUP_RANGE) {
                player.pickupSword(sword);
                uiPort().log("Picked up: " + sword.getName() + " | STR now: " + player.getStats().getStrength());
                uiPort().updatePlayerPosition(player.getX(), player.getY());
                return;
            }
        }


        for (Sword nearbySword : canvas.getExtraSwords()) {
            if (!nearbySword.isOnGround()) continue;
            double distanceToSword = Math.sqrt(Math.pow(player.getX() - nearbySword.getX(), 2) +
                                    Math.pow(player.getY() - nearbySword.getY(), 2));
            if (distanceToSword <= MainPlayer.PICKUP_RANGE) {
                player.pickupSword(nearbySword);
                uiPort().log("Picked up: " + nearbySword.getName() + " | STR now: " + player.getStats().getStrength());
                uiPort().updatePlayerPosition(player.getX(), player.getY());
                return;
            }
        }
    }

    public void throwSword() {
        MainPlayer player = App.content().canvas().getMainPlayer();
        if (!player.hasSword()) return;

        Sword dropped = player.dropSword();
        uiPort().log("Dropped: " + dropped.getName() + " | STR now: " + player.getStats().getStrength());
        uiPort().updatePlayerPosition(player.getX(), player.getY());
    }

    // Handles player one attack logic for enemies and duel targets
    public void attackEnemy() {
        if (state != GameState.PLAYING) return;
        Canvas canvas = App.content().canvas();
        MainPlayer player = canvas.getMainPlayer();

        if (player.getStats().isDead()) return;


        if (!player.getStats().hasEnergy(player.getActiveAttack().getMpCost())) {
            uiPort().log("Not enough MP for " + player.getActiveAttackName());
            return;
        }

        if (player.isOnAttackCooldown()) return;

        player.startAttackAnimation(player.getActiveAttackName());

        if (canvas.getSelectedGameMode() == GameMode.PVP) {
            MainPlayer player2 = canvas.getMainPlayer2();
            if (player2 != null && !player2.getStats().isDead()
                    && player.useActiveAttack(player2)) {
                uiPort().log("[P1] Hit Player 2 | HP: "
                        + (int) player2.getStats().getHealth() + " / "
                        + (int) player2.getStats().getMaxHealth());
            }
            uiPort().updatePlayerPosition(player.getX(), player.getY());
            return;
        }


        for (Enemy enemy : canvas.getEnemies()) {
            if (player.useActiveAttack(enemy)) {
                uiPort().log("Attacked " + enemy.getDisplayName() + " | HP: " +
                        (int) enemy.getStats().getHealth() + " / " +
                        (int) enemy.getStats().getMaxHealth());
                uiPort().updatePlayerPosition(player.getX(), player.getY());
                return;
            }
        }

        uiPort().updatePlayerPosition(player.getX(), player.getY());
    }

    public void attackOrThrow() {
        attackEnemy();
    }

    public void switchAttack(int index) {
        MainPlayer player = App.content().canvas().getMainPlayer();
        player.setActiveAttack(index);
        uiPort().log("Active attack: " + player.getActiveAttackName());
    }

    public void cycleAttack() {
        MainPlayer player = App.content().canvas().getMainPlayer();
        player.selectNextAttack();
        uiPort().log("Active attack: " + player.getActiveAttackName());
    }


    // Routes number input according to the current screen
    public void onNumberKey(int index) {
        switch (state) {
            case MAP_SELECT: selectMap(index);    break;
            case PVP_MAP_SELECT: startPvpMatch(index); break;
            case UPGRADE:    applyUpgrade(index); break;
            case SHOP:       buyItem(index);      break;
            case PLAYING:    switchAttack(index); break;
            default:         break;
        }
    }

    // Routes number pad input to player two skill selection
    public void onPlayerTwoNumberKey(int index) {
        if (state == GameState.PLAYING) {
            switchAttack_p2(index);
        }
    }



    // Opens or closes the upgrade menu while preserving player state
    public void toggleUpgradePanel() {
        if (App.content().canvas().getSelectedGameMode() == GameMode.PVP) return;
        if (state == GameState.PLAYING) {
            App.content().canvas().getMainPlayer().setVelocityX(0);
            state = GameState.UPGRADE;
        } else if (state == GameState.UPGRADE) {
            state = GameState.PLAYING;
        }
        refreshPlayer();
    }


    // Applies one selected stat upgrade to player one
    public void applyUpgrade(int index) {
        if (state != GameState.UPGRADE || pendingUpgrades <= 0) return;
        PlayerStats playerStats = App.content().canvas().getMainPlayer().getStats();
        switch (index) {
            case 0: playerStats.increaseMaxHealth(20);            break;
            case 1: playerStats.increaseMaxEnergy(10);            break;
            case 2: playerStats.setStrength(playerStats.getStrength() + 3); break;
            case 3: playerStats.setAgility(playerStats.getAgility() + 3);   break;
            default: return;
        }
        pendingUpgrades--;
        uiPort().log("Upgrade applied. Points left: " + pendingUpgrades);
        refreshPlayer();
    }



    // Opens or closes map selection during adventure modes
    public void toggleMapSelect() {
        if (App.content().canvas().getSelectedGameMode() == GameMode.PVP) return;
        if (state == GameState.PLAYING) {
            App.content().canvas().getMainPlayer().setVelocityX(0);
            state = GameState.MAP_SELECT;
        } else if (state == GameState.MAP_SELECT) {
            state = GameState.PLAYING;
        }
        refreshPlayer();
    }



    // Opens or closes the shop menu during adventure modes
    public void toggleShop() {
        if (App.content().canvas().getSelectedGameMode() == GameMode.PVP) return;
        if (state == GameState.PLAYING) {
            App.content().canvas().getMainPlayer().setVelocityX(0);
            shopPage = 0;
            state = GameState.SHOP;
        } else if (state == GameState.SHOP) {
            state = GameState.PLAYING;
        }
        refreshPlayer();
    }


    // Buys a shop item from the active shop page
    public void buyItem(int index) {
        if (state != GameState.SHOP) return;
        if (shopPage == 0) buyWeapon(index);
        else               buyArmor(index);
    }

    private void buyWeapon(int index) {
        ShopItem[] items = ShopItem.values();
        if (index < 0 || index >= items.length) return;
        ShopItem item = items[index];
        Canvas canvas = App.content().canvas();
        MainPlayer player = canvas.getMainPlayer();
        if (!player.getProgress().spendCoins(item.price)) {
            uiPort().log("Not enough coins for " + item.name + " (need " + item.price + ")");
            return;
        }
        if (player.hasSword()) player.dropSword();
        Sword newSword = new Sword(item.name, item.strBonus, player.getX() + 14, player.getY() + 18);
        canvas.setSword(newSword);
        state = GameState.PLAYING;
        uiPort().log("Bought " + item.name + " (STR +" + item.strBonus + ") — press N to pick up");
        refreshPlayer();
    }

    private void buyArmor(int index) {
        ArmorSet[] sets = ArmorSet.values();
        if (index < 0 || index >= sets.length) return;
        ArmorSet armor = sets[index];
        MainPlayer player = App.content().canvas().getMainPlayer();
        if (!player.getProgress().spendCoins(armor.price)) {
            uiPort().log("Not enough coins for " + armor.name + " (need " + armor.price + ")");
            return;
        }
        PlayerStats playerStats = player.getStats();
        playerStats.increaseMaxHealth(armor.hpBonus);
        playerStats.increaseMaxEnergy(armor.mpBonus);
        playerStats.setStrength(playerStats.getStrength() + armor.strBonus);
        playerStats.increaseDefense(armor.defBonus);
        state = GameState.PLAYING;
        uiPort().log("Equipped " + armor.name + " (+HP+" + armor.hpBonus + " DEF+" + armor.defBonus + ")");
        refreshPlayer();
    }


    // Moves the player to an unlocked map while keeping progress
    public void selectMap(int index) {
        if (state != GameState.MAP_SELECT) return;
        MapType[] allMaps = MapType.values();
        if (index < 0 || index >= allMaps.length) return;
        if (!isMapUnlocked(index)) {
            uiPort().log("Map locked: clear " + allMaps[index - 1].displayName + " first.");
            return;
        }
        Canvas canvas = App.content().canvas();
        canvas.loadMap(allMaps[index]);
        state = GameState.PLAYING;
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());
        uiPort().log("Teleported to map: " + allMaps[index].displayName);
    }

    private void refreshPlayer() {
        MainPlayer player1 = App.content().canvas().getMainPlayer();
        uiPort().updatePlayerPosition(player1.getX(), player1.getY());
    }



    // Updates physics attacks enemies resources and game ending rules
    public void updatePlayer() {

        if (state != GameState.PLAYING) return;

        Canvas canvas = App.content().canvas();
        MainPlayer player1 = canvas.getMainPlayer();
        MainPlayer player2 = canvas.getMainPlayer2();


        if (player1.getStats().isDead()) player1.setVelocityX(0);
        player1.update(canvas.getMap().getRectangles());
        player1.updateAttackAnimation();


        if (player2 != null) {
            if (player2.getStats().isDead()) player2.setVelocityX(0);
            player2.update(canvas.getMap().getRectangles());
            player2.updateAttackAnimation();
        }

        if (canvas.getSelectedGameMode() != GameMode.PVP) {
            updateEnemies(canvas);
            handleRespawn(canvas);
        }


        player1.getStats().restoreEnergy(MP_REGEN_PER_TICK);
        if (player2 != null) {
            player2.getStats().restoreEnergy(MP_REGEN_PER_TICK);
        }


        boolean p1Dead = player1.getStats().isDead();
        boolean p2Dead = player2 != null && player2.getStats().isDead();

        if (canvas.getSelectedGameMode() == GameMode.PVP && (p1Dead || p2Dead)) {
            pvpWinner = p1Dead == p2Dead ? 0 : (p1Dead ? 2 : 1);
            state = GameState.GAME_OVER;
            uiPort().log(pvpWinner == 0 ? "PvP ended in a draw."
                    : "Player " + pvpWinner + " wins!");
        } else {
            boolean allDead = (player2 == null) ? p1Dead : (p1Dead && p2Dead);
            if (allDead) {
                state = GameState.GAME_OVER;
                uiPort().log("Game Over");
            }
        }

        uiPort().updatePlayerPosition(player1.getX(), player1.getY());
        if (player2 != null) {
            uiPort().updatePlayer2Position(player2.getX(), player2.getY());
        }
    }

    // Updates enemy movement target selection attacks and removal
    private void updateEnemies(Canvas canvas) {
        MainPlayer player1 = canvas.getMainPlayer();
        MainPlayer player2 = canvas.getMainPlayer2();
        java.util.List<team.model.MapRect> platforms = canvas.getMap().getRectangles();
        Iterator<Enemy> iterator = canvas.getEnemies().iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            MainPlayer target = chooseClosestTarget(player1, player2, enemy);
            enemy.updateAi(target != null ? target : player1);
            enemy.update(platforms);
            enemy.updateCooldown();


            if (target != null && enemy.canAttack(target, Enemy.ATTACK_RANGE)) {
                enemy.attackPlayer(target);
            }

            enemy.updateDeathAnimation();
            if (enemy.shouldDisappear()) {
                rewardForKill(target != null ? target : player1, enemy);
                iterator.remove();
            }
        }
    }

    // Chooses the nearest living player as an enemy target
    private MainPlayer chooseClosestTarget(MainPlayer player1, MainPlayer player2, Enemy enemy) {
        if (player2 == null || player2.getStats().isDead()) {
            return player1;
        }
        if (player1.getStats().isDead()) {
            return player2;
        }

        double distance1 = distanceBetween(enemy.getX(), enemy.getY(), player1.getX(), player1.getY());
        double distance2 = distanceBetween(enemy.getX(), enemy.getY(), player2.getX(), player2.getY());
        return distance1 <= distance2 ? player1 : player2;
    }

    private double distanceBetween(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }


    // Keeps the map populated with enemies based on the current stage
    private void handleRespawn(Canvas canvas) {
        int enemyLimit = (canvas.getCurrentMap() == MapType.BOSS_ARENA) ? 1 : MAX_ENEMIES;
        if (canvas.getEnemies().size() >= enemyLimit) {
            respawnTimer = RESPAWN_DELAY_TICKS;
            return;
        }
        if (--respawnTimer <= 0) {
            EnemyType[] enemySpawnPool = canvas.getCurrentMap().enemyTypes;
            EnemyType enemyType = enemySpawnPool[(int) (Math.random() * enemySpawnPool.length)];
            double x = 200 + Math.random() * 800;
            canvas.spawnEnemy(nextEnemyId++, x, 430, enemyType);
            respawnTimer = RESPAWN_DELAY_TICKS;
            uiPort().log("Respawned " + enemyType.displayName);
        }
    }


    // Grants coins experience and boss loot after an enemy disappears
    private void rewardForKill(MainPlayer player, Enemy enemy) {
        player.getProgress().addCoins(enemy.getType().coinReward);
        grantXp(player, enemy.getType().xpReward);
        recordStageKill(App.content().canvas());
        if (enemy.getType() == EnemyType.FINAL_BOSS) {
            dropBossLoot(App.content().canvas(), enemy);
        }
    }

    // Records stage clear progress and unlocks the next map
    private void recordStageKill(Canvas canvas) {
        int stageIndex = canvas.getCurrentMap().ordinal();
        if (stageIndex != highestUnlockedMapIndex) return;

        int requiredKills;
        if (canvas.getCurrentMap() == MapType.BOSS_ARENA) {
            requiredKills = 1;
        } else if (canvas.getSelectedGameMode() == GameMode.MULTIPLAYER) {
            requiredKills = MULTIPLAYER_ENEMIES_TO_CLEAR_STAGE;
        } else {
            requiredKills = SOLO_ENEMIES_TO_CLEAR_STAGE;
        }
        if (stageKills[stageIndex] >= requiredKills) return;
        stageKills[stageIndex]++;

        if (stageKills[stageIndex] < requiredKills) {
            uiPort().log("Stage progress: " + stageKills[stageIndex] + "/" + requiredKills
                    + " enemies defeated.");
            return;
        }

        MapType[] maps = MapType.values();
        if (stageIndex + 1 < maps.length) {
            highestUnlockedMapIndex = stageIndex + 1;
            uiPort().log("STAGE CLEARED! " + maps[highestUnlockedMapIndex].displayName
                    + " is now unlocked.");
        } else {
            uiPort().log("FINAL STAGE CLEARED! You completed every stage.");
        }
    }

    // Resets adventure map unlocks and kill counters
    private void resetStageProgression() {
        highestUnlockedMapIndex = 0;
        for (int i = 0; i < stageKills.length; i++) {
            stageKills[i] = 0;
        }
    }


    // Drops legendary swords around the defeated boss
    private void dropBossLoot(Canvas canvas, Enemy boss) {
        double bossX = boss.getX(), bossY = boss.getY();
        canvas.addExtraSword(new Sword("Soul Sever",   120, bossX - 300, bossY));
        canvas.addExtraSword(new Sword("Chaos Blade",  110, bossX - 150, bossY));
        canvas.addExtraSword(new Sword("Wraith Edge",  100, bossX,       bossY));
        canvas.addExtraSword(new Sword("Bone Crusher",  95, bossX + 150, bossY));
        canvas.addExtraSword(new Sword("Eternal Lance", 90, bossX + 300, bossY));
        uiPort().log("BOSS DEFEATED! 5 legendary weapons dropped — press N to pick up!");
    }


    // Adds experience and handles level rewards
    private void grantXp(MainPlayer player, int amount) {
        int levelsGained = player.getProgress().addXp(amount);
        if (levelsGained > 0) {
            player.getStats().increaseMaxEnergy(MP_GAIN_PER_LEVEL * levelsGained);

            player.getStats().heal(player.getStats().getMaxHealth());
            player.getStats().restoreEnergy(player.getStats().getMaxEnergy());

            pendingUpgrades += levelsGained;
            uiPort().log("LEVEL UP! Level " + player.getProgress().getLevel()
                    + " | Max MP +" + (int) (MP_GAIN_PER_LEVEL * levelsGained)
                    + " — press C to spend " + pendingUpgrades + " point(s)");
        }
    }
}
