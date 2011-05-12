
import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;
import java.math.*;

class Convolution {

	//S = Sound 
	//L = Listener
	//L_hrtf = The subject # of the HRTF database 
	
	//@author: Bryan Baugher
	//convolveFFT - This is the method which is called to convolve a sound using the FFT algorithm
	//It handles everything from interpolation, to integration, to the actual FFT algorithm and segmenting the audio
	public static double[][] convolveFFT(double Sx, double Sy, double Sz, double[] source_sound, String subject){
		//Error Checking: Check for audio/file errors

		double[][] interpolatedHRTF = Interpolation.getInterpolatedHrtfBuffer(subject, Sx, Sy, Sz);
		
		//if(interpolatedHRTF==null) System.out.println("NULL");
		if(interpolatedHRTF==null){
			try{
				/*
                                FileWriter fstream = new FileWriter("errors.txt", true);
                                BufferedWriter out = new BufferedWriter(fstream);
				double[] ele_az = convertVectorsToDegrees(Sx, Sy, Sz);			
                                out.write("Error (NULL) for subject ("+subject+") at (ele, az) ( "+ele_az[0]+", "+ele_az[1]+")\n");
                                out.close();*/
				System.out.println("Error--NULL POINTER EXCEPTION IN CONVOLUTION");
				return null;
                        }
                        catch(Exception e1){
                                System.out.println(e1.getMessage());
                        }
		}
		double[][] hrtf = Interpolation.getInterpolatedHrtfBuffer(subject, Sx, Sy, Sz);

		int M = hrtf[0].length;
		int TWO_M_MINUS_ONE = 2*M-1;
		int N_PLUS_M_MINUS_ONE = source_sound.length + hrtf[0].length -1;

		//What we will return
		double[][] output = new double[2][N_PLUS_M_MINUS_ONE];

		//The hrtf buffer that has been fourier transformed
		double[][] fft_hrtf = new double[hrtf.length][2*TWO_M_MINUS_ONE];
		for(int i=0; i<hrtf.length; i++){
			for(int j=0; j<hrtf[0].length; j++)
				fft_hrtf[i][j] = hrtf[i][j];
		} 		

		for(int c=0; c<2; c++){
			//The sound signal segmented to work with FFT
                	double[][] fft_seg_sound = segmentSignal(source_sound, hrtf[0].length);

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

	//@author: Bryan Baugher
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
	
	//@author: Bryan Baugher
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
	//@author: Bryan Baugher
	//Complex Multiplication for our buffers
	static private double complexMultiplication(boolean isReal, double real_a, double img_a, double real_b, double img_b){
		if(isReal)
			return (real_a*real_b) - (img_a*img_b);
		else
			return (real_a*img_b) + (real_b*img_a);
	}

	//@author: Bryan Baugher
	//Convolution Sum Algorithm - Direct convolution algorithm
	/*
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
	}*/
}
