package shared.routers;

import base.Params;
import base.SubRouter;
import my_base.App;
import team.control.Backend;

// Translates system routes into backend actions

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

            // This route is called when the user presses a key down, then the UI calls the backend to perform the action associated with that key, which first goes through the SystemRouter to determine which action to perform
            case "/key/down": {
                String key = p.getString(0);
                
                switch (key) {

                    case "left":   backend.startMoveLeft();  break;
                    case "right":  backend.startMoveRight(); break;
                    case "up":     backend.playerJump();     break;


                    case "left_p2":   backend.startMoveLeft_p2();  break;
                    case "right_p2":  backend.startMoveRight_p2(); break;
                    case "up_p2":     backend.playerJump_p2();     break;


                    case "pickup": backend.attemptPickup();  break;
                    case "throw":  backend.throwSword();     break;
                    case "pickup_p2": backend.attemptPickup_p2();  break;
                    case "throw_p2":  backend.throwSword_p2();     break;
                    case "start":  backend.startScenario();     break;
                    case "attack": backend.attackOrThrow();     break;
                    case "attack_p2": backend.attackOrThrow_p2(); break;
                    case "nextAttack": backend.cycleAttack();   break;
                    case "nextAttack_p2": backend.cycleAttack_p2(); break;
                    case "skill1": backend.onNumberKey(0);     break;
                    case "skill2": backend.onNumberKey(1);     break;
                    case "skill3": backend.onNumberKey(2);     break;
                    case "skill4": backend.onNumberKey(3);     break;
                    case "skill5": backend.onNumberKey(4);     break;
                    case "skill1_p2": backend.onPlayerTwoNumberKey(0); break;
                    case "skill2_p2": backend.onPlayerTwoNumberKey(1); break;
                    case "skill3_p2": backend.onPlayerTwoNumberKey(2); break;
                    case "skill4_p2": backend.onPlayerTwoNumberKey(3); break;
                    case "skill5_p2": backend.onPlayerTwoNumberKey(4); break;


                    case "selectHero":      backend.cycleHero();            break;
                    case "selectGameMode":  backend.cycleGameMode();        break;
                    case "shopTab":         backend.cycleShopPage();        break;
                    case "upgradePanel":    backend.toggleUpgradePanel();   break;
                    case "mapSelect":       backend.toggleMapSelect();      break;
                    case "shop":            backend.toggleShop();           break;
                }
                return null;
            }

            // This route is called when the user releases a key, which stops a player from moving left or right
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

            case "/home": {
                backend.goHome();
                return null;
            }

            default:
                throw new RuntimeException("Unknown system route: " + subPath);
        }
    }
}
