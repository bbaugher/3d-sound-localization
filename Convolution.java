/*
 * This program reads in a wav file and applies a SyncKernel lowpass filter;
 * the output is the new wav file.
 * Usage: main in.wav out.wav
 */

import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;

class Convolution {

    public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("usage: main stero.wav hrtf.wav out.wav");
			return;
		}
		String infilename = args[0];
		// check that it has .wav extension
		if (! (infilename.toLowerCase().endsWith(".wav"))) {
			System.err.println("must use a .wav file!");
			return;
		}
		String w1_filename = args[0];
		String w2_filename = args[1];
		String outfileName = args[2];
		// check that it has .wav extension
		if (! (outfileName.toLowerCase().endsWith(".wav"))) {
			System.err.println("must use a .wav file!");
			return;
		}
		File outfile = new File(outfileName);

		/*
		//Original WavReader Version

		WavReader wr = new WavReader(new File(infilename));

		// load the entire sound into memory

		int theSize = wr.getNumSamples();

		short[] theSound = wr.getShortSamples(); // new int[theSize];

		// result, also in memory, actual space allocated by convolve
		short [] theResult;

		// files are now open/created/loaded

		int samplerate = wr.getSampleRate();
		System.err.println("Sample rate: " + samplerate + ", sample size: " +
				wr.getSampleSize());

		double lowerFrequency = 600; // in Hertz
		double upperFrequency = 1200;
		System.err.println("Cutoff frequencies: lower=" + lowerFrequency+ " upper=" +
			upperFrequency);

		//======================================================================

		// first kernel

		int len1 = 1000;	// random value
		double[] k1 = new double[len1];

		// now initialize k1

		// here we do the convolution operation; may need twice for bandpass!
		theResult = convolve(theSound, k1);

		// now output the result
		WavReader.writeWav(theResult, samplerate, outfile);

		*/

		//WavFile Version
		try
		{
			// Open the wav file specified as the first argument
			WavFile w1 = WavFile.openWavFile(new File(w1_filename));
			
			// Display information about the wav file
			w1.display();
			
			long w1_sampleRate = w1.getSampleRate();
			long w1_numChannels = w1.getNumChannels();
			long w1_numFrames = w1.getNumFrames();
			
			double[][] w1_buffer = new double[(int)w1_numChannels][(int)w1_numFrames];
			
			w1.readFrames(w1_buffer, (int)w1_numFrames);
			
			// Open the wav file specified as the second argument
			WavFile w2 = WavFile.openWavFile(new File(w2_filename));
			
			// Display information about the wav file
			w2.display();
			
			long w2_sampleRate = w2.getSampleRate();
			long w2_numChannels = w2.getNumChannels();
			long w2_numFrames = w2.getNumFrames();
			
			double[][] w2_buffer = new double[(int)w2_numChannels][(int)w2_numFrames];
			
			w2.readFrames(w2_buffer, (int)w2_numFrames);

			int new_frames = (int)w1_numFrames+(int)w2_numFrames-1;
			double[][] newSound = new double[2][new_frames];

			int sampleRate = (int)w1_sampleRate;

			WavFile out = WavFile.newWavFile(new File(args[2]), 2, new_frames, 16, sampleRate);

			long start, end, total, cstart, cend;
			start = System.currentTimeMillis();

			newSound[0] = convolve_double(w2_buffer[0], w1_buffer[0]);
			System.out.println("Left Channel Finished");
			newSound[1] = convolve_double(w2_buffer[1], w1_buffer[0]);
			System.out.println("Right Channel Finished");

			/*

			//An attempt at using FFT convolution

			Complex[][] w1_complex = new Complex[(int)w1_numChannels][(int)w1_numFrames]; 
			for(int i=0; i<(int)w1_numChannels; i++){
				for(int j=0; j<(int)w1_numFrames; j++){
					w1_complex[i][j] = new Complex(w1_buffer[i][j], 0);
				}
			}

			Complex[][] w2_complex = new Complex[(int)w2_numChannels][(int)w2_numFrames];
                        for(int i=0; i<(int)w2_numChannels; i++){
                                for(int j=0; j<(int)w2_numFrames; j++){
                                        w2_complex[i][j] = new Complex(w2_buffer[i][j], 0);
                                }
                        }

			cstart = System.currentTimeMillis();

			Complex[][] final_complex = new Complex[2][new_frames];
			final_complex[0] = FFT.convolve(w1_complex[0], w2_complex[0]);
			final_complex[1] = FFT.convolve(w1_complex[0], w2_complex[1]);
			
			for(int i=0; i<2; i++){
				for(int j=0; j<new_frames; j++){
					newSound[i][j] = final_complex[i][j].re();
				}
			}			

			cend = System.currentTimeMillis();

			*/

			out.writeFrames(newSound, new_frames);
			System.out.println("Sound File Written.");

			end = System.currentTimeMillis();
			total = end - start;

			System.out.println("Process took "+total+" milliseconds.");
			//System.out.println("Convolution took "+(cend-cstart)+" milliseconds.");

		}
		catch(Exception e){
				System.err.println(e);
		}
		
	}

	/*
	 * I'm giving this to you, as the implementation is rather messy.
	 * This is the version with full "tails" implemented: a value is assigned
	 * whenever samples and the kernel have any overlap at all.
	 * As of June 19, it is only partially tested.
	 */
	static public short[] convolve(short[] samples, double[] kernel) {
		int len = kernel.length;
		short[] result = new short[samples.length + len-1];
		for (int n = 0; n < result.length; n++) {
			double sum = 0;
			for (int i = 0; i<len; i++) {
				if (i<samples.length + (len-1) - n && i >= len-n-1) {
					sum += samples[i+n-len+1]*kernel[len-i-1];
				}
			}
			result[n] = (short) sum;

		}
		return result;
	}

	static public double[] convolve_double(double[] HRTF, double[] source){
		int len = HRTF.length;
		double[] result = new double[source.length + len-1];
		for (int n = 0; n < result.length; n++) {
			double sum = 0;
			for (int i = 0; i<len; i++) {
				if (i<source.length + (len-1) - n && i >= len-n-1) {
					sum += source[i+n-len+1]*HRTF[len-i-1];
				}
			}
			result[n] = (double) sum;

		}
		return result;
	}
}
