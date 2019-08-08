package com.pages.page4;

import com.constants.Constants;
import com.gui.Gui;
import com.pages.Pages;

import java.awt.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class Page4 extends JPanel implements Pages {
  private static final long serialVersionUID = 1L;
  private Gui gui;
  private JPanel tab4;
  private JPanel mainPanel = new JPanel();
  private JPanel buttonsPanel = new JPanel();
  private JButton startButton = new JButton("Start");
  private JButton pauseButton = new JButton("Pause");
  private JPanel webcamPanel = new JPanel();

  private DaemonThread myThread = null;
  private int count = 0;
  private VideoCapture webSource = null;
  private Mat frame = new Mat();
  private MatOfByte mem = new MatOfByte();
  private CascadeClassifier faceDetector = new CascadeClassifier(Constants.CASCADE_CLASSIFIER);
  private MatOfRect faceDetections = new MatOfRect();


  public Page4(Gui gui) {
    this.gui = gui;
    tab4 = gui.getTab4();
    initComponents();
    addListeners();
  }

  private void initComponents() {
    ///////////////////////////////////////////////////////////////
    mainPanel.setBackground(Color.blue);
    buttonsPanel.setBackground(Color.green);
    ///////////////////////////////////////////////////////////////

    webcamPanel.setPreferredSize(new Dimension(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT));
    mainPanel.add(webcamPanel);

    buttonsPanel.add(startButton);
    buttonsPanel.add(pauseButton);

    mainPanel.setPreferredSize(new Dimension(800, 500));
    buttonsPanel.setPreferredSize(new Dimension(800, 100));
    tab4.add(mainPanel);
    tab4.add(buttonsPanel);
    pauseButton.setEnabled(false);
  }

  private void addListeners() {
    startButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        webSource = new VideoCapture(0); // video capture from default cam
        myThread = new Page4.DaemonThread(); //create object of threat class
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();                 //start thrad
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        gui.getTabs().setEnabled(false);
      }
    });

    pauseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        myThread.runnable = false;            // stop thread
        webSource.release();  // stop caturing fron cam
        pauseButton.setEnabled(false);
        startButton.setEnabled(true);
        gui.getTabs().setEnabled(true);
      }
    });
  }

  class DaemonThread implements Runnable {
    protected volatile boolean runnable = false;

    @Override
    public void run() {
      synchronized (this) {
        while (runnable) {
          if (webSource.grab()) {
            try {
              webSource.retrieve(frame);
              Graphics g = webcamPanel.getGraphics();
              faceDetector.detectMultiScale(frame, faceDetections);
              for (Rect rect : faceDetections.toArray()) {
                Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0));
              }
              Imgcodecs.imencode(".bmp", frame, mem);
              Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
              BufferedImage buff = (BufferedImage) im;
              if (g.drawImage(buff, 0, 0, Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT, 0, 0, buff.getWidth(), buff.getHeight(), null)) {
                if (runnable == false) {
                  System.out.println("Paused ..... ");
                  this.wait();
                }
              }
            } catch (Exception ex) {
              System.out.println("Error!!");
              ex.printStackTrace();
            }
          }
        }
      }
    }
  }
}