package game.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class AudioAdapter {
    private String filePath;

    public AudioAdapter(String filePath) {
        this.filePath = filePath;
    }

    public void play(int repeat) throws AudioAdapterException {
        try {
            InputStream stream = ClassLoader
                    .getSystemClassLoader()
                    .getResourceAsStream(filePath);
            if(stream != null) {
                Clip sound = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
                sound.open(AudioSystem.getAudioInputStream(new BufferedInputStream(stream)));
                sound.loop(repeat);
            } else {
                throw new AudioAdapterException();
            }
        } catch (Exception e) {
            throw new AudioAdapterException();
        }
    }
}
