package team.control;

import java.util.Iterator;
import my_base.App;
import shared.ui_ports.UiPort;
import team.model.Canvas;
import team.model.Enemy;
import team.model.HeroType;
import team.model.MainPlayer;
import team.model.Sword;

public class Backend {

    private GameState state = GameState.START_SCREEN;

    private UiPort uiPort() {
        return UiPort.getInstance();
    }

    public GameState getState()    { return state; }
    public boolean isGameStarted() { return state != GameState.START_SCREEN; }
    public boolean isGameOver()    { return state == GameState.GAME_OVER; }

    // --- אתחול ---

    // נקרא אוטומטית בעליית האפליקציה — מכין את הנתונים בלבד, לא מתחיל משחק
    public void initializeApp() {
        Canvas canvas = App.content().canvas();
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());
        uiPort().log("App initialized — waiting for player to press Enter");
    }

    // נקרא כשהשחקן לוחץ Enter
    //   ממסך פתיחה  → מתחיל משחק
    //   מ-Game Over → מאתחל ומתחיל מחדש
    //   תוך כדי משחק → מתעלם
    public void startScenario() {
        if (state == GameState.GAME_OVER) { resetScenario(); return; }
        if (state == GameState.PLAYING) return;

        Canvas canvas = App.content().canvas();

        uiPort().addImage(99, "resources/canvaBackround.jpg", 0, 0, 1200, 800, 0, true);
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());

        state = GameState.PLAYING;

        // שלב 4 בתרשים רצף — Game System מודיע ל-UI לצייר את מצב הפתיחה
        uiPort().renderInitials();

        uiPort().log("Scenario started: MapleQuest");
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
        uiPort().setMainPlayer(canvas.getMainPlayer());
        uiPort().log("Hero selected: " + next);
    }

    public void resetScenario() {
        Canvas canvas = App.content().canvas();
        canvas.initCanvas();
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());
        state = GameState.PLAYING;
        uiPort().log("Scenario reset");
    }

    // --- פקודות תנועה (מגיעות מ-Router בלבד, אין כאן שום ידיעה על מקשים) ---

    public void startMoveLeft() {
        App.content().canvas().getMainPlayer().setVelocityX(-MainPlayer.MOVE_SPEED);
    }

    public void startMoveRight() {
        App.content().canvas().getMainPlayer().setVelocityX(MainPlayer.MOVE_SPEED);
    }

    public void stopMove() {
        App.content().canvas().getMainPlayer().setVelocityX(0);
    }

    public void playerJump() {
        App.content().canvas().getMainPlayer().jump();
    }

    // --- הרמה וזריקה של חרב לפי Sequence Diagram ---

    // checkItemAvailability + updateInventoryData (שלבים 3-4 בתרשים)
    public void attemptPickup() {
        MainPlayer player = App.content().canvas().getMainPlayer();
        Sword sword = App.content().canvas().getSword();

        if (sword == null || !sword.isOnGround() || player.hasSword()) return;

        // בדיקת מרחק — checkItemAvailability
        double dist = Math.sqrt(Math.pow(player.getX() - sword.getX(), 2) +
                                Math.pow(player.getY() - sword.getY(), 2));

        if (dist <= MainPlayer.PICKUP_RANGE) {
            // updateInventoryData — מרים ומעדכן stats
            player.pickupSword(sword);
            uiPort().log("Picked up: " + sword.getName() + " | STR now: " + player.getStats().getStrength());
            // updateDisplay — מצייר מחדש (שלב 6 בתרשים)
            uiPort().updatePlayerPosition(player.getX(), player.getY());
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
        Canvas canvas = App.content().canvas();
        MainPlayer player = canvas.getMainPlayer();

        // אם אין מספיק MP לסקיל הפעיל — אין תקיפה וגם אין אנימציה
        if (!player.getStats().hasEnergy(player.getActiveAttack().getMpCost())) {
            uiPort().log("Not enough MP for " + player.getActiveAttackName());
            return;
        }

        player.startAttackAnimation(player.getActiveAttackName());

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

    public void switchAttack(int index) {
        MainPlayer player = App.content().canvas().getMainPlayer();
        player.setActiveAttack(index);
        uiPort().log("Active attack: " + player.getActiveAttackName());
    }

    // --- עדכון תקופתי (נקרא מ-MyPeriodicLoop כל tick) ---

    public void updatePlayer() {
        // קופא במסך הפתיחה וב-Game Over — אין פיזיקה / AI
        if (state != GameState.PLAYING) return;

        Canvas canvas = App.content().canvas();
        MainPlayer player = canvas.getMainPlayer();
        player.update(canvas.getMap().getRectangles());
        player.updateAttackAnimation();
        updateEnemies(canvas);

        // US-2 — מות השחקן מעביר את המערכת ל-Game Over
        if (player.getStats().isDead()) {
            state = GameState.GAME_OVER;
            uiPort().log("Game Over");
        }

        uiPort().updatePlayerPosition(player.getX(), player.getY());
    }

    private void updateEnemies(Canvas canvas) {
        MainPlayer player = canvas.getMainPlayer();
        java.util.List<team.model.MapRect> platforms = canvas.getMap().getRectangles();
        Iterator<Enemy> iterator = canvas.getEnemies().iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.updateAi(player);
            enemy.update(platforms);
            enemy.updateCooldown();

            // US-1 — אויב בטווח מכה את השחקן (עם cooldown בין מכות)
            if (enemy.canAttack(player, Enemy.ATTACK_RANGE)) {
                enemy.attackPlayer(player);
            }

            enemy.updateDeathAnimation();
            if (enemy.shouldDisappear()) {
                rewardForKill(player);
                iterator.remove();
            }
        }
    }

    // תגמול על חיסול אויב — מטבעות ו-XP, ועליית רמה במידת הצורך
    private static final int COIN_REWARD = 10;
    private static final int XP_REWARD   = 25;

    private void rewardForKill(MainPlayer player) {
        player.getProgress().addCoins(COIN_REWARD);
        int levelsGained = player.getProgress().addXp(XP_REWARD);

        if (levelsGained > 0) {
            // פרס עלייה ברמה: חיזוק כוח + ריפוי מלא + מילוי MP
            player.getStats().setStrength(player.getStats().getStrength() + 2 * levelsGained);
            player.getStats().heal(player.getStats().getMaxHealth());
            player.getStats().restoreEnergy(player.getStats().getMaxEnergy());
            uiPort().log("LEVEL UP! Now level " + player.getProgress().getLevel());
        }
    }
}
