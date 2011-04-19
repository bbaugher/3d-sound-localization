
import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;
import java.math.*;

class Run {

//A Class to test and benchmark Convolution

    public static void main(String[] args) {
		if (args.length != 5) {
			System.err.println("usage: stero.wav x y z out.wav");
			return;
		}

		String w1_filename = args[0];
		String lr = args[1];
		String ud = args[3];
		String fb = args[2];
		//String w2_filename = args[1];
		//String ds_out = args[2];
		String fft_out = args[4];

		double leftright = 0;
		double updown = 0;
		double frontback = 0;		

		try{
    			leftright = Double.valueOf(lr).doubleValue();
	                updown = Double.valueOf(ud).doubleValue();
        	        frontback = Double.valueOf(fb).doubleValue();
			
			if(leftright>1 || leftright<-1){
                 	       System.err.println("LeftRight value must be between -1.0 and 1.0");
                        	return;
                	}
                	if(updown>1 || updown<-1){
                	        System.err.println("UpDown value must be between -1.0 and 1.0");
                        	return;
                	}
                	if(frontback>1 || frontback<-1){
               	        	System.err.println("FrontBack value must be between -1.0 and 1.0");
                        	return;
                	}
		}
    		catch (NumberFormatException e){
      			System.out.println("NumberFormatException: " + e.getMessage());
    		}
		
		// check that it has .wav extension
		if (! (w1_filename.toLowerCase().endsWith(".wav"))) {
			System.err.println("must use a .wav file!");
			return;
		}
		if(leftright>1 || leftright<-1){
			System.err.println("LeftRight value must be between -1.0 and 1.0");
			return;
		}
		if(updown>1 || updown<-1){
                        System.err.println("UpDown value must be between -1.0 and 1.0");
                        return;
                }
		if(frontback>1 || frontback<-1){
                        System.err.println("FrontBack value must be between -1.0 and 1.0");
                        return;
                }
		/*
		if (! (w2_filename.toLowerCase().endsWith(".wav"))) {
                        System.err.println("must use a .wav file!");
                        return;
                }
		if (! (ds_out.toLowerCase().endsWith(".wav"))) {
                        System.err.println("must use a .wav file!");
                        return;
                }
		*/
		if (! (fft_out.toLowerCase().endsWith(".wav"))) {
                        System.err.println("must use a .wav file!");
                        return;
                }
			

		Signal sound = new Signal(w1_filename);
		//Signal hrtf = new Signal(w2_filename);

		//Convolution Direct Sum
		long start, end, total;	

		/*
		System.out.println("Convolution Sum Process");
                start = System.currentTimeMillis();	

		Signal convolveSum = new Signal(Convolution.convolveSum(w1_filename, w2_filename), sound.sampleRate);		
		convolveSum.display();
		convolveSum.writeWav(ds_out);	

		end = System.currentTimeMillis();
                total = end -start;

                System.out.println("Convolution Sum took "+total+" milliseconds.");
		*/
		//Convolution FFT
		System.out.println("FFT Process");
                start = System.currentTimeMillis();

		Signal convolveFFT = new Signal(Convolution.convolveFFT(leftright, frontback, updown, sound.buffer[0], "003", sound.sampleRate), sound.sampleRate);	
		//Signal convolveFFT = new Signal(Convolution.convolveFFT(0, 0, 0, w1_filename, 0, 0, 0, w2_filename), sound.sampleRate);
		//convolveFFT.display();
		convolveFFT.writeWav(fft_out);

		end = System.currentTimeMillis();
                total = end -start;

                System.out.println("FFT took "+total+" milliseconds.");

		System.exit(0);
	}
}
