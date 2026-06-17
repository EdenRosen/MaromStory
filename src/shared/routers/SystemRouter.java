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
                    // Player 1 (Arrow Keys + Space)
                    case "left":   backend.startMoveLeft();  break;
                    case "right":  backend.startMoveRight(); break;
                    case "up":     backend.playerJump();     break;
                    
                    // Player 2 (WASD + Shift)
                    case "left_p2":   backend.startMoveLeft_p2();  break;
                    case "right_p2":  backend.startMoveRight_p2(); break;
                    case "up_p2":     backend.playerJump_p2();     break;
                    
                    // Shared actions
                    case "pickup": backend.attemptPickup();  break;
                    case "throw":  backend.throwSword();     break;
                    case "pickup_p2": backend.attemptPickup_p2();  break;
                    case "throw_p2":  backend.throwSword_p2();     break;
                    case "start":  backend.startScenario();     break;
                    case "attack": backend.attackEnemy();       break;
                    case "attack_p2": backend.attackEnemy_p2();  break;
                    case "skill1": backend.onNumberKey(0);     break;
                    case "skill2": backend.onNumberKey(1);     break;
                    case "skill3": backend.onNumberKey(2);     break;
                    case "skill4": backend.onNumberKey(3);     break;
                    case "skill5": backend.onNumberKey(4);     break;
                    
                    // Selection / Navigation
                    case "selectHero":      backend.cycleHero();            break;
                    case "selectGameMode":  backend.cycleGameMode();        break;
                    case "shopTab":         backend.cycleShopPage();        break;
                    case "upgradePanel":    backend.toggleUpgradePanel();   break;
                    case "mapSelect":       backend.toggleMapSelect();      break;
                    case "shop":            backend.toggleShop();           break;
                }
                return null;
            }

            case "/key/up": {
                String key = p.getString(0);
                switch (key) {
                    case "left":
                    case "right":
                        backend.stopMove();
                        break;
                    case "left_p2":
                    case "right_p2":
                        backend.stopMove_p2();
                        break;
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
