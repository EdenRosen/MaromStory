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
        enemies.add(new Enemy(1, 700, 430, "Henry1"));
        enemies.add(new Enemy(2, 900, 430, "Henry2"));
        enemies.add(new Enemy(3, 600, 430, "Henry3"));
    }

    public Map getMap()               { return map; }
    public MainPlayer getMainPlayer() { return mainPlayer; }
    public Sword getSword()           { return sword; }
    public List<Enemy> getEnemies()   { return enemies; }
}
