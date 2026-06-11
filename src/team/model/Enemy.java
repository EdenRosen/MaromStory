package team.model;

public class Enemy extends Character {

    public static final int DEATH_ANIMATION_TICKS = 18;
    public static final double ATTACK_RANGE = 55;          // טווח מגע לתקיפת השחקן
    public static final int ATTACK_COOLDOWN_TICKS = 30;    // ~0.9 שניות בין מכה למכה

    private final EnemyType type;
    private int deathAnimationTicks = 0;
    private int attackCooldown = 0;

    public Enemy(int id, double x, double y, EnemyType type) {
        super(id, x, y, new PlayerStats(type.maxHealth, type.maxEnergy, type.strength, type.agility));
        this.type = type;
    }
    
    public void updateAi(MainPlayer player) {
        if (isDying()) {
            velocityX = 0;
            return;
        }
        double dx = player.getX() - x;
        if (Math.abs(dx) > 40) {
            // המהירות נגזרת מסוג האויב
            velocityX = (dx > 0) ? type.speed : -type.speed;
            facingRight = (dx > 0);
        } else {
            velocityX = 0;
        }
    }

    public boolean canAttack(MainPlayer player, double range) {
        if (isDying() || attackCooldown > 0) return false;   // לא תוקף בזמן cooldown / גסיסה
        double dist = Math.sqrt(Math.pow(x - player.getX(), 2) +
                                Math.pow(y - player.getY(), 2));
        return dist <= range;
    }

    // תוקף את השחקן — מוריד חיים לפי הכוח, ומפעיל cooldown
    public void attackPlayer(MainPlayer player) {
        double damage = stats.getStrength();
        player.getStats().takeDamage(damage);
        attackCooldown = ATTACK_COOLDOWN_TICKS;
    }

    public void updateCooldown() {
        if (attackCooldown > 0) attackCooldown--;
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

    public EnemyType getType()     { return type; }
    public String getDisplayName() { return type.displayName; }
    public boolean isDying() { return stats.isDead() && deathAnimationTicks > 0; }
    public boolean shouldDisappear() { return stats.isDead() && deathAnimationTicks <= 0; }
    public int getDeathAnimationTicks() { return deathAnimationTicks; }
}
