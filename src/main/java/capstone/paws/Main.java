package capstone.paws;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;


public class Main {
    private static final int BUFFER_SIZE = 1024;  // 버퍼의 크기
    private static final String TARGET_PATTERN = "1010";  // 종료 조건 패턴

    public static void main(String[] args) {
        double carrierFreq = 19000;  // 19 kHz
        int sampleRate = 48000;      // 48.0 kHz
        double bitDuration = 1.0 / 16; // 0.0625초 (1초에 16비트)

        AudioCapture audioCapture = new AudioCapture();
        FrequencyDetector detector = new FrequencyDetector();
        try {
            audioCapture.start();
            while (true) {
                // 1초 동안의 오디오 데이터를 캡처
                byte[] audioData = audioCapture.captureAudio(500); // 0.5초
                double magnitude = detector.detectFrequencyMagnitude(audioData);
                if (magnitude >= 1) { // 주파수 크기가 임계값 이상인 경우
                    try {
                        int[] decodedBits = BPSKDecoder.decodeAudioData(audioData, carrierFreq, sampleRate, bitDuration);
                        String bitString = convertBitsToString(decodedBits);
                        System.out.println("Decoded Bits: " + bitString);

                        // 종료 조건 패턴이 bitString에 포함되어 있는지 확인
                        if (bitString.contains(TARGET_PATTERN)) {
                            System.out.println("Target pattern found. Exiting...");
                            break;  // 종료
                        }
                    } catch (IOException | UnsupportedAudioFileException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("No significant signal detected.");
                }
                Thread.sleep(500); // 0.5초 대기
            }
        } catch (LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            audioCapture.stop();
        }
    }

    private static String convertBitsToString(int[] bits) {
        StringBuilder sb = new StringBuilder();
        for (int bit : bits) {
            sb.append(bit);
        }
        return sb.toString();
    }

}
