package team.model;

// Controls enemy movement targeting attacks and death timing

public class Enemy extends Character {

    public static final int DEATH_ANIMATION_TICKS = 18;
    public static final double ATTACK_RANGE = 55;
    public static final int ATTACK_COOLDOWN_TICKS = 30;

    private final EnemyType enemyType;
    private int deathAnimationTicks = 0;
    private int attackCooldown = 0;

    public Enemy(int id, double x, double y, EnemyType enemyType) {
        super(id, x, y, new PlayerStats(enemyType.maxHealth, enemyType.maxEnergy, enemyType.strength, enemyType.agility));
        this.enemyType = enemyType;
    }

    public void updateAi(MainPlayer player) {
        if (isDying()) {
            velocityX = 0;
            return;
        }
        double horizontalDistanceToPlayer = player.getX() - x;
        if (Math.abs(horizontalDistanceToPlayer) > 40) {

            velocityX = (horizontalDistanceToPlayer > 0) ? enemyType.speed : -enemyType.speed;
            facingRight = (horizontalDistanceToPlayer > 0);
        } else {
            velocityX = 0;
        }
    }

    public boolean canAttack(MainPlayer player, double attackRange) {
        if (isDying() || attackCooldown > 0) return false;
        double distanceToPlayer = Math.sqrt(Math.pow(x - player.getX(), 2) +
                                Math.pow(y - player.getY(), 2));
        return distanceToPlayer <= attackRange;
    }


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

    public EnemyType getType()     { return enemyType; }
    public String getDisplayName() { return enemyType.displayName; }
    public boolean isDying() { return stats.isDead() && deathAnimationTicks > 0; }
    public boolean shouldDisappear() { return stats.isDead() && deathAnimationTicks <= 0; }
    public int getDeathAnimationTicks() { return deathAnimationTicks; }
}
