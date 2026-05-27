package team.model;

public class Enemy extends Character {

    public static final int DEATH_ANIMATION_TICKS = 18;
    
    private String type;
    private int deathAnimationTicks = 0;

    public Enemy(int id, double x, double y, String type) {
        super(id, x, y, new PlayerStats(80, 30, 8, 3));
        this.type = type;
    }
    
    public void updateAi(MainPlayer player) {
        if (isDying()) {
            velocityX = 0;
            return;
        }
        double dx = player.getX() - x;
        if (Math.abs(dx) > 40) {
            // Henry1 is the fastest, while Henry2 and Henry3 are the slowest
            double speed = "Henry1".equals(type) ? 4.0 : 1.0;
            velocityX = (dx > 0) ? speed : -speed;
            facingRight = (dx > 0);
        } else {
            velocityX = 0;
        }
    }

    public boolean canAttack(MainPlayer player, double range) {
        double dist = Math.sqrt(Math.pow(x - player.getX(), 2) +
                                Math.pow(y - player.getY(), 2));
        return dist <= range;
    }

    // תוקף את השחקן — מוריד חיים לפי הכוח
    public void attackPlayer(MainPlayer player) {
        double damage = stats.getStrength();
        player.getStats().takeDamage(damage);
    }

    public void takeDamage(double damage) {
        if (isDying()) return;

        stats.takeDamage(damage);
        if (stats.isDead()) {
            deathAnimationTicks = DEATH_ANIMATION_TICKS;
        }
    }

    public void updateDeathAnimation() {
        if (deathAnimationTicks > 0) deathAnimationTicks--;
    }

    public String getType() { return type; }
    public boolean isDying() { return stats.isDead() && deathAnimationTicks > 0; }
    public boolean shouldDisappear() { return stats.isDead() && deathAnimationTicks <= 0; }
    public int getDeathAnimationTicks() { return deathAnimationTicks; }
}
