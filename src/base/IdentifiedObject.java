package base;

/**
 * Gives model objects a shared numeric identity
 */
public abstract class IdentifiedObject {
    private final int id;

    protected IdentifiedObject(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}