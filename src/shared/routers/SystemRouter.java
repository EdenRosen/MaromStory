package shared.routers;

import base.Params;
import base.SubRouter;
import my_base.App;
import team.control.Backend;

public class SystemRouter implements SubRouter {

    private final Backend backend;

    public SystemRouter() {
        this.backend = App.content().backend();
    }

    @Override
    public Object route(String subPath, Params p) {
        switch (subPath) {

            case "/init":
                backend.initializeApp();
                return null;

            case "/start":
                backend.startScenario();
                return null;

            // תרגום מקשים לפעולות — Router אחראי על המיפוי, Backend לא יודע כלל על מקשים
            case "/key/down": {
                String key = p.getString(0);
                switch (key) {
                    case "left":   backend.startMoveLeft();  break;
                    case "right":  backend.startMoveRight(); break;
                    case "up":     backend.playerJump();     break;
                    case "pickup": backend.attemptPickup();  break;
                    case "throw":  backend.throwSword();     break;
                    case "start":  backend.startScenario();     break;
                    case "attack": backend.attackEnemy();       break;
                    case "skill1": backend.switchAttack(0);    break;
                    case "skill2": backend.switchAttack(1);    break;
                    case "selectHero": backend.cycleHero();    break;
                }
                return null;
            }

            case "/key/up": {
                String key = p.getString(0);
                switch (key) {
                    case "left":
                    case "right": backend.stopMove(); break;
                }
                return null;
            }

            case "/reset": {
                backend.resetScenario();
                return null;
            }

            default:
                throw new RuntimeException("Unknown system route: " + subPath);
        }
    }
}
