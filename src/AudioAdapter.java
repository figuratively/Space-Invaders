import javax.sound.sampled.*;
import java.io.File;

public class AudioAdapter {
    private AudioInputStream audioInputStream;
    private DataLine.Info info;
    private AudioFormat format;
    private int size;
    private byte[] data;

    AudioAdapter(String filePath) throws AudioAdapterException {
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
            format = audioInputStream.getFormat();
            size = (int) (format.getFrameSize() * audioInputStream.getFrameLength());
            data = new byte[size];
            info = new DataLine.Info(Clip.class, format, size);
            audioInputStream.read(data, 0, size);
        } catch (Exception e) {
            throw new AudioAdapterException();
        }
    }

    public void play(int repeat) throws AudioAdapterException {
        try {
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(format, data, 0, size);
            clip.loop(repeat);
        } catch (Exception e) {
            throw new AudioAdapterException();
        }
    }
}
