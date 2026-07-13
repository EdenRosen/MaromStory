package team.model;

import java.util.List;

public class MainPlayer extends Character {

    public static final double MOVE_SPEED   = 5;
    public static final double PICKUP_RANGE = 60;
    public static final double ATTACK_RANGE = 90;
    public static final double ATTACK_HEIGHT = 65;
    public static final int ATTACK_ANIMATION_TICKS = 8;

    private int attackAnimationTicks = 0;
    private String currentAnimation = "";

    private final PlayerProgress progress = new PlayerProgress();
    private final HeroType heroType;

    public MainPlayer(int id, double x, double y) {
        this(id, x, y, HeroType.WARRIOR);
    }

    public MainPlayer(int id, double x, double y, HeroType heroType) {
        super(id, x, y, new PlayerStats(
                startingHealth(heroType),
                50,
                10,
                5));
        this.heroType = heroType;
    }

    private static double startingHealth(HeroType heroType) {
        switch (heroType) {
            case DRAGON: return 150;
            case WARRIOR: return 120;
            default: return 100;
        }
    }

    private static double baseMoveSpeed(HeroType heroType) {
        switch (heroType) {
            case MAGE: return 6.2;
            case DRAGON: return 3.8;
            default: return MOVE_SPEED;
        }
    }

    public PlayerProgress getProgress() { return progress; }
    public HeroType getHeroType() { return heroType; }

    // מהירות תנועה מושפעת מזריזות — כל נקודת AGI מעל הבסיס מוסיפה מעט מהירות
    public double getMoveSpeed() {
        return baseMoveSpeed(heroType) + (getStats().getAgility() - 5) * 0.35;
    }

    // --- תנועה --- עם עדכון כיוון

    @Override
    public void setVelocityX(double vx) {
        super.setVelocityX(vx);
        if (vx > 0) facingRight = true;
        if (vx < 0) facingRight = false;
    }

    // --- הרמה וזריקת חרב ---

    public Sword dropSword() {
        Sword dropped = super.dropSword();
        return dropped;
    }

    public void startAttackAnimation(String attackName) {
        attackAnimationTicks = ATTACK_ANIMATION_TICKS;
        currentAnimation = attackName;
    }

    public void updateAttackAnimation() {
        if (attackAnimationTicks > 0) attackAnimationTicks--;
    }

    // --- Getters ---

    public boolean isAttacking() { return attackAnimationTicks > 0; }
    public int getAttackAnimationTicks() { return attackAnimationTicks; }
    public String getCurrentAnimation() { return currentAnimation; }
}
