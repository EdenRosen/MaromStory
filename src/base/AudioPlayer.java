package base;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Provides simple audio playback support for game sounds
 */
public class AudioPlayer {

	public enum MusicStatus{
		PLAYING,
		STOPPED
	}

	private static Clip clip;
	private static AudioInputStream audioInputStream;
	private static MusicStatus status = MusicStatus.STOPPED;






	public static void play(String musicPath, int numIterations) {
		File musicFile = new File(musicPath);
		try {
			audioInputStream = AudioSystem.getAudioInputStream(musicFile.getAbsoluteFile());
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.loop(numIterations - 1);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		clip.start();
		status = MusicStatus.PLAYING;
	}

	public static void stop() {
		clip.stop();
		status = MusicStatus.STOPPED;
	}

	public static void close() {
		clip.close();
	}


	public static MusicStatus getStatus() {
		return status;
	}

}
