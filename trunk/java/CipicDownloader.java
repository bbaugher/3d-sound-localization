import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;


public class CipicDownloader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int azimin = -80;
		int azimax = 90;
		int aziinc = 5;
		
		double elemin = -45;
		double elemax = 230.625;
		double eleinc = 5.625;
		
		DecimalFormat df = new DecimalFormat("#.###");
		
		String subjectnum[] = {"003", "010", "018", "020", "021", "027", "028", "033", "040", "044"};
		
		for(int i=0; i<subjectnum.length; i++)
		{
		int acount = azimin;
		while(acount<=azimax)
		{
			double ecount = elemin;
			while(ecount<=elemax)
			{
				//Build string
				String fileloc;
				fileloc = "http://earlab.bu.edu/CIPIC/Subject_";
				fileloc += subjectnum[i];
				fileloc += "_";
				fileloc += acount;
				fileloc += "_";
				//Add elevation
				fileloc += df.format(ecount);
				if(fileloc.lastIndexOf('.')<=20)
				{}
				else
				{
					while(fileloc.charAt(fileloc.length()-1)=='0')
					{
						fileloc = fileloc.substring(0, fileloc.length()-1);
					}
				}
				fileloc += ".txt";
				getFile(fileloc, fileloc.substring(27));
				ecount += eleinc;
			}
			acount += aziinc;
		}
		}

		

	}
	public static StringBuilder getFile(String target,String name){

		InputStream is = null;
		OutputStream os = null;
		BufferedWriter bw;
		DataInputStream dis;
		StringBuilder sb = new StringBuilder();
		String line;	
		File f = new File("hrtf");
		if(!f.isDirectory())f.mkdir();
		//f = new File(name);
		try{
			URL url = new URL(target);
			is = url.openStream();
			dis = new DataInputStream(new BufferedInputStream(is));
			//This writes it as a text file
			//bw = new BufferedWriter(new FileWriter(name));	

			int i = 5;
			int index = 0;
			double sound_buffer[][] = new double[2][200];
			while((line = dis.readLine()) != null){
				//Writes it to the buffer
				//bw.write(line+"\n");
				if(i>0){
					i--;
					continue;
				}
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

			
			Signal sound = new Signal(sound_buffer, 44100);
			sound.writeWav("hrtf/"+name.replace(".txt", ".wav"));
			System.out.println("Written Sound File to hrtf/"+name.replace(".txt", ".wav"));
			//bw.close();
		}catch(Exception e){System.out.println("D: "+e );}
		finally{ try{is.close();}catch(Exception e){}}	
		return sb;
	}

}
