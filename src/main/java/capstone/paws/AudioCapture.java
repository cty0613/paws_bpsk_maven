package capstone.paws;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

public class AudioCapture {

    private TargetDataLine targetDataLine;

    public void start() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(48000, 16, 1, true, false); // 엔디안 설정을 false로 변경
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
        targetDataLine.open(format);
        targetDataLine.start();
    }

    public byte[] captureAudio(int durationMillis) {
        int numBytesRead;
        byte[] data = new byte[targetDataLine.getBufferSize() / 5];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesToCapture = (int) (durationMillis / 1000.0 * 48000 * 2); // 16비트(2바이트) 샘플

        while (out.size() < bytesToCapture) {
            numBytesRead = targetDataLine.read(data, 0, data.length);
            out.write(data, 0, numBytesRead);
        }

        return out.toByteArray();
    }

    public void stop() {
        targetDataLine.stop();
        targetDataLine.close();
    }
}
