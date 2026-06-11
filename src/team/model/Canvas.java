package team.model;

import java.util.ArrayList;
import java.util.List;

public class Canvas {

    private Map map;
    private MainPlayer mainPlayer;
    private Sword sword;
    private List<Enemy> enemies;
    private int nextEnemyId = 100;   // מזהים ייחודיים ל-spawn דינמי (spot)

    // הגיבור הנבחר — נשמר בין אתחולים (reset) ונקבע במסך הפתיחה
    private HeroType selectedHero = HeroType.WARRIOR;

    public HeroType getSelectedHero()          { return selectedHero; }
    public void setSelectedHero(HeroType hero)  { this.selectedHero = hero; }

    public void initCanvas() {
        map = new Map(0);
        map.addRectangle(new MapRect(0, 0,   500, 1200, 60));
        map.addRectangle(new MapRect(0, 500, 300, 100,  40));
        map.addRectangle(new MapRect(0, 200, 400, 80,   40));
        map.addRectangle(new MapRect(0, 400, 400, 80,   40));

        mainPlayer = new MainPlayer(0, 150, 150);
        // סקיל שני (index 1) נקבע לפי הגיבור הנבחר
        switch (selectedHero) {
            case WARRIOR: mainPlayer.addAttack(new SlashAttack());    break;
            case MAGE:    mainPlayer.addAttack(new FireballAttack()); break;
        }

        sword = new Sword("Iron Sword", 10, 300, 450);

        enemies = new ArrayList<>();
        spawnEnemy(1, 700, 430, EnemyType.SWIFT_HENRY);
        spawnEnemy(2, 900, 430, EnemyType.EVIL_HENRY);
        spawnEnemy(3, 600, 430, EnemyType.GIANT_HENRY);
    }

    public Map getMap()               { return map; }
    public MainPlayer getMainPlayer() { return mainPlayer; }
    public Sword getSword()           { return sword; }
    public List<Enemy> getEnemies()   { return enemies; }

    // יצירת אויב מסוג נתון והוספתו לעולם — נקודה אחת לכל spawn
    public Enemy spawnEnemy(int id, double x, double y, EnemyType type) {
        Enemy enemy = new Enemy(id, x, y, type);
        enemies.add(enemy);
        return enemy;
    }

    // מוסיפה 5 מפלצות מאותו סוג באותו אזור (פרושות מעט אופקית כדי שלא יחפפו).
    public void spot(EnemyType type, double x, double y) {
        final int COUNT   = 5;
        final int SPACING = 60;
        for (int i = 0; i < COUNT; i++) {
            spawnEnemy(nextEnemyId++, x + i * SPACING, y, type);
        }
    }
}
