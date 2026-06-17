package ai.ui;

import java.util.Map;
import java.util.HashMap;
import javax.swing.JPanel;
import shared.ui_ports.UiPort;

public class UiPortImpl extends UiPort {
    private Map<String, ImageElement> images;
    private team.model.Map map;
    private team.model.MainPlayer mainPlayer;
    private team.model.MainPlayer mainPlayer2;  // P2 in multiplayer mode
    private JPanel panel;

    public UiPortImpl(Map<String, ImageElement> images, team.model.Map map, JPanel panel) {
        this.images = images;
        this.map    = map;
        this.panel  = panel;
        this.mainPlayer2 = null;  // Initially null (solo mode)
    }

    @Override
    public void addImage(int imageId, String path, double x, double y, int w, int h, double angle, boolean visible) {
        images.put(String.valueOf(imageId), new ImageElement(path, (int) x, (int) y, w, h, angle, visible));
        panel.repaint();
    }

    @Override
    public void updateImage(int imageId, double x, double y, int w, int h, double angle, boolean visible) {
        ImageElement image = images.get(String.valueOf(imageId));
        if (image != null) {
            image.update((int) x, (int) y, w, h, angle, visible);
            panel.repaint();
        }
    }

    @Override
    public void setMap(team.model.Map newMap) {
        this.map = newMap;
        panel.repaint();
    }

    @Override
    public team.model.Map getMap() {
        return this.map;
    }

    @Override
    public void setMainPlayer(team.model.MainPlayer player) {
        this.mainPlayer = player;
        panel.repaint();
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
            panel.repaint();
        }
    }

    @Override
    public void setMainPlayer2(team.model.MainPlayer player) {
        this.mainPlayer2 = player;
        panel.repaint();
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
            panel.repaint();
        }
    }

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    @Override
    public void renderInitials() {
        if (panel instanceof DrawingPanel) {
            ((DrawingPanel) panel).setGameStarted(true);
        }
        panel.repaint();
    }
}
