package team.model;

import base.IdentifiedObject;
import java.util.ArrayList;
import java.util.List;

public class Map extends IdentifiedObject {
    private List<MapRect> rectangles;

    public Map(int id) {
        super(id);
        this.rectangles = new ArrayList<>();
    }

    public List<MapRect> getRectangles() {
        return rectangles;
    }

    public void addRectangle(MapRect rect) {
        this.rectangles.add(rect);
    }
}