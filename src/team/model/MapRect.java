package team.model;

import base.IdentifiedObject;

// Stores one rectangle used for map collision

public class MapRect extends IdentifiedObject {
    private double x;
    private double y;
    private double width;
    private double height;

    public MapRect(int id, double x, double y, double width, double height) {
        super(id);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
}