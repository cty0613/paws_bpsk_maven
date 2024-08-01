package capstone.paws;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Arrays;

public class BPSKDecoder {
    public static int[] decodeAudioData(byte[] audioBytes, double carrierFreq, int sampleRate, double bitDuration) throws IOException, UnsupportedAudioFileException {
        int bytesPerSample = 2;
        int numSamples = audioBytes.length / bytesPerSample;
        double[] signal = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            signal[i] = (short) ((audioBytes[i * 2] & 0xFF) | (audioBytes[i * 2 + 1] << 8)); // 리틀 엔디안 처리
        }

        double maxAmplitude = Arrays.stream(signal).map(Math::abs).max().orElse(1.0);
        for (int i = 0; i < numSamples; i++) {
            signal[i] /= maxAmplitude;
        }

        int bitDurationSamples = (int) (bitDuration * sampleRate);
        int numBits = numSamples / bitDurationSamples;

        return demodulateBpsk(signal, carrierFreq, sampleRate, bitDuration, numBits);
    }

    public static int[] demodulateBpsk(double[] signal, double carrierFreq, int sampleRate, double bitDuration, int numBits) {
        int numSamples = signal.length;
        double[] t = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            t[i] = i / (double) sampleRate;
        }

        int bitDurationSamples = (int) (bitDuration * sampleRate);
        int[] demodulatedBits = new int[numBits];

        for (int i = 0; i < numBits; i++) {
            int start = i * bitDurationSamples;
            int end = (i + 1) * bitDurationSamples;

            // 시작과 끝 인덱스를 조정하여 세그먼트의 길이를 맞추기
            if (end > numSamples) {
                end = numSamples;
                start = Math.max(0, numSamples - bitDurationSamples);
            }

            double[] segment = Arrays.copyOfRange(signal, start, end);
            double[] carrierWave = new double[segment.length];
            for (int j = 0; j < segment.length; j++) {
                carrierWave[j] = Math.cos(2 * Math.PI * carrierFreq * t[start + j]);
            }

            double integral = 0.0;
            for (int j = 0; j < segment.length; j++) {
                integral += segment[j] * carrierWave[j];
            }
            demodulatedBits[i] = integral < 0 ? 0 : 1;
        }

        return demodulatedBits;
    }
}
