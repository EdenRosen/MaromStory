package base;

/**
 * Defines a route handler for one routing namespace
 */
public interface SubRouter {
    Object route(String subPath, Params p);
}