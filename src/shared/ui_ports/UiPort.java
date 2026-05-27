package shared.ui_ports;

public abstract class UiPort {

    private static UiPort instance;

    public static void setInstance(UiPort ui) {
        if (ui == null) throw new IllegalArgumentException("UiPort instance cannot be null");
        if (instance != null) throw new IllegalStateException("UiPort instance already set");
        instance = ui;
    }

    public static UiPort getInstance() {
        if (instance == null) throw new IllegalStateException("UiPort instance not set yet");
        return instance;
    }

    public abstract void addImage(int imageId, String path, double x, double y, int w, int h, double angle, boolean visible);
    public abstract void updateImage(int imageId, double x, double y, int w, int h, double angle, boolean visible);

    public abstract void setMap(team.model.Map map);
    public abstract team.model.Map getMap();

    public abstract void setMainPlayer(team.model.MainPlayer player);
    public abstract team.model.MainPlayer getMainPlayer();
    public abstract void updatePlayerPosition(double x, double y);

    public abstract void log(String message);

    public abstract void renderInitials();
}
