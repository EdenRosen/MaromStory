package my_base;

import team.control.Backend;
import team.model.Canvas;

/*
 * This class should hold the content of the system, i.e., all elements that are
 * related to the essence of the system.
 * 
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
