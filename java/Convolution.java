
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

			System.out.println("Starting Convolution Sum.");

			long start, end, total, cstart, cend;
			start = System.currentTimeMillis();

			newSound[0] = convolve_double(w2_buffer[0], w1_buffer[0]);
			System.out.println("Left Channel Finished");
			newSound[1] = convolve_double(w2_buffer[1], w1_buffer[0]);
			System.out.println("Right Channel Finished");

			out.writeFrames(newSound, new_frames);
			System.out.println("Sound File Written.");

			end = System.currentTimeMillis();
			long total1 = end - start;

			System.out.println("Process took "+total1+" milliseconds.");

			System.out.println("FFT Process");
			start = System.currentTimeMillis();
			
			//FFT Method
			double[][] finalSound = convolve3D(0, 0, 0, w1_filename, 0, 0, 0, "1002", w2_filename);	
			WavFile new_out = WavFile.newWavFile(new File("out2.wav"), 2, finalSound[0].length, 16, sampleRate);
			new_out.writeFrames(finalSound, finalSound[0].length);
			System.out.println("Sound file written.");	
			//FFT Method
	
			end = System.currentTimeMillis();
			total = end -start;
	
			System.out.println("Process took "+total+" milliseconds.");
			System.out.println("Convolution Sum vs FFT "+(total1/total)+" to 1");
			System.exit(0);
		}
		catch(Exception e){
				System.err.println(e);
		}
		
	}

	//S = Sound 
	//L = Listener
	//L_hrtf = The subject # of the HRTF database 
	
	static public double[][] convolve3D(double Sx, double Sy, double Sz, String Sa, double Lx, double Ly, double Lz, String L_hrtf, String hrtf_file){
		//Error Checking: Check for audio/file errors

		try{
			// Open the wav file for the sound
                	WavFile Sound_wave = WavFile.openWavFile(new File(Sa));	

			//Read the file into a buffer
			double[][] sound_buffer = new double[2][(int)Sound_wave.getNumFrames()];
			Sound_wave.readFrames(sound_buffer, (int)Sound_wave.getNumFrames());		

			//If sound file only has one channel copy it to the second channel
			if(Sound_wave.getNumChannels()==1){
				for(int i=0; i<sound_buffer[0].length; i++)
					sound_buffer[1][i] = sound_buffer[0][i];
			}

			// Open the wav file for the HRTF
			//String file_name = "../../HRTF Database/Raw/IRC_"+L_hrtf+"_R/IRC_"+L_hrtf+"_R_R0195_T"+h+"_P"+a+".wav";
			WavFile hrtf_wave = WavFile.openWavFile(new File(hrtf_file));

			//Read the file
			double[][] hrtf_buffer = new double[(int)hrtf_wave.getNumChannels()][(2*(int)hrtf_wave.getNumFrames()-1)];
			hrtf_wave.readFrames(hrtf_buffer, (int)hrtf_wave.getNumFrames());		

			double[][] output_signal = new double[2][(sound_buffer[0].length+(int)hrtf_wave.getNumFrames()-1)];

			//Interpolation: Mix HRTF's using weighted average based on direction of sound

			//For each channel
			for(int c=0; c<sound_buffer.length; c++){
				
				System.out.println("Segmenting Channel "+(c+1));
				//Segmentation: Segment audio based on length of HRTF
				double[][] segmented_buffer = segmentSignal(sound_buffer[c], (int)hrtf_wave.getNumFrames());	
			
				//Pad the end of the hrtf_buffer with 0's
				for(int j=(int)hrtf_wave.getNumFrames(); j<hrtf_buffer[c].length; j++)
					hrtf_buffer[c][j] = 0;
			
				//Apply the Forward Fourier Transform to the filter kernel
				DoubleFFT_1D hFFT = new DoubleFFT_1D(hrtf_buffer[c].length);
				hFFT.realForward(hrtf_buffer[c]);

				//For each Segment
				for(int i=0; i<segmented_buffer.length; i++){
					
					System.out.println("FFT of segment "+(i+1)+" of Channel "+(c+1));
					//FT: Forward FT the sound segment and mixed HRTF
					DoubleFFT_1D sFFT = new DoubleFFT_1D(segmented_buffer[i].length);
        	        	        sFFT.realForward(segmented_buffer[i]);	

					//DoubleFFT_1D hFFT = new DoubleFFT_1D(hrtf_buffer[c].length);
					//hFFT.realForward(hrtf_buffer[c]);

					//Multiplication: Multiply the two FT signals together
					for(int j=0; j<segmented_buffer[i].length; j++)
						segmented_buffer[i][j] *= hrtf_buffer[c][j];

					//FT: Inverse FT the product
					sFFT.realInverse(segmented_buffer[i], false);	
				}

				System.out.println("Reconstructing Channel "+(c+1));
				//Rebuild: Add the inverse segment into the new sound
				System.out.println((int)hrtf_wave.getNumFrames()+"+"+sound_buffer[0].length+"-1 ="+output_signal[c].length);
				output_signal[c] = reconstructSignal(segmented_buffer, output_signal[c].length);
			}	
		
			//Return new sound
			return output_signal;
		}
		catch(Exception e){
                                System.err.println(e);
                }

		return null;
	}

	//Segments the signal based on the given length of the HRTF (Windowing)
	static private double[][] segmentSignal(double[] s, int length){	
		int segments = (int)Math.ceil((double)s.length/((double)length));
		double[][] seg_signal = new double[segments][(2*length-1)];
		for(int i=0; i<segments; i++){
			for(int j=0; j<seg_signal[0].length; j++)
				seg_signal[i][j] = 0;
		}
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

	//Reconstructs the signal from the segmented version (Overlap and Add Algorithm)
	static private double[] reconstructSignal(double[][] s, int length){
		double[] signal = new double[length];
		for(int i=0; i<length; i++) signal[i] = 0;
		int m = (int)((s[0].length+1)/2);
		for(int i=0; i<s.length; i++){
			//System.out.println("m="+m+", seg="+i+", i="+(i*m)+" to "+(i*m+s[i].length));
			for(int j=0; j<s[i].length; j++){
				if((i*m+j)<length)
					signal[i*m+j] += s[i][j];
			}
		}
		return signal;		
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
