package ai.ui;

import java.util.Map;
import java.util.HashMap;
import javax.swing.JPanel;
import shared.ui_ports.UiPort;

/**
 * Implements the shared user interface bridge used by the game backend
 */
public class UiPortImpl extends UiPort {
    private Map<String, ImageElement> imageElementsById;
    private team.model.Map activeMap;
    private team.model.MainPlayer mainPlayer;
    private team.model.MainPlayer mainPlayer2;
    private JPanel drawingPanel;

    public UiPortImpl(Map<String, ImageElement> imageElementsById, team.model.Map activeMap, JPanel drawingPanel) {
        this.imageElementsById = imageElementsById;
        this.activeMap    = activeMap;
        this.drawingPanel  = drawingPanel;
        this.mainPlayer2 = null;
    }

    @Override
    public void addImage(int imageId, String path, double x, double y, int width, int height, double angle, boolean visible) {
        imageElementsById.put(String.valueOf(imageId), new ImageElement(path, (int) x, (int) y, width, height, angle, visible));
        drawingPanel.repaint();
    }

    @Override
    public void updateImage(int imageId, double x, double y, int width, int height, double angle, boolean visible) {
        ImageElement image = imageElementsById.get(String.valueOf(imageId));
        if (image != null) {
            image.update((int) x, (int) y, width, height, angle, visible);
            drawingPanel.repaint();
        }
    }

    @Override
    public void setMap(team.model.Map newMap) {
        this.activeMap = newMap;
        drawingPanel.repaint();
    }

    @Override
    public team.model.Map getMap() {
        return this.activeMap;
    }

    @Override
    public void setMainPlayer(team.model.MainPlayer player) {
        this.mainPlayer = player;
        drawingPanel.repaint();
    }

    @Override
    public team.model.MainPlayer getMainPlayer() {
        return this.mainPlayer;
    }

    @Override
    public void updatePlayerPosition(double x, double y) {
        if (mainPlayer != null) {
            mainPlayer.setX(x);
            mainPlayer.setY(y);
            drawingPanel.repaint();
        }
    }

    @Override
    public void setMainPlayer2(team.model.MainPlayer player) {
        this.mainPlayer2 = player;
        drawingPanel.repaint();
    }

    @Override
    public team.model.MainPlayer getMainPlayer2() {
        return this.mainPlayer2;
    }

    @Override
    public void updatePlayer2Position(double x, double y) {
        if (mainPlayer2 != null) {
            mainPlayer2.setX(x);
            mainPlayer2.setY(y);
            drawingPanel.repaint();
        }
    }

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    @Override
    public void renderInitials() {
        if (drawingPanel instanceof DrawingPanel) {
            ((DrawingPanel) drawingPanel).setGameStarted(true);
        }
        drawingPanel.repaint();
    }
}
