package team.model;

import java.util.ArrayList;
import java.util.List;

public class Canvas {

    private Map map;
    private MainPlayer mainPlayer;
    private Sword sword;
    private List<Enemy> enemies;

    // הגיבור הנבחר — נשמר בין אתחולים (reset) ונקבע במסך הפתיחה
    private HeroType selectedHero = HeroType.WARRIOR;
    // המפה הנוכחית — נשמרת בין אתחולים
    private MapType currentMap = MapType.MEADOW;

    public HeroType getSelectedHero()          { return selectedHero; }
    public void setSelectedHero(HeroType hero)  { this.selectedHero = hero; }
    public MapType getCurrentMap()             { return currentMap; }
    public void setCurrentMap(MapType map)      { this.currentMap = map; }

    // אתחול מלא — יוצר שחקן חדש ובונה את העולם (עלייה / reset)
    public void initCanvas() {
        mainPlayer = new MainPlayer(0, 150, 150);
        // סקיל שני (index 1) נקבע לפי הגיבור הנבחר
        switch (selectedHero) {
            case WARRIOR: mainPlayer.addAttack(new SlashAttack());    break;
            case MAGE:    mainPlayer.addAttack(new FireballAttack()); break;
        }
        buildWorld();
    }

    // מעבר (טלפורט) למפה אחרת — שומר את השחקן וההתקדמות שלו, בונה עולם חדש
    public void loadMap(MapType type) {
        currentMap = type;
        mainPlayer.setPosition(150, 150);   // חזרה לנקודת ההתחלה במפה החדשה
        buildWorld();
    }

    // בונה את המפה, החרב והאויבים לפי המפה הנוכחית
    private void buildWorld() {
        map = new Map(0);
        enemies = new ArrayList<>();
        switch (currentMap) {
            case MEADOW:  buildMeadow();  break;
            case INFERNO: buildInferno(); break;
        }
    }

    private void buildMeadow() {
        map.addRectangle(new MapRect(0, 0,   500, 1200, 60));
        map.addRectangle(new MapRect(0, 500, 300, 100,  40));
        map.addRectangle(new MapRect(0, 200, 400, 80,   40));
        map.addRectangle(new MapRect(0, 400, 400, 80,   40));

        sword = new Sword("Iron Sword", 10, 300, 450);

        spawnEnemy(1, 700, 430, EnemyType.SWIFT_HENRY);
        spawnEnemy(2, 900, 430, EnemyType.EVIL_HENRY);
        spawnEnemy(3, 600, 430, EnemyType.GIANT_HENRY);
    }

    private void buildInferno() {
        map.addRectangle(new MapRect(0, 0,   500, 1200, 60));
        map.addRectangle(new MapRect(0, 450, 250, 120,  40));
        map.addRectangle(new MapRect(0, 250, 350,  90,  40));
        map.addRectangle(new MapRect(0, 350, 520,  70,  40));

        sword = new Sword("Demon Blade", 30, 350, 300);   // חרב חזקה בהרבה

        spawnEnemy(1, 700, 430, EnemyType.INFERNO_HENRY);
        spawnEnemy(2, 950, 430, EnemyType.INFERNO_HENRY);
        spawnEnemy(3, 550, 430, EnemyType.DOOM_HENRY);
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
    
}
