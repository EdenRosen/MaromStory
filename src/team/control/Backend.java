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

public class Backend {

    private static final double MP_REGEN_PER_TICK = 0.2;   // ~6.6 MP לשנייה (30ms tick)
    private static final int MAX_ENEMIES         = 3;      // כמות האויבים שנשמרת על המפה
    private static final int RESPAWN_DELAY_TICKS = 90;     // ~2.7 שניות בין ריספאונים

    private GameState state = GameState.START_SCREEN;
    private int respawnTimer    = RESPAWN_DELAY_TICKS;
    private int nextEnemyId     = 100;                     // מזהים ייחודיים לאויבי ריספאון
    private int pendingUpgrades = 0;                       // שדרוגי רמה שממתינים לבחירת השחקן
    private int shopPage        = 0;                       // 0=weapons, 1=armor

    private UiPort uiPort() {
        return UiPort.getInstance();
    }

    public GameState getState()    { return state; }
    public boolean isGameStarted() { return state != GameState.START_SCREEN; }
    public boolean isGameOver()    { return state == GameState.GAME_OVER; }
    public boolean isUpgrade()     { return state == GameState.UPGRADE; }
    public boolean isMapSelect()   { return state == GameState.MAP_SELECT; }
    public boolean isShop()        { return state == GameState.SHOP; }
    public int     getShopPage()   { return shopPage; }
    public int getPendingUpgrades(){ return pendingUpgrades; }

    // --- אתחול ---

    // נקרא אוטומטית בעליית האפליקציה — מכין את הנתונים בלבד, לא מתחיל משחק
    public void initializeApp() {
        Canvas canvas = App.content().canvas();
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());
        uiPort().log("App initialized — waiting for player to press Enter");
    }

    // נקרא כשהשחקן לוחץ Enter
    //   ממסך פתיחה  → מסך בחירת מצב (Solo/Multiplayer)
    //   מ-Game Mode → מתחיל משחק
    //   מ-Game Over → חזרה למסך פתיחה
    //   תוך כדי משחק → מתעלם
    public void startScenario() {
        if (state == GameState.GAME_OVER) {
            state = GameState.START_SCREEN;
            Canvas canvas = App.content().canvas();
            canvas.initCanvas();
            uiPort().setMap(canvas.getMap());
            syncPlayers(canvas);
            return;
        }
        if (state == GameState.START_SCREEN) {
            // המעבר הבא הוא לבחירת מצב (Solo / Multiplayer)
            state = GameState.GAME_MODE_SELECT;
            uiPort().log("Select game mode: Solo or Multiplayer");
            return;
        }
        if (state == GameState.GAME_MODE_SELECT) {
            // כעת התחל משחק בפועל
            Canvas canvas = App.content().canvas();
            uiPort().addImage(99, "resources/canvaBackround.jpg", 0, 0, 1200, 800, 0, true);
            uiPort().setMap(canvas.getMap());
            syncPlayers(canvas);
            state = GameState.PLAYING;
            uiPort().renderInitials();
            uiPort().log("Scenario started: MaromQuest");
            return;
        }
        if (state == GameState.PLAYING) return;  // בעוד המשחק רץ — אל תעשה כלום
    }

    // החלפת טאב בחנות — TAB מחליף בין נשק לשריון
    public void cycleShopPage() {
        if (state != GameState.SHOP) return;
        shopPage = 1 - shopPage;
        refreshPlayer();
    }

    // בחירת גיבור — פעיל רק במסך הפתיחה. מחליף את הסוג ובונה מחדש כדי
    // שמסך הפתיחה יציג את הגיבור הנבחר ואת הסקילים שלו.
    public void cycleHero() {
        if (state != GameState.START_SCREEN) return;
        Canvas canvas = App.content().canvas();
        HeroType[] all = HeroType.values();
        HeroType next = all[(canvas.getSelectedHero().ordinal() + 1) % all.length];
        canvas.setSelectedHero(next);
        canvas.initCanvas();
        uiPort().setMap(canvas.getMap());
        syncPlayers(canvas);
        uiPort().log("Hero selected: " + next);
    }

    // בחירת מצב משחק — Solo או Multiplayer. פעיל רק במסך בחירת מצב
    public void cycleGameMode() {
        if (state != GameState.GAME_MODE_SELECT) return;
        Canvas canvas = App.content().canvas();
        GameMode next = canvas.getSelectedGameMode() == GameMode.SOLO 
            ? GameMode.MULTIPLAYER 
            : GameMode.SOLO;
        canvas.setSelectedGameMode(next);
        canvas.initCanvas();
        uiPort().setMap(canvas.getMap());
        syncPlayers(canvas);
        uiPort().log("Game mode selected: " + next);
    }

    public void resetScenario() {
        Canvas canvas = App.content().canvas();
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

    // --- פקודות תנועה (מגיעות מ-Router בלבד, אין כאן שום ידיעה על מקשים) ---

    public void startMoveLeft() {
        MainPlayer p = App.content().canvas().getMainPlayer();
        if (p.getStats().isDead()) return;
        p.setVelocityX(-p.getMoveSpeed());
    }

    public void startMoveRight() {
        MainPlayer p = App.content().canvas().getMainPlayer();
        if (p.getStats().isDead()) return;
        p.setVelocityX(p.getMoveSpeed());
    }

    public void stopMove() {
        MainPlayer player = App.content().canvas().getMainPlayer();
        if (player != null) player.setVelocityX(0);
    }

    public void playerJump() {
        MainPlayer p = App.content().canvas().getMainPlayer();
        if (p.getStats().isDead()) return;
        p.jump();
    }

    // --- Player 2 Control (Multiplayer Mode) ---

    public void startMoveLeft_p2() {
        MainPlayer p2 = App.content().canvas().getMainPlayer2();
        if (p2 != null && !p2.getStats().isDead()) p2.setVelocityX(-p2.getMoveSpeed());
    }

    public void startMoveRight_p2() {
        MainPlayer p2 = App.content().canvas().getMainPlayer2();
        if (p2 != null && !p2.getStats().isDead()) p2.setVelocityX(p2.getMoveSpeed());
    }

    public void stopMove_p2() {
        MainPlayer p2 = App.content().canvas().getMainPlayer2();
        if (p2 != null) p2.setVelocityX(0);
    }

    public void playerJump_p2() {
        MainPlayer p2 = App.content().canvas().getMainPlayer2();
        if (p2 != null && !p2.getStats().isDead()) p2.jump();
    }

    public void attackEnemy_p2() {
        if (state != GameState.PLAYING) return;
        Canvas canvas = App.content().canvas();
        MainPlayer p2 = canvas.getMainPlayer2();
        if (p2 == null) return;

        if (p2.getStats().isDead()) return;   // שחקן שנפל לא תוקף

        if (!p2.getStats().hasEnergy(p2.getActiveAttack().getMpCost())) {
            uiPort().log("[P2] Not enough MP for " + p2.getActiveAttackName());
            return;
        }

        if (p2.isOnAttackCooldown()) return;  // עדיין ב-cooldown — אין תקיפה ואין אנימציה

        p2.startAttackAnimation(p2.getActiveAttackName());

        // Co-op: שחקנים לא פוגעים זה בזה — רק באויבים
        for (Enemy enemy : canvas.getEnemies()) {
            if (p2.useActiveAttack(enemy)) {
                uiPort().log("[P2] Attacked " + enemy.getDisplayName() + " | HP: " +
                        (int) enemy.getStats().getHealth() + " / " +
                        (int) enemy.getStats().getMaxHealth());
                uiPort().updatePlayer2Position(p2.getX(), p2.getY());
                return;
            }
        }

        uiPort().updatePlayer2Position(p2.getX(), p2.getY());
    }

    public void attackOrThrow_p2() {
        attackEnemy_p2();
        throwSword_p2();
    }

    public void switchAttack_p2(int index) {
        MainPlayer p2 = App.content().canvas().getMainPlayer2();
        if (p2 != null) {
            p2.setActiveAttack(index);
            uiPort().log("[P2] Active attack: " + p2.getActiveAttackName());
        }
    }

    public void cycleAttack_p2() {
        MainPlayer p2 = App.content().canvas().getMainPlayer2();
        if (p2 != null) {
            p2.selectNextAttack();
            uiPort().log("[P2] Active attack: " + p2.getActiveAttackName());
        }
    }

    public void attemptPickup_p2() {
        MainPlayer player2 = App.content().canvas().getMainPlayer2();
        if (player2 == null || player2.hasSword()) return;

        Canvas canvas = App.content().canvas();
        Sword sword = canvas.getSword();

        if (sword != null && sword.isOnGround()) {
            double dist = Math.sqrt(Math.pow(player2.getX() - sword.getX(), 2) +
                                    Math.pow(player2.getY() - sword.getY(), 2));
            if (dist <= MainPlayer.PICKUP_RANGE) {
                player2.pickupSword(sword);
                uiPort().log("[P2] Picked up: " + sword.getName() + " | STR now: " + player2.getStats().getStrength());
                uiPort().updatePlayer2Position(player2.getX(), player2.getY());
                return;
            }
        }

        for (Sword s : canvas.getExtraSwords()) {
            if (!s.isOnGround()) continue;
            double dist = Math.sqrt(Math.pow(player2.getX() - s.getX(), 2) +
                                    Math.pow(player2.getY() - s.getY(), 2));
            if (dist <= MainPlayer.PICKUP_RANGE) {
                player2.pickupSword(s);
                uiPort().log("[P2] Picked up: " + s.getName() + " | STR now: " + player2.getStats().getStrength());
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

    // --- הרמה וזריקה של חרב לפי Sequence Diagram ---

    // checkItemAvailability + updateInventoryData (שלבים 3-4 בתרשים)
    public void attemptPickup() {
        MainPlayer player = App.content().canvas().getMainPlayer();
        if (player.hasSword()) return;

        Canvas canvas = App.content().canvas();
        Sword sword = canvas.getSword();

        if (sword != null && sword.isOnGround()) {
            double dist = Math.sqrt(Math.pow(player.getX() - sword.getX(), 2) +
                                    Math.pow(player.getY() - sword.getY(), 2));
            if (dist <= MainPlayer.PICKUP_RANGE) {
                player.pickupSword(sword);
                uiPort().log("Picked up: " + sword.getName() + " | STR now: " + player.getStats().getStrength());
                uiPort().updatePlayerPosition(player.getX(), player.getY());
                return;
            }
        }

        // בדיקת חפצים נוספים (טיפות Boss וכו')
        for (Sword s : canvas.getExtraSwords()) {
            if (!s.isOnGround()) continue;
            double dist = Math.sqrt(Math.pow(player.getX() - s.getX(), 2) +
                                    Math.pow(player.getY() - s.getY(), 2));
            if (dist <= MainPlayer.PICKUP_RANGE) {
                player.pickupSword(s);
                uiPort().log("Picked up: " + s.getName() + " | STR now: " + player.getStats().getStrength());
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

    public void attackEnemy() {
        if (state != GameState.PLAYING) return;   // אין תקיפה במסך שדרוג / Game Over
        Canvas canvas = App.content().canvas();
        MainPlayer player = canvas.getMainPlayer();

        if (player.getStats().isDead()) return;   // שחקן שנפל לא תוקף

        // אם אין מספיק MP לסקיל הפעיל — אין תקיפה וגם אין אנימציה
        if (!player.getStats().hasEnergy(player.getActiveAttack().getMpCost())) {
            uiPort().log("Not enough MP for " + player.getActiveAttackName());
            return;
        }

        if (player.isOnAttackCooldown()) return;  // עדיין ב-cooldown — אין תקיפה ואין אנימציה

        player.startAttackAnimation(player.getActiveAttackName());

        // Co-op: שחקנים לא פוגעים זה בזה — רק באויבים
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
        throwSword();
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

    // מקשי המספרים — תלוי-מצב: בחירת מפה / בחירת שדרוג / חנות / החלפת סקיל
    public void onNumberKey(int index) {
        switch (state) {
            case MAP_SELECT: selectMap(index);    break;
            case UPGRADE:    applyUpgrade(index); break;
            case SHOP:       buyItem(index);      break;
            case PLAYING:    switchAttack(index); break;
            default:         break;
        }
    }

    // --- פאנל שדרוג נקודות (נפתח/נסגר ב-C) ---

    public void toggleUpgradePanel() {
        if (state == GameState.PLAYING) {
            App.content().canvas().getMainPlayer().setVelocityX(0);
            state = GameState.UPGRADE;
        } else if (state == GameState.UPGRADE) {
            state = GameState.PLAYING;
        }
        refreshPlayer();
    }

    // החלת שדרוג נבחר (מוציא נקודת שדרוג אחת מהמאגר)
    public void applyUpgrade(int index) {
        if (state != GameState.UPGRADE || pendingUpgrades <= 0) return;
        PlayerStats s = App.content().canvas().getMainPlayer().getStats();
        switch (index) {
            case 0: s.increaseMaxHealth(20);            break;  // +20 HP מקסימלי
            case 1: s.increaseMaxEnergy(10);            break;  // +10 MP מקסימלי
            case 2: s.setStrength(s.getStrength() + 3); break;  // +3 כוח
            case 3: s.setAgility(s.getAgility() + 2);   break;  // +2 זריזות (מהירות וקפיצה)
            default: return;
        }
        pendingUpgrades--;
        uiPort().log("Upgrade applied. Points left: " + pendingUpgrades);
        refreshPlayer();
    }

    // --- תפריט בחירת מפה (נפתח/נסגר ב-M) ---

    public void toggleMapSelect() {
        if (state == GameState.PLAYING) {
            App.content().canvas().getMainPlayer().setVelocityX(0);
            state = GameState.MAP_SELECT;
        } else if (state == GameState.MAP_SELECT) {
            state = GameState.PLAYING;
        }
        refreshPlayer();
    }

    // --- חנות (נפתחת/נסגרת ב-B) ---

    public void toggleShop() {
        if (state == GameState.PLAYING) {
            App.content().canvas().getMainPlayer().setVelocityX(0);
            shopPage = 0;
            state = GameState.SHOP;
        } else if (state == GameState.SHOP) {
            state = GameState.PLAYING;
        }
        refreshPlayer();
    }

    // קניית פריט לפי אינדקס — מנכה מטבעות, שם חרב על הקרקע ליד השחקן
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
        PlayerStats s = player.getStats();
        s.increaseMaxHealth(armor.hpBonus);
        s.increaseMaxEnergy(armor.mpBonus);
        s.setStrength(s.getStrength() + armor.strBonus);
        s.increaseDefense(armor.defBonus);
        state = GameState.PLAYING;
        uiPort().log("Equipped " + armor.name + " (+HP+" + armor.hpBonus + " DEF+" + armor.defBonus + ")");
        refreshPlayer();
    }

    // בחירת מפה לפי אינדקס מהתפריט — טלפורט תוך שמירת התקדמות
    public void selectMap(int index) {
        if (state != GameState.MAP_SELECT) return;
        MapType[] all = MapType.values();
        if (index < 0 || index >= all.length) return;
        Canvas canvas = App.content().canvas();
        canvas.loadMap(all[index]);
        state = GameState.PLAYING;
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());
        uiPort().log("Teleported to map: " + all[index].displayName);
    }

    private void refreshPlayer() {
        MainPlayer p = App.content().canvas().getMainPlayer();
        uiPort().updatePlayerPosition(p.getX(), p.getY());
    }

    // --- עדכון תקופתי (נקרא מ-MyPeriodicLoop כל tick) ---

    public void updatePlayer() {
        // קופא במסך הפתיחה וב-Game Over — אין פיזיקה / AI
        if (state != GameState.PLAYING) return;

        Canvas canvas = App.content().canvas();
        MainPlayer player1 = canvas.getMainPlayer();
        MainPlayer player2 = canvas.getMainPlayer2();

        // עדכון Player 1 — שחקן שנפל לא זז (מונע "החלקת גופה")
        if (player1.getStats().isDead()) player1.setVelocityX(0);
        player1.update(canvas.getMap().getRectangles());
        player1.updateAttackAnimation();

        // עדכון Player 2 (אם קיים במצב Multiplayer)
        if (player2 != null) {
            if (player2.getStats().isDead()) player2.setVelocityX(0);
            player2.update(canvas.getMap().getRectangles());
            player2.updateAttackAnimation();
        }

        updateEnemies(canvas);
        handleRespawn(canvas);

        // US-5 — התחדשות MP הדרגתית בכל tick (עד למקסימום)
        player1.getStats().restoreEnergy(MP_REGEN_PER_TICK);
        if (player2 != null) {
            player2.getStats().restoreEnergy(MP_REGEN_PER_TICK);
        }

        // US-2 — Game Over: ב-Solo כשהשחקן מת; ב-Co-op רק כששני השחקנים נפלו
        boolean p1Dead = player1.getStats().isDead();
        boolean allDead = (player2 == null) ? p1Dead : (p1Dead && player2.getStats().isDead());

        if (allDead) {
            state = GameState.GAME_OVER;
            uiPort().log("Game Over");
        }

        uiPort().updatePlayerPosition(player1.getX(), player1.getY());
        if (player2 != null) {
            uiPort().updatePlayer2Position(player2.getX(), player2.getY());
        }
    }

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

            // US-1 — אויב בטווח מכה את השחקן הקרוב ביותר (עם cooldown בין מכות)
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

    // ריספאון — שומר על MAX_ENEMIES אויבים על המפה (בBoss Arena — מקסימום 1)
    private void handleRespawn(Canvas canvas) {
        int limit = (canvas.getCurrentMap() == MapType.BOSS_ARENA) ? 1 : MAX_ENEMIES;
        if (canvas.getEnemies().size() >= limit) {
            respawnTimer = RESPAWN_DELAY_TICKS;
            return;
        }
        if (--respawnTimer <= 0) {
            EnemyType[] pool = canvas.getCurrentMap().enemyTypes;   // אויבים לפי המפה הנוכחית
            EnemyType type = pool[(int) (Math.random() * pool.length)];
            double x = 200 + Math.random() * 800;   // מיקום אקראי על המפה
            canvas.spawnEnemy(nextEnemyId++, x, 430, type);
            respawnTimer = RESPAWN_DELAY_TICKS;
            uiPort().log("Respawned " + type.displayName);
        }
    }

    // תגמול על חיסול אויב — מטבעות ו-XP לפי סוג האויב (אויב חזק = יותר XP)
    private void rewardForKill(MainPlayer player, Enemy enemy) {
        player.getProgress().addCoins(enemy.getType().coinReward);
        grantXp(player, enemy.getType().xpReward);
        if (enemy.getType() == EnemyType.FINAL_BOSS) {
            dropBossLoot(App.content().canvas(), enemy);
        }
    }

    // הFINAL BOSS מוריד חמישה נשקים אגדיים על הקרקע
    private void dropBossLoot(Canvas canvas, Enemy boss) {
        double bx = boss.getX(), by = boss.getY();
        canvas.addExtraSword(new Sword("Soul Sever",   120, bx - 300, by));
        canvas.addExtraSword(new Sword("Chaos Blade",  110, bx - 150, by));
        canvas.addExtraSword(new Sword("Wraith Edge",  100, bx,       by));
        canvas.addExtraSword(new Sword("Bone Crusher",  95, bx + 150, by));
        canvas.addExtraSword(new Sword("Eternal Lance", 90, bx + 300, by));
        uiPort().log("BOSS DEFEATED! 5 legendary weapons dropped — press N to pick up!");
    }

    // הוספת XP וטיפול בעליית רמה — צובר נקודות שדרוג (לפתיחה ידנית ב-C)
    private void grantXp(MainPlayer player, int amount) {
        int levelsGained = player.getProgress().addXp(amount);
        if (levelsGained > 0) {
            // פרס בסיסי: ריפוי מלא + מילוי MP
            player.getStats().heal(player.getStats().getMaxHealth());
            player.getStats().restoreEnergy(player.getStats().getMaxEnergy());
            // צבירת נקודות שדרוג — השחקן פותח את הפאנל ב-C כדי לשפר
            pendingUpgrades += levelsGained;
            uiPort().log("LEVEL UP! Level " + player.getProgress().getLevel()
                    + " — press C to spend " + pendingUpgrades + " point(s)");
        }
    }
}
