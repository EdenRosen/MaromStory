package team.control;

import java.util.Iterator;
import my_base.App;
import shared.ui_ports.UiPort;
import team.model.Canvas;
import team.model.Enemy;
import team.model.MainPlayer;
import team.model.Sword;

public class Backend {

    private boolean gameStarted = false;

    private UiPort uiPort() {
        return UiPort.getInstance();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    // --- אתחול ---

    // נקרא אוטומטית בעליית האפליקציה — מכין את הנתונים בלבד, לא מתחיל משחק
    public void initializeApp() {
        Canvas canvas = App.content().canvas();
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());
        uiPort().log("App initialized — waiting for player to press Enter");
    }

    // נקרא כשהשחקן לוחץ Enter — מתחיל את המשחק בפועל
    public void startScenario() {
        if (gameStarted) return;
        Canvas canvas = App.content().canvas();

        uiPort().addImage(99, "resources/canvaBackround.jpg", 0, 0, 1200, 800, 0, true);
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());

        gameStarted = true;

        // שלב 4 בתרשים רצף — Game System מודיע ל-UI לצייר את מצב הפתיחה
        uiPort().renderInitials();

        uiPort().log("Scenario started: MapleQuest");
    }

    public void resetScenario() {
        Canvas canvas = App.content().canvas();
        canvas.initCanvas();
        uiPort().setMap(canvas.getMap());
        uiPort().setMainPlayer(canvas.getMainPlayer());
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

        player.startAttackAnimation(player.getActiveAttackName());

        for (Enemy enemy : canvas.getEnemies()) {
            if (player.useActiveAttack(enemy)) {
                uiPort().log("Attacked " + enemy.getType() + " | HP: " +
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
        Canvas canvas = App.content().canvas();
        MainPlayer player = canvas.getMainPlayer();
        player.update(canvas.getMap().getRectangles());
        player.updateAttackAnimation();
        updateEnemies(canvas);
        uiPort().updatePlayerPosition(player.getX(), player.getY());

        // עדכון כל האויבים — פיזיקה
    }

    private void updateEnemies(Canvas canvas) {
        MainPlayer player = canvas.getMainPlayer();
        java.util.List<team.model.MapRect> platforms = canvas.getMap().getRectangles();
        Iterator<Enemy> iterator = canvas.getEnemies().iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.updateAi(player);
            enemy.update(platforms);
            enemy.updateDeathAnimation();
            if (enemy.shouldDisappear()) {
                iterator.remove();
            }
        }
    }
}
