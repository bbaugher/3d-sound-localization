/*
 * This program reads in a wav file and applies a SyncKernel lowpass filter;
 * the output is the new wav file.
 * Usage: main in.wav out.wav
 */

import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;
//import edu.emory.mathcs.jtransforms.fft;
//import edu.emory.mathcs.utils.IOUtils;

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
			
			//System.out.println("Length = "+w1_numFrames);
			//DoubleFFT_1D w1_ft = new DoubleFFT_1D((int)w1_numFrames);
			//w1_ft.realForwardFull(w1_buffer[0]);

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

			out.writeFrames(newSound, new_frames);
			System.out.println("Sound File Written.");

			end = System.currentTimeMillis();
			total = end - start;

			System.out.println("Process took "+total+" milliseconds.");

		}
		catch(Exception e){
				System.err.println(e);
		}
		
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
