package my_base;

import base.PeriodicLoop;

public class MyPeriodicLoop extends PeriodicLoop {

	private AppContent content = App.content();

	@Override
	public void execute() {
		super.execute();
		// עדכון השחקן בכל tick — פיזיקה + ציור
		content.backend().updatePlayer();
	}

}
