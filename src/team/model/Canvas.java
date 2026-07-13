package team.model;

import java.util.ArrayList;
import java.util.List;

// Builds the current map players enemies weapons and selected mode

public class Canvas {

    private Map map;
    private MainPlayer mainPlayer1;
    private MainPlayer mainPlayer2;
    private Sword sword;
    private List<Enemy> enemies;
    private List<Sword> extraSwords = new ArrayList<>();

    // Stores the current hero choices and mode between resets
    private HeroType selectedHero = HeroType.WARRIOR;
    private HeroType selectedHero2 = HeroType.WARRIOR;

    private MapType currentMap = MapType.MEADOW;

    private GameMode selectedGameMode = GameMode.SOLO;

    public HeroType getSelectedHero()          { return selectedHero; }
    public void setSelectedHero(HeroType hero)  { this.selectedHero = hero; }
    public HeroType getSelectedHero2()          { return selectedHero2; }
    public void setSelectedHero2(HeroType hero) { this.selectedHero2 = hero; }
    public MapType getCurrentMap()             { return currentMap; }
    public void setCurrentMap(MapType map)      { this.currentMap = map; }
    public GameMode getSelectedGameMode()      { return selectedGameMode; }
    public void setSelectedGameMode(GameMode mode) { this.selectedGameMode = mode; }

    // Creates players and rebuilds the active world
    public void initCanvas() {

        mainPlayer1 = new MainPlayer(0, 150, 150, selectedHero);
        setupPlayerAttacks(mainPlayer1, selectedHero);


        if (selectedGameMode == GameMode.MULTIPLAYER || selectedGameMode == GameMode.PVP) {
            mainPlayer2 = new MainPlayer(1, 900, 150, selectedHero2);
            setupPlayerAttacks(mainPlayer2, selectedHero2);
            mainPlayer2.setVelocityX(-1);
            mainPlayer2.setVelocityX(0);
        } else {
            mainPlayer2 = null;
        }

        if (selectedGameMode == GameMode.PVP) {
            preparePvpPlayer(mainPlayer1);
            preparePvpPlayer(mainPlayer2);
        }

        buildWorld();
    }

    // Adds hero specific attacks after the basic attack is created
    private void setupPlayerAttacks(MainPlayer player, HeroType heroType) {

        switch (heroType) {
            case WARRIOR:
                player.addAttack(new SlashAttack());
                break;
            case MAGE:
                player.addAttack(new FireballAttack());
                player.addAttack(new AquaBeamAttack());
                break;
            case DRAGON:
                player.addAttack(new DragonFireballAttack());
                break;
        }
    }

    // Prepares duel players with stronger resources and starter weapons
    private void preparePvpPlayer(MainPlayer player) {
        player.getProgress().setStartingLevel(5);
        player.getStats().increaseMaxEnergy(60);
        if (player.getHeroType() == HeroType.WARRIOR) {
            player.pickupSword(new Sword("Iron Sword", 10, player.getX(), player.getY()));
        }
    }

    // Moves players to a fresh version of the selected map
    public void loadMap(MapType type) {
        currentMap = type;
        mainPlayer1.setPosition(150, 150);
        if (mainPlayer2 != null) {
            mainPlayer2.setPosition(900, 150);
        }
        buildWorld();
    }

    // Builds platforms enemies swords and map specific objects
    private void buildWorld() {
        map = new Map(0);
        enemies = new ArrayList<>();
        extraSwords.clear();
        switch (currentMap) {
            case MEADOW:      buildMeadow();     break;
            case INFERNO:     buildInferno();    break;
            case FROST:       buildFrost();      break;
            case VOID:        buildVoid();       break;
            case BOSS_ARENA:  buildBossArena();  break;
        }
        if (selectedGameMode == GameMode.PVP) {
            enemies.clear();
            extraSwords.clear();
            sword = null;
        }
    }

    // Builds the first adventure stage
    private void buildMeadow() {
        map.addRectangle(new MapRect(0, 0,   500, 2000, 60));
        map.addRectangle(new MapRect(0, 200, 370, 80,   30));
        map.addRectangle(new MapRect(0, 280, 270, 100,  30));
        map.addRectangle(new MapRect(0, 380, 370, 80,   30));
        map.addRectangle(new MapRect(0, 460, 270, 100,  30));
        map.addRectangle(new MapRect(0, 560, 370, 80,   30));

        
        map.addRectangle(new MapRect(0, 700, 300, 150,   30));

        sword = new Sword("Iron Sword", 10, 300, 450);

        spawnEnemy(1, 500, 430, EnemyType.SWIFT_HENRY);
        spawnEnemy(2, 700, 430, EnemyType.EVIL_HENRY);
        spawnEnemy(3, 400, 430, EnemyType.GIANT_HENRY);
    }

    // Builds the fire themed adventure stage
    private void buildInferno() {
        map.addRectangle(new MapRect(0, 0,   500, 1200, 60));
        map.addRectangle(new MapRect(0, 450, 250, 120,  40));
        map.addRectangle(new MapRect(0, 250, 350,  90,  40));
        map.addRectangle(new MapRect(0, 350, 520,  70,  40));

        sword = new Sword("Demon Blade", 30, 350, 300);

        spawnEnemy(1, 700, 430, EnemyType.INFERNO_HENRY);
        spawnEnemy(2, 950, 430, EnemyType.INFERNO_HENRY);
        spawnEnemy(3, 550, 430, EnemyType.DOOM_HENRY);
    }

    // Builds the ice themed adventure stage
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

    // Builds the void themed adventure stage
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

    // Builds the final boss arena
    private void buildBossArena() {

        map.addRectangle(new MapRect(0, 0,   500, 1200, 60));
        map.addRectangle(new MapRect(0, 100, 250,  180, 30));
        map.addRectangle(new MapRect(0, 950, 250,  180, 30));
        map.addRectangle(new MapRect(0, 460, 320,  280, 30));
        map.addRectangle(new MapRect(0, 240, 350,  160, 25));
        map.addRectangle(new MapRect(0, 780, 350,  160, 25));

        sword = null;


        spawnEnemy(1, 630, 430, EnemyType.FINAL_BOSS);
    }

    public Map getMap()               { return map; }
    public MainPlayer getMainPlayer() { return mainPlayer1; }
    public MainPlayer getMainPlayer2() { return mainPlayer2; }
    public Sword getSword()           { return sword; }
    public void setSword(Sword s)     { sword = s; }
    public List<Enemy> getEnemies()   { return enemies; }
    public List<Sword> getExtraSwords()   { return extraSwords; }
    public void addExtraSword(Sword s)    { extraSwords.add(s); }

    // Creates an enemy and registers it in the active world
    public Enemy spawnEnemy(int id, double x, double y, EnemyType type) {
        Enemy enemy = new Enemy(id, x, y, type);
        enemies.add(enemy);
        return enemy;
    }

}
