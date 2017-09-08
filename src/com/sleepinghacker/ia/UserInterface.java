package com.sleepinghacker.ia;

import javax.swing.JButton;
import javax.swing.JFrame;
import static org.bytedeco.javacpp.opencv_highgui.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.JLabel;

public class UserInterface {

	/***************************************************************************************
	*    Title: UserInterface
	*    Project: DoorProject2
	*    Author: SleepingCoder	
	*    Date: September 4 2017 at 03:06
	*    Code version: 2.0
	*
	***************************************************************************************/

	public static OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0); // Captures webcam!" 
	public static Frame capturedFrame = null;
	public static DataProcess data = new DataProcess();

	public static CanvasFrame cFrame = new CanvasFrame("Door Project",
			CanvasFrame.getDefaultGamma() / grabber.getGamma());

	public static JFrame frame = new JFrame();
	public static JFrame frame2 = new JFrame();
	public static IplImage catchedEye;
	public static IplImage newEye;

	public static ArrayList<int[]> eyeList = new ArrayList<>();
	
	public static int[] compare;
	public static double hd = 0.23;

	public static void main(String[] Args) {
		UserInterface uinterface = new UserInterface();

	}

	public UserInterface() {

		// Makes a frame to advice if is a correct iris
		frame.setTitle("Aviso!");
		frame.setBounds(100, 100, 450, 100);

		JLabel lblPortaEstAberta = new JLabel("Porta está aberta!");
		lblPortaEstAberta.setForeground(new Color(0, 128, 0));
		lblPortaEstAberta.setFont(new Font("Dialog", Font.BOLD, 20));
		lblPortaEstAberta.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(lblPortaEstAberta, BorderLayout.CENTER);

		// Create the main frame
		cFrame.getCanvas().setBounds(0, 25, 446, 248);
		cFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		cFrame.getContentPane().add(panel, BorderLayout.NORTH);

		// A button to register the rigth iris
		JButton btnBoto = new JButton("Registrar");
		btnBoto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (newEye != null) {
					eyeList.add(data.newIrisArray(null, newEye));
					cvShowImage("Blacked Pupil Iris", data.irisBlackedPupil);
					cvShowImage("Polar to Cart", data.polar2cart);
					cvWaitKey(0);	
					}
			}
		});
		panel.add(btnBoto);

		// A button to clean iris array, just not clean the first irises
		JButton btnBoto_1 = new JButton("Limpar");
		btnBoto_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 2; i <= eyeList.size(); i++) {
					eyeList.remove(i);
				}
			}
		});
		panel.add(btnBoto_1);

		// Starts to capture webcam
		try {
			grabber.start();
			capturedFrame = grabber.grab();

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Runs a thread to capture webcam image
		Thread tr1 = new Thread() {
			public void run() {
				while (true) {
					webcamShow();
				}

			}
		};
		// Runs a thread to show in frame, capture this frame and compare
		Thread tr2 = new Thread() {
			public void run() {
				while (true) {
					
						captureWebcam();
						try {
							grabber.flush(); // Avoid to crash webcam, dont remove it
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						compareEyes();
					

				}

			}
		};
		// Starts threads
		tr1.start();
		tr2.start();
	}

	public static void compareEyes(){
			
		compare = data.newIrisArray(null, catchedEye); // Recives a int array w/ iriscode

		if (compare != null) { // If finds a iris
			
			newEye = catchedEye;
			
			if (eyeList.size() != 0) { // If there is a iris in array list
				
				System.out.println(eyeList.size()); // Prints his size
				
				for (int i = 0; i < eyeList.size(); i++) { // Go to all iris codes registred and get the hammingcode
					
					double n = data.hammingDistance(compare, eyeList.get(i), 10);
					System.out.println(n + "%");
				
					if (n < hd && n > 0) { // If its a safe hamming distance, you shall must pass!
						frame.setVisible(true); // Sets frame visible
					}

				}
			}
		}
	}

	public static void captureWebcam() { // Convert a frame into a IplImage
		OpenCVFrameConverter converter = new OpenCVFrameConverter.ToIplImage();
		catchedEye = converter.convertToIplImage(capturedFrame);// Capture webcam frame

	}

	public static void webcamShow() { // Pass a frame to captureFrame
		try {
			if ((capturedFrame = grabber.grab()) != null) { // If its not null
				capturedFrame = grabber.grab(); // Pass the frame
				cFrame.showImage(capturedFrame);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
