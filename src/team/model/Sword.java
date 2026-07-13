package team.model;

// Stores a sword item and its world position

public class Sword {

    private String name;
    private double strengthBonus;
    private double x;
    private double y;
    private boolean onGround;

    public Sword(String name, double strengthBonus, double x, double y) {
        this.name           = name;
        this.strengthBonus  = strengthBonus;
        this.x              = x;
        this.y              = y;
        this.onGround       = true;
    }

    public String getName()          { return name; }
    public double getStrengthBonus() { return strengthBonus; }
    public double getX()             { return x; }
    public double getY()             { return y; }
    public boolean isOnGround()      { return onGround; }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public void setOnGround(boolean onGround)    { this.onGround = onGround; }
}
