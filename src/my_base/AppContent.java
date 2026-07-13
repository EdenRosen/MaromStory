package my_base;

import team.control.Backend;
import team.model.Canvas;






/**
 * Stores shared application services used across the game
 */
public class AppContent {
	private Canvas canvas = new Canvas();
	private Backend backend;

	public void initContent() {
		backend = new Backend();
		canvas.initCanvas();
	};

	public Canvas canvas() {
		return canvas;
	}
	public Backend backend() {
		return backend;
	}
}
