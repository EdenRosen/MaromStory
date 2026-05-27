package team.model;

import java.util.ArrayList;
import java.util.List;

public class Canvas {

    private Map map;
    private MainPlayer mainPlayer;
    private Sword sword;
    private List<Enemy> enemies;

    public void initCanvas() {
        map = new Map(0);
        map.addRectangle(new MapRect(0, 0,   500, 1200, 60));
        map.addRectangle(new MapRect(0, 500, 300, 100,  40));
        map.addRectangle(new MapRect(0, 200, 400, 80,   40));
        map.addRectangle(new MapRect(0, 400, 400, 80,   40));

        mainPlayer = new MainPlayer(0, 150, 150);
        mainPlayer.addAttack(new SlashAttack()); // index 1

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
