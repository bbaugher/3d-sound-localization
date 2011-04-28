
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
		long start;	

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

		String[] subjects = {"003", "010", "018", "020", "021", "027", "028", "033", "040", "044"};

		//j => leftright
		//p => frontback	

		for(int i=0; i<subjects.length; i++){
			for(double x=-1; x<=1; x+=0.1){
				for(double y=-1; y<=1; y+=0.1){
					for(double z=-1; z<=1; z+=0.1){
					/*
					String output = "customization/"+subjects[i];
					if(j==-1){
						if(p==-1) output = output+"_3.wav";
						else output = output+"_0.wav";
					}
					else{
						if(p==-1) output = output+"_2.wav";
						else output = output+"_1.wav";
					}*/
					
					//System.out.println(x+", "+y+", "+z);

					Convolution.convolveFFT(x, y, z, sound.buffer[0], subjects[i], sound.sampleRate);
					//convolveFFT.writeWav(output);

					/*
					long end = System.currentTimeMillis();
                			long total = end -start;
					start = end;
                			System.out.println("FFT took "+total+" milliseconds.");*/
					}
				}
			}
			long end = System.currentTimeMillis();
                        long total = (end -start)/60000;
                        start = end;
			System.out.println("Subject took "+total+" minutes.");
			System.out.println("#Left = "+((subjects.length-(i+1))*1000));
		}
		System.out.println("Finished!");
		System.exit(0);
	}

 	//Convert direction unit vectors into azimuth [-90, 90] and elevation [-180, 180]
        public static double[] convertVectorsToDegrees(double x, double y, double z){
                //ele_az[0] = elevation
                //ele_az[1] = azimuth
                double ele_az[] = new double[2];

                //Assumes
                        //HRTFS
                                //Elevation > 0 -> above, Elevation < 0 -> below
                                //Azimuth > 0 -> right, Azimuth < 0 -> left
                        //Coordinates
                                //Coordinates point to source of sound from listener
                                //x > 0 -> left, x < 0 -> right
                                //y > 0 -> front, y < 0 -> back
                                //z > 0 -> above, z < 0 -> below                

                //Calcualte Elevation
                if(z>0){//Above
                        if(y>0){//Above in front
                                ele_az[0] = Math.atan(Math.abs(z/y))/Math.PI*180;
                        }
                        else{//Above in back
                                if(y!=0)
                                        ele_az[0] = 180 - Math.atan(Math.abs(z/y))/Math.PI*180;
                                else
                                        ele_az[0] = 180;
                        }
                }
                else{//Below
                        if(y>0){ //Below in front
                                ele_az[0] = -Math.atan(Math.abs(z/y))/Math.PI*180;
                        }
                        else{//Below in back
                                if(y!=0)
                                        ele_az[0] = Math.atan(Math.abs(z/y))/Math.PI*180-180;
                                else if(z<0)
                                        ele_az[0] = -180;
                                else
                                        ele_az[0] = 0;
                        }
                }

		//Calculate Azimuth
                if(y>0){//Front
                        if(x>0){//Front to the left
                                ele_az[1] = -Math.atan(Math.abs(x/y))/Math.PI*180;
                        }
                        else{//Front to the right
                                ele_az[1] = Math.atan(Math.abs(x/y))/Math.PI*180;
                        }
                }
                else{//Back
                        if(y!=0){
                                if(x>0){//Back to the left
                                        ele_az[1] = Math.atan(Math.abs(x/y))/Math.PI*180;
                                }
                                else{//Back to the right
                                        ele_az[1] = -Math.atan(Math.abs(x/y))/Math.PI*180;
                                }
                        }
                        else{// y=0
                                if(x>0)
                                        ele_az[1] = -90;
                                else if(x<0)
                                        ele_az[1] = 90;
                                else
                                        ele_az[1] = 0;
                        }
                }

                return ele_az;
        }
}
