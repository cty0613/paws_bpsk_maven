package capstone.paws;

import org.jtransforms.fft.DoubleFFT_1D;

public class FrequencyDetector {
    private static final double TARGET_FREQUENCY = 19000;
    private static final double SAMPLE_RATE = 48000;

    public double detectFrequencyMagnitude(byte[] audioData) {
        int n = audioData.length / 2;
        double[] samples = new double[n];
        for (int i = 0; i < n; i++) {
            int sample = (audioData[2 * i + 1] << 8) | (audioData[2 * i] & 0xFF);
            samples[i] = sample / 32768.0;
        }

        DoubleFFT_1D fft = new DoubleFFT_1D(n);
        double[] fftData = new double[2 * n];
        System.arraycopy(samples, 0, fftData, 0, n);
        fft.realForwardFull(fftData);

        int targetIndex = (int) Math.round(TARGET_FREQUENCY * n / SAMPLE_RATE);

        double re = fftData[2 * targetIndex];
        double im = fftData[2 * targetIndex + 1];
        double magnitude = Math.sqrt(re * re + im * im);

        return magnitude;
    }
}