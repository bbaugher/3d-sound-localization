
import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;
import java.math.*;

class Create_Customization_Files {

//A Class to test and benchmark Convolution

    public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("usage: stero.wav volume");
			return;
		}

		String w1_filename = args[0];
		String vol = args[1];

		double volume = 0;

		try{
    			volume = Double.valueOf(vol).doubleValue();
			
                	if(volume<=0){
               	        	System.err.println("Volume must be greater than zero.");
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

		Signal sound = new Signal(w1_filename);
		sound.display();

		long start;	

		//Convolution FFT
		System.out.println("Convolving Sounds...");
                start = System.currentTimeMillis();

		String[] subjects = {"003", "010", "018", "020", "021", "027", "028", "033", "040", "044"};

		//j => leftright
		//p => frontback	

		for(int i=0; i<subjects.length; i++){
			for(int j=-1; j<=1; j+=2){
				for(int p=-1; p<=1; p+=2){
					String output = "customization/"+subjects[i];
					if(j==-1){
						if(p==-1) output = output+"_1.wav";
						else output = output+"_4.wav";
					}
					else{
						if(p==-1) output = output+"_2.wav";
						else output = output+"_3.wav";
					}

					Signal convolveFFT = new Signal(Convolution.convolveFFT((double)j, (double)p, 0.0, sound.buffer[0], subjects[i]), sound.sampleRate);	
					//Lessen the volume
					for(int k=0; k<convolveFFT.buffer[0].length; k++){
						convolveFFT.buffer[0][k] *= volume;
						convolveFFT.buffer[1][k] *= volume;
					}
					convolveFFT.writeWav(output);

					long end = System.currentTimeMillis();
                			long total = end -start;
					start = end;
                			System.out.println("An individual FFT took "+total+" milliseconds.");
				}
			}
			System.out.println("Convolutions Left = "+((subjects.length-(i+1))*4));
		}
		System.out.println("Finished!");
		System.exit(0);
	}
}
