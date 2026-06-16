package my_base;
import ai.ui.Ui;
import base.PeriodicScheduler;
import shared.MainRouter;
import shared.routers.SystemRouter;

public class App {

    private static MainRouter mainRouter = new MainRouter();
    private static Ui ui;
    private static AppContent content = new AppContent();
    
    // TO_DO: Register all routers here
    private static void registerRouters() {
        mainRouter.addRouter("system", new SystemRouter());
    }

    // Allows all classes in teh system to access content
    // entities from everywhere.
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
