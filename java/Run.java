
import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;
import java.math.*;

class Run {

    public static void main(String[] args) {
		if (args.length != 5) {
			System.err.println("usage: stero.wav x y z out.wav");
			return;
		}

		String w1_filename = args[0];
		String lr = args[1];
		String ud = args[3];
		String fb = args[2];
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
		if (! (fft_out.toLowerCase().endsWith(".wav"))) {
                        System.err.println("must use a .wav file!");
                        return;
                }
			

		Signal sound = new Signal(w1_filename);

		//Convolution Direct Sum
		long start;	

		//Convolution FFT
		System.out.println("Convolving audio...");
                start = System.currentTimeMillis();

		String[] subjects = {"003", "010", "018", "020", "021", "027", "028", "033", "040", "044"};

		//j => leftright
		//p => frontback	

		Signal convolveFFT = new Signal(Convolution.convolveFFT(leftright, frontback, updown, sound.buffer[0], "003"), sound.sampleRate);
		convolveFFT.writeWav(fft_out);

		long end = System.currentTimeMillis();
                double total = (end -start)/1000.0;
		System.out.println("Convolution took "+total+" seconds.");
		System.exit(0);
	}
}
