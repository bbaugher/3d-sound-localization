
import java.io.*;
import java.util.Scanner;

public class Interpolation {
	private static final double EL_INTERVAL = 5.625;
	private static final double EL_START = -45.0;
	private static final int AZ_START = -90;
	private static final int AZ_INTERVAL = 5;
	private static final String HRTF_PATH = "hrtf/";
	private static final String HRTF_FILETYPE = ".txt";
	private static final int DATAPOINTS = 200;
	
	public static void main(String[] args) {
		//int index = getElevationIndex(174, 5.625, -45);
		String[] result = getHrtfs("003",32, -52);
		for (int i = 0; i < result.length; i++){
			System.out.println(result[i]);
		}
	}
	
	/* getHrtfs - returns a string array of 4 hrtfs to interpolate
	 * returns null if input is invalid
	 */
	public static String[] getHrtfs(String subject, double tarEl, double tarAz){
		String[] elevations = getElevation(tarEl);
		String[] azimuths = getAzimuth(tarAz);
		if(elevations == null || azimuths == null) return null;
		String[] results = new String[elevations.length * azimuths.length];
		int index = 0;
		for(int i = 0; i < elevations.length; i++){
			for(int j = 0; j < azimuths.length;j++){
				results[index] = HRTF_PATH+"ubject_"+subject+"_"+azimuths[j]+"_"+elevations[i]+HRTF_FILETYPE;
				index++;
			}
		}
		return results;
	}
	//Returns valid elevations as string array or null if input is invalid
	private static String[] getElevation(double tarEl){
		String[] s;
		int elIndex = getElevationIndex(tarEl,EL_INTERVAL, EL_START);
		if(tarEl > 180 || tarEl < -45 || elIndex < 0) return null;
		double calcEl = (-45.0 + 5.625 * elIndex);
		if (Double.compare(tarEl,calcEl) == 0){
			s = new String[1];
			s[0] = fixString(calcEl+"");
		}
		else {
			s = new String[2];
			s[0] = fixString(calcEl+"");
			if(tarEl > 0)
				s[1] = fixString((calcEl+5.625)+"");
			else if(tarEl < 0)
				s[1] = fixString((calcEl-5.625)+"");				
		}
		return s;		
	}
	//Returns valid azimuths as string array or null if input is invalid
	private static String[] getAzimuth(double tarAz){
		String[] s;
		double absAz = Math.abs(tarAz);
		int azIndex = getAzimuthIndex(tarAz,AZ_INTERVAL, AZ_START);
		if(absAz > 90 || azIndex < 0) return null;
		if(tarAz > 0 && absAz > 80) tarAz = 80;
		if(tarAz < 0 && absAz > 80) tarAz = -80;
		int calcAz = -90 + 5 * azIndex;
		int nextAz;
		if(tarAz < 0){
			nextAz = calcAz;
			calcAz += 5;
			if(absAz > 45){
				calcAz -=10;
				nextAz += 5;	
			}
	        if(absAz > 65) calcAz -=5;			
		}
		else{
			nextAz = calcAz + 5;
			if(absAz > 45){
				nextAz -=5;
				calcAz +=10;
			}
	        if(absAz > 65){
	        	calcAz +=5;			
	        }
		}
		if(Math.abs(calcAz) > 80) calcAz = nextAz;
		if (Double.compare(tarAz,calcAz) == 0 || nextAz > 80){
			s = new String[1];
			s[0] = fixString(calcAz+"");
		}
		else {
			s = new String[2];
			s[0] = fixString(calcAz+"");		
			s[1] = fixString(nextAz+"");
		}
		return s;
	}
	//Formats strings to match file name format
	private static String fixString(String s){
		int index = s.indexOf('.');
		if(index == -1)
			return s;
		if(s.charAt(index+1) == '0') {
			s = s.substring(0,index);
		}
		return s;
	}
	//Returns elevation index
	private static int getElevationIndex(double target, double interval, double start){
		if(target < -45 || target > 180) return -1;
		double elevation = (target - (target%interval))/interval - (start/interval);
		return (int)elevation;
	}	
	//Returns azimuth index
	private static int getAzimuthIndex(double target, double interval, double start){
		double absTarget = Math.abs(target);
		if( absTarget > 90) return -1;
		if( absTarget > 80) target = 80*(target/absTarget);	
		else if( absTarget > 65) target = 65*(target/absTarget);
		else if( absTarget > 55) target = 55*(target/absTarget);
		else if( absTarget > 45) target = 45*(target/absTarget);
		double azimuth = (target - (target%interval))/interval - (start/interval);
		int result = (int) azimuth;
		if(target < 0) result--;
		return result;		
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
	//Read txt hrtf file
	public static double[][] getHrtfBuffer(String name){
		
		File file = new File(name);
		double sound_buffer[][] = new double[2][200];		

		try{
			Scanner scanner = new Scanner(file);
	
			int i = 5;
                	int index = 0;
		
                	while(scanner.hasNextLine()){
                		if(i>0){
                        		i--;
					String line = scanner.nextLine();
                		}
				else{
					String line = scanner.nextLine();
					String ear_values[] = line.split("\\s");
                        		String left_ear[] = ear_values[0].split("e");
                        		String right_ear[] = ear_values[1].split("e");

                        		int left_pow = 0;
                        		int right_pow = 0;

                        		if(left_ear[1].charAt(0)=='-')
                        			left_pow = -Integer.parseInt(left_ear[1].substring(1));
                        		else
                        			left_pow = Integer.parseInt(left_ear[1].substring(1));

                        		if(right_ear[1].charAt(0)=='-')
                        			right_pow = -Integer.parseInt(right_ear[1].substring(1));
                        		else
                        			right_pow = Integer.parseInt(right_ear[1].substring(1));

                        		sound_buffer[0][index] = Double.valueOf(left_ear[0])*Math.pow(10, left_pow);
                        		sound_buffer[1][index] = Double.valueOf(right_ear[0])*Math.pow(10, right_pow);
                        		index++;
				}
                	}
			scanner.close();
		}
		catch(Exception e) {
                	try{
				/*
                        	FileWriter fstream = new FileWriter("errors.txt", true);
                                BufferedWriter out = new BufferedWriter(fstream);
                                out.write("Error "+e.getMessage()+" at "+name+"\n");
                                out.close();*/
				System.out.println("Error "+e.getMessage());
                        }
                        catch(Exception e1){
                        	System.out.println(e1.getMessage());
                       	}
                }

		return sound_buffer;
	}
	//Get interpolated HRTF buffer -- What will get called by Convolution
	public static double[][] getInterpolatedHrtfBuffer(String subject, double x, double y, double z){

		double ele_az[] = convertVectorsToDegrees(x, y, z);
		String hrtfs[] = getHrtfs(subject, ele_az[0], ele_az[1]);

		if(hrtfs!=null){
			double hrtf_buffers[][][] = new double[hrtfs.length][2][200];
			for(int i=0; i<hrtfs.length; i++){
				hrtf_buffers[i] = getHrtfBuffer(hrtfs[i]);
			}
			
			if(hrtfs.length==4)
				return interpolate4(hrtf_buffers[0], hrtf_buffers[1], hrtf_buffers[2], hrtf_buffers[3]);
			else if(hrtfs.length==2){
				double returnVal[][] = interpolate2(hrtf_buffers[0], hrtf_buffers[1], 0.5, 0.5);
				return interpolate2(hrtf_buffers[0], hrtf_buffers[1], 0.5, 0.5);
			}
			else if(hrtfs.length==1){
				return hrtf_buffers[0];
			}
			else return null;
		}
		else{
			return interpolateSpecial(subject, ele_az[0], ele_az[1]);		
		}
	}

	//Interpolation for elevation < -45
	public static double[][] interpolateSpecial(String subject, double elevation, double azimuth){

		double w1, w2, w3, w4;
		//How close you are to elevations, w1 = 0 -> elevation = 45
		w1 = (Math.abs(elevation)-45)/135; 
		w2 = 1 - w1;

		String az[] = getAzimuth(azimuth);
		if(az.length==2){
			//How close you are to azimuths
			w3 = Math.abs(Double.valueOf(az[0])-azimuth)/Math.abs(Double.valueOf(az[0])-Double.valueOf(az[1]));
			w4 = 1 - w3;

			return interpolate4(getHrtfBuffer(HRTF_PATH+"ubject_"+subject+"_"+az[0]+"_180"+HRTF_FILETYPE), getHrtfBuffer(HRTF_PATH+"ubject_"+subject+"_"+az[0]+"_-45"+HRTF_FILETYPE), getHrtfBuffer(HRTF_PATH+"ubject_"+subject+"_"+az[1]+"_180"+HRTF_FILETYPE), getHrtfBuffer(HRTF_PATH+"ubject_"+subject+"_"+az[1]+"_-45"+HRTF_FILETYPE), w1*w4, w2*w4, w1*w3, w2*w3);
		}
		else{
			return interpolate2(getHrtfBuffer(HRTF_PATH+"ubject_"+subject+"_"+az[0]+"_180"+HRTF_FILETYPE), getHrtfBuffer(HRTF_PATH+"ubject_"+subject+"_"+az[0]+"_-45"+HRTF_FILETYPE), w1, w2);
		}
	}
	//Interpolate 2 hrtfs
	public static double[] interpolate2(double[] a1, double[] a2, double weight1,
			double weight2){
		int i;
		double[] result = new double[DATAPOINTS];
		for(i = 0; i < DATAPOINTS; i++){
			result[i] = weight1*a1[i] + weight2*a2[i];
		}
		return result;
	}
	//Interpolate 2 hrtfs
	public static double[][] interpolate2(double[][] a1, double[][] a2, double weight1,
                        double weight2){
                int i;
                double[][] result = new double[2][DATAPOINTS];
                
		for(i = 0; i < DATAPOINTS; i++){
                        result[0][i] = weight1*a1[0][i] + weight2*a2[0][i];
                }
		for(i = 0; i < DATAPOINTS; i++){
                      	result[1][i] = weight1*a1[1][i] + weight2*a2[1][i];
                }
                return result;
        }
	//Interpolate 4 hrtfs
	public static double[][] interpolate4(double[][] a1, double[][] a2, double[][] a3, double[][] a4, double w1,
			double w2, double w3, double w4){
		int i;
		double[][] result = new double[2][DATAPOINTS];
		for(i = 0; i < DATAPOINTS; i++){
			result[0][i] = w1*a1[0][i] + w2*a2[0][i] + w3*a3[0][i] + w4*a4[0][i];
		}
		for(i = 0; i < DATAPOINTS; i++){
			result[1][i] = w1*a1[1][i] + w2*a2[1][i] + w3*a3[1][i] + w4*a4[1][i];
		}
		return result;
	}
	public static double[][] interpolate4(double[][] a1, double[][] a2, double[][] a3, double[][] a4){
		int i;
		double w1, w2, w3, w4;
	    w1 = w2 = w3 = w4 = .25;
		double[][] result = new double[2][DATAPOINTS];
		for(i = 0; i < DATAPOINTS; i++){
			result[0][i] = w1*a1[0][i] + w2*a2[0][i] + w3*a3[0][i] + w4*a4[0][i];
		}
		for(i = 0; i < DATAPOINTS; i++){
			result[1][i] = w1*a1[1][i] + w2*a2[1][i] + w3*a3[1][i] + w4*a4[1][i];
		}
		return result;
	}
/*	public static double[][] interpolate(String subject, double tarEl, double tarAz){
		String[] filenames = getHrtfs(subject, tarEl, tarAz);
		File[] files = new File[filenames.length];
		double[][][] hrtfs = new double[4][2][DATAPOINTS]; 
		//Read HRTFs into "hrtfs" here
		//Can use interpolate4 for 4 hrtfs or interpolate2 for just 2
		
		double[][] finalhrtf = interpolate4(hrtfs[0], hrtfs[1], hrtfs[2], hrtfs[3]);
		return finalhrtf;
	}*/
}

