
import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;
import java.math.*;

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
			
			//FT Stereo Sound Portion
			DoubleFFT_1D w1_ft = new DoubleFFT_1D((int)w1_numFrames);
			w1_ft.realForward(w1_buffer[0]);

			// Open the wav file specified as the second argument
			WavFile w2 = WavFile.openWavFile(new File(w2_filename));
			
			// Display information about the wav file
			w2.display();
			
			long w2_sampleRate = w2.getSampleRate();
			long w2_numChannels = w2.getNumChannels();
			long w2_numFrames = w2.getNumFrames();
			
			double[][] w2_buffer = new double[(int)w2_numChannels][(int)w2_numFrames];
			
			w2.readFrames(w2_buffer, (int)w2_numFrames);

			//FT HRTF Portion
			DoubleFFT_1D w2_ft = new DoubleFFT_1D((int)w2_numFrames);
			w2_ft.realForward(w2_buffer[0]);

			//Multiply FT Stereo Sound with FT HRTF

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

	//S = Sound 
	//L = Listener
	//L_hrtf = The subject # of the HRTF database 
	
	static public double[] convolve3D(double Sx, double Sy, double Sz, double[] Sa, double Lx, double Ly, double Lz, int L_hrtf){
		//Error Checking: Check for audio/file errors
		
		//Interpolation: Mix HRTF's using weighted average based on direction of sound

		//Segmentation: Segment audio based on length of HRTF

		//For each Segment		

			//FT: Forward FT the sound segment and mixed HRTF

			//Multiplication: Multiply the two FT signals together

			//FT: Inverse FT the product
	
			//Rebuild: Add the inverse segment into the new sound

		//Return new sound
		return null;
	}

	//Segments the signal based on the given length of the HRTF
	private double[][] segmentSignal(double[] s, int length){	
		if(s.length>length){
			int segments = (int)Math.ceil((double)s.length/((double)length));
			double[][] seg_signal = new double[segments][length];
			for(int i=0; i<segments; i++){
				for(int j=0; j<length; j++){
					if(i!=(segments-1))
						seg_signal[i][j] = s[j+i*length];
					else if((j+i*length)<s.length)
						seg_signal[i][j] = s[j+i*length];
					else
						seg_signal[i][j] = 0;
				}
			}
			return seg_signal;
		}
		else{
			double[][] seg_signal = new double[1][length];
			for(int i=0; i<length; i++){
				if(s.length<i)
					seg_signal[0][i] = s[i];
				else
					seg_signal[0][i] = 0;
			}
			return seg_signal;
		}
	}


	//Convolution Sum Algorithm
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
