
import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;
import java.math.*;

class Run {

//A Class to test and benchmark Convolution

    public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("usage: main stero.wav hrtf.wav out.wav out2.wav");
			return;
		}

		String w1_filename = args[0];
		String w2_filename = args[1];
		String ds_out = args[2];
		String fft_out = args[3];

		// check that it has .wav extension
		if (! (w1_filename.toLowerCase().endsWith(".wav"))) {
			System.err.println("must use a .wav file!");
			return;
		}
		if (! (w2_filename.toLowerCase().endsWith(".wav"))) {
                        System.err.println("must use a .wav file!");
                        return;
                }
		if (! (ds_out.toLowerCase().endsWith(".wav"))) {
                        System.err.println("must use a .wav file!");
                        return;
                }
		if (! (fft_out.toLowerCase().endsWith(".wav"))) {
                        System.err.println("must use a .wav file!");
                        return;
                }
			

		Signal sound = new Signal(w1_filename);
		Signal hrtf = new Signal(w2_filename);

		//Convolution Direct Sum
		long start, end, total;	

		System.out.println("Convolution Sum Process");
                start = System.currentTimeMillis();	

		Signal convolveSum = new Signal(Convolution.convolveSum(w1_filename, w2_filename), sound.sampleRate);		
		convolveSum.writeWav(ds_out);	

		end = System.currentTimeMillis();
                total = end -start;

                System.out.println("Convolution Sum took "+total+" milliseconds.");

		//Convolution FFT
		System.out.println("FFT Process");
                start = System.currentTimeMillis();

		Signal convolveFFT = new Signal(Convolution.convolveFFT(0, 0, 0, w1_filename, 0, 0, 0, w2_filename), sound.sampleRate);
		convolveFFT.writeWav(fft_out);

		end = System.currentTimeMillis();
                total = end -start;

                System.out.println("FFT took "+total+" milliseconds.");

		System.exit(0);
	}
}
