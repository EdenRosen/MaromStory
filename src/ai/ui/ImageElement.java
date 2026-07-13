package ai.ui;

import java.awt.Image;
import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Loads image assets and stores their current drawing state
 */
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
        debug("Loading image: " + path);
        debug("user.dir = " + System.getProperty("user.dir"));

        File directFile = new File(path);
        debugFile("direct path", directFile);
        if (directFile.isFile()) {
            debug("FOUND direct path");
            return new ImageIcon(directFile.getAbsolutePath());
        }

        File projectFile = new File("MaromStory", path);
        debugFile("MaromStory path", projectFile);
        if (projectFile.isFile()) {
            debug("FOUND MaromStory path");
            return new ImageIcon(projectFile.getAbsolutePath());
        }

        File foundFile = findInParentDirectories(path);
        if (foundFile != null) {
            debugFile("FOUND parent search", foundFile);
            return new ImageIcon(foundFile.getAbsolutePath());
        }

        File classLocationFile = findFromClassLocation(path);
        if (classLocationFile != null) {
            debugFile("FOUND class location search", classLocationFile);
            return new ImageIcon(classLocationFile.getAbsolutePath());
        }

        URL resource = ImageElement.class.getClassLoader().getResource(path);
        debug("classpath resource = " + resource);
        if (resource != null) {
            debug("FOUND classpath resource");
            return new ImageIcon(resource);
        }

        System.err.println("Could not load image: " + path);
        return new ImageIcon(path);
    }

    private static File findInParentDirectories(String path) {
        File current = new File(System.getProperty("user.dir"));
        while (current != null) {
            File candidate = new File(current, path);
            debugFile("parent search", candidate);
            if (candidate.isFile()) {
                return candidate;
            }
            current = current.getParentFile();
        }
        return null;
    }

    private static File findFromClassLocation(String path) {
        try {
            URL location = ImageElement.class.getProtectionDomain().getCodeSource().getLocation();
            debug("class location = " + location);
            File current = new File(location.toURI());
            if (current.isFile()) {
                current = current.getParentFile();
            }

            while (current != null) {
                File candidate = new File(current, path);
                debugFile("class location search", candidate);
                if (candidate.isFile()) {
                    return candidate;
                }
                current = current.getParentFile();
            }
        } catch (Exception e) {
            debug("class location search failed: " + e.getMessage());
        }
        return null;
    }

    private static void debugFile(String label, File file) {
        debug(label + " = " + file.getAbsolutePath()
                + " | exists=" + file.exists()
                + " | isFile=" + file.isFile());
    }

    private static void debug(String message) {
        System.err.println("[ImageElement] " + message);
    }
}
