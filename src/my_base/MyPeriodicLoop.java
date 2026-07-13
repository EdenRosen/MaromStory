package my_base;

import base.PeriodicLoop;

/**
 * Updates the game backend on each scheduler tick
 */
public class MyPeriodicLoop extends PeriodicLoop {

	private AppContent content = App.content();

	@Override
	public void execute() {
		super.execute();

		content.backend().updatePlayer();
	}

}
