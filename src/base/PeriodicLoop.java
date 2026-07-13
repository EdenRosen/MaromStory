package base;

/**
 * Defines work that runs repeatedly on a scheduler
 */
public abstract class  PeriodicLoop {

	private static long time = 0;





	public void execute() {
		time += PeriodicScheduler.periodicInterval;

	}

	public static long elapsedTime() {
		return time;
	}

}
