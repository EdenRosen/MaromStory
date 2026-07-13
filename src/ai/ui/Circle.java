package ai.ui;

import java.awt.Color;

/**
 * Stores circle drawing data for simple user interface shapes
 */
public class Circle {
    public int cx, cy, radius;
    public boolean isBlinking;
    public Color color;

    public Circle(int cx, int cy, int radius) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.isBlinking = false;
        this.color = Color.BLACK;
    }

    public void update(int cx, int cy, int radius) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
    }
}
