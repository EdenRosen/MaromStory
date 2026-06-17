package team.model;
import java.util.ArrayList;
import java.util.List;
import base.IdentifiedObject;
import java.util.List;

/**
 * מחלקת בסיס לכל הדמויות במשחק.
 * מכילה את כל הלוגיקה המשותפת בין MainPlayer ל-Enemy:
 * פיזיקה, stats, ונשק.
 */
public abstract class Character extends IdentifiedObject {

    // קבועי פיזיקה — משותפים לכל הדמויות
    public static final double JUMP_FORCE   = -13;
    public static final double GRAVITY      = 0.5;
    public static final double SCREEN_RIGHT = 1150;
    public static final int    WIDTH        = 50;
    public static final int    HEIGHT       = 50;

    
    protected double x;
    protected double y;
    protected double velocityX = 0;
    protected double velocityY = 0;
    protected boolean onGround = false;
    protected boolean facingRight = true;
    protected List<Attacks> attacks;
    protected int activeAttackIndex = 0;
    protected int attackCooldownTicks = 0;            // טיקים שנותרו עד שאפשר לתקוף שוב
    private static final double TICK_SECONDS = 0.03;  // לולאת המשחק רצה כל 30ms

    protected final PlayerStats stats;
    protected Sword equippedSword = null;

    public Character(int id, double x, double y, PlayerStats stats) {
        super(id);
        this.x     = x;
        this.y     = y;
        this.stats = stats;
        this.attacks = new ArrayList<>();
        attacks.add(new BasicAttack());
    }

    // movement

    public void setVelocityX(double vx) { this.velocityX = vx; }

    public void jump() {
        if (onGround) {
            // זריזות מגבירה את כוח הקפיצה (ערך שלילי יותר = קפיצה גבוהה יותר)
            velocityY = JUMP_FORCE - (stats.getAgility() - 5) * 0.35;
            onGround  = false;
        }
    }

    // Pyhsics 

    public void update(List<MapRect> platforms) {
        if (attackCooldownTicks > 0) attackCooldownTicks--;   // ספירת cooldown לתקיפה

        velocityY += GRAVITY;
        x += velocityX;
        y += velocityY;

        onGround = false;
        for (MapRect rect : platforms) {
            if (landedOn(rect)) {
                y         = rect.getY() - HEIGHT;
                velocityY = 0;
                onGround  = true;
            }
        }

        if (x < 0)            x = 0;
        if (x > SCREEN_RIGHT) x = SCREEN_RIGHT;
    }

    private boolean landedOn(MapRect rect) {
        boolean overlapX  = (x + WIDTH  > rect.getX()) && (x < rect.getX() + rect.getWidth());
        boolean fromAbove = (y + HEIGHT >= rect.getY()) && (y + HEIGHT <= rect.getY() + rect.getHeight());
        return overlapX && fromAbove && velocityY >= 0;
    }

    public void addAttack(Attacks attack) {
        attacks.add(attack);
    }

    public Attacks getActiveAttack() {
        return attacks.get(activeAttackIndex);
    }
    public boolean useAttack(int index, Character target) {
        if (index < 0 || index >= attacks.size()) return false;
        if (attackCooldownTicks > 0) return false;            // עדיין ב-cooldown
        Attacks attack = attacks.get(index);
        if (attack.getMpCost() > stats.getEnergy()) return false;
        if (!attack.canExecute(this, target)) return false;
        attack.executeAttack(this, target);
        stats.useEnergy(attack.getMpCost());
        // AGI מקצר קולדאון — כל נקודה מעל 5 מוריד 4% (מינימום 40% מהמקור)
        double agiReduction = Math.max(0.4, 1.0 - (stats.getAgility() - 5) * 0.04);
        attackCooldownTicks = (int) Math.round(attack.getCooldown() * agiReduction / TICK_SECONDS);
        return true;
    }

    public boolean isFacingRight() { return facingRight; }

    public boolean isOnAttackCooldown() { return attackCooldownTicks > 0; }

    public void setActiveAttack(int index) {
        if (index >= 0 && index < attacks.size()) activeAttackIndex = index;
    }

    public boolean useActiveAttack(Character target) {
        return useAttack(activeAttackIndex, target);
    }

    public String getActiveAttackName() {
        return attacks.get(activeAttackIndex).getAttackName();
    }

    // --- נשק ---

    public void pickupSword(Sword sword) {
        equippedSword = sword;
        stats.equipSword(sword);
        sword.setOnGround(false);
    }

    public Sword dropSword() {
        if (equippedSword == null) return null;
        Sword dropped = equippedSword;
        stats.unequipSword(dropped);
        dropped.setPosition(x, y);
        dropped.setOnGround(true);
        equippedSword = null;
        return dropped;
    }
    

    public boolean hasSword()          { return equippedSword != null; }
    public Sword  getEquippedSword()   { return equippedSword; }

    public void takeDamage(double damage) {
        stats.takeDamage(damage);
    }

    // --- Getters / Setters ---

    public double getX()          { return x; }
    public double getY()          { return y; }
    public boolean isOnGround()   { return onGround; }
    public PlayerStats getStats() { return stats; }

    public void setX(double x)    { this.x = x; }
    public void setY(double y)    { this.y = y; }
    public void setPosition(double x, double y) { this.x = x; this.y = y; }
}
