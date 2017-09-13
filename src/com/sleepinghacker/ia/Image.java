package com.sleepinghacker.ia;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

public class Image {
	
	/***************************************************************************************
	*    Title: Image
	*    Project: DoorProject2
	*    Author: SleepingCoder	
	*    Date: September 4 2017 at 03:06
	*    Code version: 2.0
	*
	***************************************************************************************/


	public static CvPoint centerCircle;// Circle center
	
	private static int rad;// Circle radius
	private CvPoint3D32f circle;

	public IplImage reduceSize(String filepath, IplImage img, int w, int h) { // Reduce Image size

		if (img != null) { // If IplImage is different to null resize IplImage
			IplImage newRImage = IplImage.create(w, h, img.depth(), img.nChannels());
			cvResize(img, newRImage);
			return newRImage;

		} else if (filepath != null) {// If IplImage is equals to null, creates a new IplImage w/ filepath and resize
			IplImage image = cvLoadImage(filepath);
			IplImage newRImage = IplImage.create(w, h, image.depth(), image.nChannels());
			cvResize(image, newRImage);
			return newRImage;
		} else {
			System.out.println("Error");
			return img;
		}

	}

	public IplImage blackImage(String filepath, IplImage img) { // Transform the image in black and white colors

		if (img != null) { // If IplImage is different to null b/a IplImage
			IplImage newRImage = IplImage.create(img.width(), img.height(), img.depth(), 1);
			cvCvtColor(img, newRImage, COLOR_BGR2GRAY);
			cvEqualizeHist(newRImage,newRImage);
			return newRImage;

		} else if (filepath != null) {// If IplImage is equals to null, creates a new IplImage w/ filepath and b/a
			IplImage image = cvLoadImage(filepath);
			IplImage newRImage = IplImage.create(image.width(), image.height(), image.depth(), image.nChannels());
			cvCvtColor(img, newRImage, COLOR_BGR2GRAY);
			cvEqualizeHist(newRImage,newRImage);
			
			return newRImage;
		} else {
			System.out.println("Error");
			return img;
		}
	}

	public IplImage smoothImage(String filepath, IplImage img, int smooth) {

		if (img != null) { // If IplImage is different to null smooth IplImage
			IplImage newRImage = IplImage.create(img.width(), img.height(), img.depth(), img.nChannels());
			cvSmooth(img, newRImage, CV_MEDIAN, smooth, smooth, 1, 1);
			return newRImage;

		} else if (filepath != null) {// If IplImage is equals to null, creates a new IplImage w/ filepath and smooth
			IplImage image = cvLoadImage(filepath);
			IplImage newRImage = IplImage.create(image.width(), image.height(), image.depth(), image.nChannels());
			cvSmooth(img, newRImage, CV_MEDIAN, smooth, smooth, 1, 1);
			return newRImage;
		} else {
			System.out.println("Error");
			return img;
		}

	}

	public IplImage getIris(IplImage img) { // Get the Iris content

		// Creates a image resizes,b/a and smooths, after that calls getCircles
		IplImage des = cvCreateImage(cvGetSize(img), 8, 1);
		cvCvtColor(img, des, COLOR_BGR2GRAY);
		cvSmooth(des, des, CV_MEDIAN, 5, 5, 1, 1);
		CvSeq circles;
		circles = getCircles(des, 130, 150);
		if (circles != null) {
			circle = new CvPoint3D32f(cvGetSeqElem(circles, 1));
			CvPoint center = cvPointFrom32f(new CvPoint2D32f(circle.x(), circle.y()));
			int radius = Math.round(circle.z());

			// Pass this vars to the public vars
			centerCircle = center;
			rad = radius;
			if (center != null && circle != null) {
				// Separate the circle and crop the image
				CvRect rect = new CvRect(center.x() - radius, center.y() - radius, radius * 2, radius * 2);
				cvSetImageROI(img, rect);
				IplImage croppedImage = cvCreateImage(cvGetSize(img), 8, 3);
				cvCopy(img, croppedImage);
				cvResetImageROI(img);
				return croppedImage;
			} else {
				return null;
			}
			// Return iris cropped

		} else {
			return null;
		}

	}

	public static CvSeq getCircles(IplImage img, int minRad, int maxRad) { // Get Circles from image
		for (int i = 80; i < 151; i++) {// While threshold is not 150 try this
			CvMemStorage mem = CvMemStorage.create();
			CvSeq circles = cvHoughCircles(img, // Input image
					mem, // Memory Storage
					CV_HOUGH_GRADIENT, // Detection method
					2, // Inverse ratio
					100, // Minimum distance between the centers of the detected circles
					50, // Higher threshold for Canny edge detector
					i, // Threshold at the center detection stage
					minRad, // min radius
					maxRad // max radius
			);

			if (circles.total() == 1) { // If finds one circle, return
				return circles;
			}
			i++;
		}
		return null;
	}

	public static IplImage getPolar2CartImg(IplImage img) { // Transform image in Cartesian
		if (img != null) {
			IplImage resImg = cvCreateImage(cvSize(rad * 3, 360), 8, 1);// Creates a new image
			cvLogPolar(img, resImg, cvPoint2D32f(img.width() / 2.0, img.height() / 2.0), 80.0,
					CV_INTER_LINEAR + CV_WARP_FILL_OUTLIERS); // Transform in Cartesian plane
			return resImg; // Return image w/ Cartesian Plane
		} else {
			return img;
		}
	}
	
	public IplImage gaborfilter(IplImage img) {
		if (img != null) {
			int width = 10;
			int height = 10;
			double sigma = 1.4;
			double thetaDeg = 90;
			double lambda = 4;
			double gamma = 1.0;
			double psiDeg = 0;

			Size ksize = new Size(width,height);

			// Reading an image
			Mat image = new Mat(img);
			double theta = thetaDeg * Math.PI / 180; // radian
			double psi = psiDeg * Math.PI / 180; // radian

			// Making the source black and white.
			Mat mat1 = new Mat(image.rows(), image.cols(), CV_8UC1);
			cvtColor(image, mat1, COLOR_BGR2GRAY);
			

			// Applying the Gabor filter.
			Mat kernel = getGaborKernel(ksize, sigma, theta, lambda, gamma);
			Mat dest = new Mat(mat1.rows(), mat1.cols(), image.type());
			filter2D(mat1, dest, mat1.type(), kernel);

			IplImage finalImg = new IplImage(dest);
			return finalImg;
		} else {
			return img;
		}
	        
	}
	public IplImage blackPupil(IplImage img) { // Get the Iris content
		if (img != null) {
			// Creates a image resizes,b/a and smooth, after that calls getCircles

			IplImage des = cvCreateImage(cvGetSize(img), 8, 1);
			cvCvtColor(img, des, COLOR_BGR2GRAY);
			cvSmooth(des, des, CV_MEDIAN, 7, 7, 1, 1);

			CvSeq circles = null;
			circles = getCircles(des, 0, 50);

			if (circles != null) {
				circle = new CvPoint3D32f(cvGetSeqElem(circles, 1));
				CvPoint center = cvPointFrom32f(new CvPoint2D32f(circle.x(), circle.y()));
				int radius = Math.round(circle.z());

				cvCircle(img, center, radius, CvScalar.BLACK, CV_FILLED, CV_AA, 0);
				// Return iris cropped
				return img;
			} else {
				return img;
			}
		} else {
			return img;
		}

	}

}
