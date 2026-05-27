package ai.ui;

import java.awt.Image;
import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;

public class ImageElement {
    public int x, y, width, height;
    public double angle;
    public Image image;
    public boolean visible;
    public String path;

    public ImageElement(String path, int x, int y, int width, int height, double angle, boolean visible) {
        this.path = path;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.angle = angle;
        this.visible = visible;
        this.image = loadIcon(path).getImage();
    }

    public void update(int x, int y, int width, int height, double angle, boolean visible) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.angle = angle;
        this.visible = visible;
    }

    public boolean isLoaded() {
        return image != null && image.getWidth(null) > 0 && image.getHeight(null) > 0;
    }

    public static Image loadImage(String path) {
        return loadIcon(path).getImage();
    }

    private static ImageIcon loadIcon(String path) {
        File directFile = new File(path);
        if (directFile.isFile()) {
            return new ImageIcon(directFile.getAbsolutePath());
        }

        File projectFile = new File("MaromStory", path);
        if (projectFile.isFile()) {
            return new ImageIcon(projectFile.getAbsolutePath());
        }

        URL resource = ImageElement.class.getClassLoader().getResource(path);
        if (resource != null) {
            return new ImageIcon(resource);
        }

        System.err.println("Could not load image: " + path);
        return new ImageIcon(path);
    }
}
