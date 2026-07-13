package my_base;
import ai.ui.Ui;
import base.PeriodicScheduler;
import shared.MainRouter;
import shared.routers.SystemRouter;

/**
 * Starts the application and wires the main router and user interface
 */
public class App {

    private static MainRouter mainRouter = new MainRouter();
    private static Ui ui;
    private static AppContent content = new AppContent();


    private static void registerRouters() {
        mainRouter.addRouter("system", new SystemRouter());
    }



    public static AppContent content() {
        return content;
    }

    public static Ui UI() {
        return ui;
    }

    public static void main(String[] args) throws Exception {
        content.initContent();
        ui = new Ui();
        ui.setUiPorts();
        registerRouters();
        ui.start(mainRouter);
        PeriodicScheduler scheduler = new PeriodicScheduler();
        scheduler.setPeriodicLoop(new MyPeriodicLoop());
        scheduler.start();
    }

}
