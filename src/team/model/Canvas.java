package team.model;

import java.util.ArrayList;
import java.util.List;

public class Canvas {

    private Map map;
    private MainPlayer mainPlayer1;  // P1 (arrow keys in multiplayer)
    private MainPlayer mainPlayer2;  // P2 (WASD in multiplayer, null in solo)
    private Sword sword;
    private List<Enemy> enemies;

    // הגיבור הנבחר — נשמר בין אתחולים (reset) ונקבע במסך הפתיחה
    private HeroType selectedHero = HeroType.WARRIOR;
    // המפה הנוכחית — נשמרת בין אתחולים
    private MapType currentMap = MapType.MEADOW;
    // מצב המשחק — Solo או Multiplayer
    private GameMode selectedGameMode = GameMode.SOLO;

    public HeroType getSelectedHero()          { return selectedHero; }
    public void setSelectedHero(HeroType hero)  { this.selectedHero = hero; }
    public MapType getCurrentMap()             { return currentMap; }
    public void setCurrentMap(MapType map)      { this.currentMap = map; }
    public GameMode getSelectedGameMode()      { return selectedGameMode; }
    public void setSelectedGameMode(GameMode mode) { this.selectedGameMode = mode; }

    // אתחול מלא — יוצר שחקן חדש ובונה את העולם (עלייה / reset)
    public void initCanvas() {
        // תמיד צור את Player 1
        mainPlayer1 = new MainPlayer(0, 150, 150);
        setupPlayerAttacks(mainPlayer1);
        
        // ב-Multiplayer mode, צור גם את Player 2 בעמדת התחלה שונה
        if (selectedGameMode == GameMode.MULTIPLAYER) {
            mainPlayer2 = new MainPlayer(1, 900, 150);
            setupPlayerAttacks(mainPlayer2);
        } else {
            mainPlayer2 = null;  // Solo mode
        }
        
        buildWorld();
    }

    // עזר לאתחול הקסמים של שחקן
    private void setupPlayerAttacks(MainPlayer player) {
        // סקיל שני (index 1) נקבע לפי הגיבור הנבחר
        switch (selectedHero) {
            case WARRIOR:
                player.addAttack(new SlashAttack());
                break;
            case MAGE:
                player.addAttack(new FireballAttack());
                player.addAttack(new AquaBeamAttack());
                break;
        }
    }

    // מעבר (טלפורט) למפה אחרת — שומר את השחקן וההתקדמות שלו, בונה עולם חדש
    public void loadMap(MapType type) {
        currentMap = type;
        mainPlayer1.setPosition(150, 150);   // חזרה לנקודת ההתחלה במפה החדשה
        if (mainPlayer2 != null) {
            mainPlayer2.setPosition(900, 150);  // P2 starts at different position
        }
        buildWorld();
    }

    // בונה את המפה, החרב והאויבים לפי המפה הנוכחית
    private void buildWorld() {
        map = new Map(0);
        enemies = new ArrayList<>();
        switch (currentMap) {
            case MEADOW:  buildMeadow();  break;
            case INFERNO: buildInferno(); break;
            case FROST:   buildFrost();   break;
            case VOID:    buildVoid();    break;
        }
    }

    private void buildMeadow() {
        map.addRectangle(new MapRect(0, 0,   500, 1200, 60));
        map.addRectangle(new MapRect(0, 500, 300, 100,  40));
        map.addRectangle(new MapRect(0, 200, 400, 80,   40));
        map.addRectangle(new MapRect(0, 400, 400, 80,   40));

        sword = new Sword("Iron Sword", 10, 300, 450);

        spawnEnemy(1, 500, 430, EnemyType.SWIFT_HENRY);
        spawnEnemy(2, 700, 430, EnemyType.EVIL_HENRY);
        spawnEnemy(3, 400, 430, EnemyType.GIANT_HENRY);
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

    private void buildFrost() {
        map.addRectangle(new MapRect(0, 0,   500, 1200, 60));
        map.addRectangle(new MapRect(0, 120, 360, 150,  35));
        map.addRectangle(new MapRect(0, 600, 300, 140,  35));
        map.addRectangle(new MapRect(0, 360, 250, 110,  35));

        sword = new Sword("Frost Fang", 50, 420, 320);

        spawnEnemy(1, 680, 430, EnemyType.FROST_HENRY);
        spawnEnemy(2, 980, 430, EnemyType.FROST_HENRY);
        spawnEnemy(3, 520, 430, EnemyType.YETI_HENRY);
    }

    private void buildVoid() {
        map.addRectangle(new MapRect(0, 0,   500, 1200, 60));
        map.addRectangle(new MapRect(0, 150, 420, 180,  30));
        map.addRectangle(new MapRect(0, 500, 250, 180,  30));
        map.addRectangle(new MapRect(0, 820, 300, 150,  30));
        map.addRectangle(new MapRect(0, 350, 280, 120,  30));

        sword = new Sword("Void Reaver", 80, 430, 300);

        spawnEnemy(1, 700, 430, EnemyType.VOID_HENRY);
        spawnEnemy(2, 980, 430, EnemyType.VOID_HENRY);
        spawnEnemy(3, 540, 430, EnemyType.COSMIC_HENRY);
    }

    public Map getMap()               { return map; }
    public MainPlayer getMainPlayer() { return mainPlayer1; }  // P1 (backward compatible)
    public MainPlayer getMainPlayer2() { return mainPlayer2; } // P2 (null in solo mode)
    public Sword getSword()           { return sword; }
    public void setSword(Sword s)     { sword = s; }
    public List<Enemy> getEnemies()   { return enemies; }

    // יצירת אויב מסוג נתון והוספתו לעולם — נקודה אחת לכל spawn
    public Enemy spawnEnemy(int id, double x, double y, EnemyType type) {
        Enemy enemy = new Enemy(id, x, y, type);
        enemies.add(enemy);
        return enemy;
    }
    
}
