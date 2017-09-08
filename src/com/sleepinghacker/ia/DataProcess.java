package com.sleepinghacker.ia;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import org.bytedeco.javacpp.opencv_core.IplImage;

public class DataProcess {
	
	/***************************************************************************************
	*    Title: DataProcess
	*    Project: DoorProject2
	*    Author: SleepingCoder	
	*    Date: September 4 2017 at 03:06
	*    Code version: 2.0
	*
	***************************************************************************************/
	
	private Mat mat;
	public IplImage polar2cart;
	public IplImage irisBlackedPupil;

	public int[] newIrisArray(String filepath, IplImage recivedImg) { // Transform a image in black and white then
																		// convert to array

		IplImage img = null; // Recived Image
		IplImage iris = null;// Captured Iris
		IplImage irisCart = null; // Cartesian image w/ iris
		int[] a = { 0 };
		if (filepath != null) {
			img = cvLoadImage(filepath); // If its diferent from null, load image from filepath
		} else if (recivedImg != null) {
			img = recivedImg; // If its diferent from null, load image from the argument
		} else if (filepath == null && recivedImg == null) {
			System.out.println("Error!"); // If its total null, print Error!
		}

		Image image = new Image(); // Loads image class

		// Get Iris and Transform in Cartesian Plane
		iris = image.getIris(img); // Get a cropped image from eye
		iris = image.blackPupil(iris); // Paint pupil with black
		irisBlackedPupil = iris; // Pass to a global variable

		if (iris != null) {
			irisCart = Image.getPolar2CartImg(iris);
			if (irisCart != null) {
				irisCart = image.blackImage(null, irisCart);
				irisCart = image.reduceSize(null, irisCart, 200, 200);
				polar2cart = irisCart; // Pass to a global variable

				// Return transformed image
				return convertIpltoArray(null, irisCart);
			}
		}

		return a;

	}

	public int[] convertIpltoArray(String filepath, IplImage recivedImage) { // Convert a grayscale image to a int array
		
		IplImage img = null; // Creates a null IplImage
		
		//Recives image
		if (filepath != null) { 
			img = cvLoadImage(filepath);
		} else if (recivedImage != null) {
			img = recivedImage;
		} else if (filepath == null && recivedImage == null) {
			System.out.println("Error");
		}
		
		//Create a mat with image
		mat = new Mat(img);
		
		//Then make a array who will recive all pixels
		int i = 0;
		int[] gray = new int[mat.rows() * mat.cols() + 1]; // A int array w/ area of the picture
		
		for (int x = 0; x < mat.cols(); x++) { // Go to all x positions

			for (int y = 0; y < mat.rows(); y++) { // Go to all y positions
				i++;
				CvScalar rgb = cvGet2D(img, y, x);// Get scalar from picture
				gray[i] = (int) rgb.get(0); // Recives the greyscale picture

			}
		}
		return gray; // return array

	}

	public double hammingDistance(int[] irisCode, int[] irisToCompare, int taxRate) { // A Hybrid between Hamming
																						// distance and my personal code

		double result = 0;
		for (int i = 0; i < irisCode.length; i++) {
			int MinNumber = irisCode[i] - taxRate; // Min number who can be founded
			int MaxNumber = irisCode[i] + taxRate;// Max number who can be founded

			if (irisToCompare[i] < MinNumber || irisToCompare[i] > MaxNumber) {
				result++;// Add one in result
			}

		}
		result = (result / irisCode.length); // Transform the result in percent
		return result;

	}


}
