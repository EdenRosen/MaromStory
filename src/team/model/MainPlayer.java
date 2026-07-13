package team.model;

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

    // Chooses starting health from the selected hero identity
    private static double startingHealth(HeroType heroType) {
        switch (heroType) {
            case DRAGON: return 150;
            case WARRIOR: return 120;
            default: return 100;
        }
    }

    // Chooses base movement speed from the selected hero identity
    private static double baseMoveSpeed(HeroType heroType) {
        switch (heroType) {
            case MAGE: return 6.2;
            case DRAGON: return 3.8;
            default: return MOVE_SPEED;
        }
    }

    public PlayerProgress getProgress() { return progress; }
    public HeroType getHeroType() { return heroType; }

    // Combines hero base speed with agility growth
    public double getMoveSpeed() {
        return baseMoveSpeed(heroType) + (getStats().getAgility() - 5) * 0.35;
    }


    // Updates movement and facing direction together
    @Override
    public void setVelocityX(double vx) {
        super.setVelocityX(vx);
        if (vx > 0) facingRight = true;
        if (vx < 0) facingRight = false;
    }


    // Drops the equipped sword through the shared character logic
    public Sword dropSword() {
        Sword dropped = super.dropSword();
        return dropped;
    }

    // Starts the named attack animation for a short duration
    public void startAttackAnimation(String attackName) {
        attackAnimationTicks = ATTACK_ANIMATION_TICKS;
        currentAnimation = attackName;
    }

    // Advances the active attack animation timer
    public void updateAttackAnimation() {
        if (attackAnimationTicks > 0) attackAnimationTicks--;
    }


    // Exposes current attack animation state
    public boolean isAttacking() { return attackAnimationTicks > 0; }
    public int getAttackAnimationTicks() { return attackAnimationTicks; }
    public String getCurrentAnimation() { return currentAnimation; }
}
