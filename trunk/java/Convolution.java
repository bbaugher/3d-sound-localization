
import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;
import java.math.*;

class Convolution {

	//S = Sound 
	//L = Listener
	//L_hrtf = The subject # of the HRTF database 
	
	public static double[][] convolveFFT(double Sx, double Sy, double Sz, String Sa, double Lx, double Ly, double Lz, String hrtf_file){
		//Error Checking: Check for audio/file errors

		Signal sound = new Signal(Sa);
		Signal hrtf = new Signal(hrtf_file);

		int M = hrtf.numFrames;
		int TWO_M_MINUS_ONE = 2*M-1;
		int N_PLUS_M_MINUS_ONE = sound.numFrames + hrtf.numFrames -1;

		//What we will return
		double[][] output = new double[2][N_PLUS_M_MINUS_ONE];

		//The hrtf buffer that has been fourier transformed
		double[][] fft_hrtf = new double[hrtf.numChannels][2*TWO_M_MINUS_ONE];
		for(int i=0; i<hrtf.numChannels; i++){
			for(int j=0; j<hrtf.numFrames; j++)
				fft_hrtf[i][j] = hrtf.buffer[i][j];
		} 		

		for(int c=0; c<2; c++){
			//The sound signal segmented to work with FFT
			int d = c;
			if(c==1 && sound.numChannels==1) d = 0; 
                	double[][] fft_seg_sound = segmentSignal(sound.buffer[d], hrtf.numFrames);

			//Apply the Forward Fourier Transform to the filter kernel
                        DoubleFFT_1D hFFT = new DoubleFFT_1D(TWO_M_MINUS_ONE);
                        hFFT.realForwardFull(fft_hrtf[c]);

			//For each segment
			for(int i=0; i<fft_seg_sound.length; i++){
				//FT: Forward FT the sound segment and mixed HRTF
                                DoubleFFT_1D sFFT = new DoubleFFT_1D(TWO_M_MINUS_ONE);
                                sFFT.realForwardFull(fft_seg_sound[i]);

				//Complex Multiplication
				for(int j=0; j<2*TWO_M_MINUS_ONE; j+=2){
					double temp  = complexMultiplication(true, fft_seg_sound[i][j], fft_seg_sound[i][j+1], fft_hrtf[c][j], fft_hrtf[c][j+1]);
                                        fft_seg_sound[i][j+1] = complexMultiplication(false, fft_seg_sound[i][j], fft_seg_sound[i][j+1], fft_hrtf[c][j], fft_hrtf[c][j+1]);
					fft_seg_sound[i][j] = temp;
				}

				//FT: Inverse FT the product
                                sFFT.complexInverse(fft_seg_sound[i], true);
			}
			output[c] = reconstructSignal(fft_seg_sound, N_PLUS_M_MINUS_ONE);			
		}
		return output;
	}

	//Segments the signal based on the given length of the HRTF (Windowing)
	static private double[][] segmentSignal(double[] s, int length){
		int TWO_M_MINUS_ONE = 2*length - 1;	
		int segments = (int)Math.ceil((double)s.length/((double)length));
		double[][] seg_signal = new double[segments][2*(TWO_M_MINUS_ONE)];
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
		int m = (int)((s[0].length+2)/4);
		int TWO_M_MINUS_ONE = 2*m-1;
		for(int i=0; i<s.length; i++){
			//System.out.println("m="+m+", seg="+i+", i="+(i*m)+" to "+(i*m+s[i].length));
			for(int j=0; j<2*TWO_M_MINUS_ONE; j+=2){
				if((i*m+(j/2))<length)
					signal[i*m+(j/2)] += s[i][j];
			}
		}
		return signal;		
	}

	//Complex Multiplication for our buffers
	static private double complexMultiplication(boolean isReal, double real_a, double img_a, double real_b, double img_b){
		if(isReal)
			return (real_a*real_b) - (img_a*img_b);
		else
			return (real_a*img_b) + (real_b*img_a);
	}

	//Convolution Sum Algorithm
	public static double[][] convolveSum(String source_file, String hrtf_file){
		Signal hrtf = new Signal(hrtf_file);
		Signal source = new Signal(source_file);

		int len = hrtf.numFrames;
		double[][] result = new double[2][source.numFrames + len-1];

		for(int c=0; c<2; c++){
			int d = c;
			if(c==1 && source.numChannels==1) d = 0;
			for (int n = 0; n < result[0].length; n++) {
				double sum = 0;
				for (int i = 0; i<len; i++) {
					if (i<source.numFrames + (len-1) - n && i >= len-n-1) {
						sum += source.buffer[d][i+n-len+1]*hrtf.buffer[c][len-i-1];
					}
				}
				result[c][n] = (double) sum;
			}
		}
		return result;
	}
}
