#include <stdio.h>
#include <math.h>
#include <malloc.h>
#include <sndfile.h>

double[] seperate_channels(double buffer[], int channel);
double[] convolution(double hrtf[], double stereo[], double *out[]);

int main(int argc, char *argv[])
{
	printf("Wav Convolution Test\n");
	if (argc != 4) {
		fprintf(stderr, "Expecting stereo.wav hrtf.wav out.wav\n");
		return 1;
	}

	// S T E R E O    F I L E

	// Open Stereo File
        SF_INFO stereo_info;
        SNDFILE *stereo_file = sf_open(argv[1], SFM_READ, &stereo_info);
        if (sndFile == NULL) {
                fprintf(stderr, "Error reading source file '%s': %s\n", argv[1], sf_strerror(stereo_file));
                return 1;
        }

        // Allocate memory for stereo file
        double *stereo_buffer = malloc(stereo_info.frames * sizeof(double));
        if (stereo_buffer == NULL) {
                fprintf(stderr, "Could not allocate memory for file\n");
                sf_close(stereo_file);
                return 1;
        }

        // Load data
        long stereo_numFrames = sf_readf_double(stereo_file, stereo_buffer, stereo_info.frames);

        // Check correct number of samples loaded
        if (stereo_numFrames != stereo_info.frames) {
                fprintf(stderr, "Did not read enough frames for stereo file.\n");
                sf_close(stereo_file);
                free(stereo_buffer);
                return 1;
        }

	// H R T F   P A R T

	// Open HRTF File
	SF_INFO hrtf_info;
        SNDFILE *hrtf_file = sf_open(argv[2], SFM_READ, &hrtf_info);
        if (sndFile == NULL) {
                fprintf(stderr, "Error reading source file '%s': %s\n", argv[1], sf_strerror(hrtf_file));
                return 1;
        }

        // Allocate memory for HRTF file
        double *hrtf_buffer = malloc(hrtf_info.frames * sizeof(double));
        if (hrtf_buffer == NULL) {
                fprintf(stderr, "Could not allocate memory for file\n");
                sf_close(hrtf_file);
                return 1;
        }

        // Load hrtf data
        long hrtf_numFrames = sf_readf_double(hrtf_file, hrtf_buffer, hrtf_info.frames);

        // Check correct number of samples loaded
        if (hrtf_numFrames != hrtf_info.frames) {
                fprintf(stderr, "Did not read enough frames for HRTF\n");
                sf_close(stereo_file);
                free(stereo_buffer);
                sf_close(hrtf_file);
		free(hrtf_buffer);
		return 1;
        }

	// O U T    F I L E

	// Set file settings for out file
        SF_INFO out_info;
        out_info.format = stereo_info.format;
        out_info.channels = 2;
        out_info.samplerate = stereo_info.sampleRate;

	// Open out file for writing
	SNDFILE *out_file = sf_open(argv[3], SFM_WRITE, &out_info);
	if (sndFile == NULL) {
		fprintf(stderr, "Error opening sound file '%s': %s\n", argv[3], sf_strerror(out_file));
		sf_close(stereo_file);
		sf_close(hrtf_file);
		free(hrtf_buffer);
		free(stereo_buffer);
		return -1;
	}

	// Setup out buffer
	double *out_buffer[] = malloc(2*(sizeof(stereo_buffer)+(int)(sizeof(hrtf)/2)-1));

	long out_numFrames = sizeof(out_buffer);

	//Convolution -- (assumes hrtf is dual channeled and stereo is mono)
	interleave_channels(convolve(seperate_channels(hrtf_buffer, 1), stereo_buffer), convolve(seperate_channels(hrtf_buffer, 2), stereo_buffer), out_buffer);

	// Write frames
	long writtenFrames = sf_writef_double(out_file, out_buffer, out_numFrames);

	// Check correct number of frames saved
	if (writtenFrames != out_numFrames) {
		fprintf(stderr, "Did not write enough frames for out file\n");
		sf_close(out_file);
		sf_close(hrtf_file);
		sf_close(stereo_file);
		free(stereo_buffer);
		free(hrtf_buffer);
		free(out_buffer);
		return -1;
	}

	// Tidy up
	sf_write_sync(out_file);
	sf_close(stereo_file);
	sf_close(hrtf_file);
	sf_close(out_file);
	free(stereo_buffer);
	free(hrtf_buffer);

	return 0;
}

//Assumes buffer is dual channeled
double[] seperate_channels(double buffer[], int channel){
	double[] *out = malloc(sizeof(buffer)/2* sizeof(double));
	int index = 0;

	for(int i=channel-1; i<sizeof(buffer); i+=2)
		out[index] = buffer[i];

	return *out;
}

void interleave_channels(double ch_1[], double ch_2[], double *out[]){
	for(int i=0; i<sizeof(ch_1)+sizeof(ch_2); i+=2){
		out[i] = ch_1[(int)(i/2)];
		out[i+2] = ch_2[(int)(i/2)];
	}
	return out;
}

double[] convolve(double hrtf[], double stereo[]){
	int result_len = sizeof(hrtf)+sizeof(stereo) - 1;
	double[] *out = malloc(result_len * sizeof(double));	
	
	for(int n=0; n < result_len; n++){
		double sum = 0;
		for(int i=0; i<sizeof(hrtf); i++){
			if (i<sizeof(stereo) + (sizeof(hrtf)-1) - n && i >= sizeof(hrtf)-n-1) {
                        	sum += stereo[i+n-sizeof(hrtf)+1]*hrtf[sizeof(hrtf)-i-1];
                        }
		}
		out[n] = sum;	
	}
	return out;	
}

/* Original Java Convolution Function
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
*/

