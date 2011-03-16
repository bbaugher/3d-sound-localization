
import java.lang.*;
import java.io.*;
import javax.sound.sampled.*;
import java.math.*;

class Signal {

	public String fileName;
	public int sampleRate, numChannels, numFrames;
	public double[][] buffer;	

	public Signal(String file){
		fileName = file;
		try{
			// Open the wav file 
                        WavFile w = WavFile.openWavFile(new File(file));

                        sampleRate = (int)w.getSampleRate();
                        numChannels = (int)w.getNumChannels();
                        numFrames = (int)w.getNumFrames();

                        buffer = new double[numChannels][numFrames];

                        w.readFrames(buffer, numFrames);
		}
		catch(Exception e){
                                System.err.println(e);
                }
	}

	public Signal(double[][] new_buffer, int new_sampleRate){
		buffer = new_buffer;
		sampleRate = new_sampleRate;
		numChannels = new_buffer.length;
		numFrames = new_buffer[0].length;
	}

	public void writeWav(String file){
		try{
			WavFile new_out = WavFile.newWavFile(new File(file), numChannels, buffer[0].length, 16, sampleRate);
                	new_out.writeFrames(buffer, buffer[0].length);
		}
		catch(Exception e){
                                System.err.println(e);
                }
	}

	public void loadWav(String file){
		fileName = file;
                try{
                        // Open the wav file 
                        WavFile w = WavFile.openWavFile(new File(file));

                        sampleRate = (int)w.getSampleRate();
                        numChannels = (int)w.getNumChannels();
                        numFrames = (int)w.getNumFrames();

                        buffer = new double[numChannels][numFrames];

                        w.readFrames(buffer, numFrames);
                }
                catch(Exception e){
                                System.err.println(e);
                }
	}

	

}
