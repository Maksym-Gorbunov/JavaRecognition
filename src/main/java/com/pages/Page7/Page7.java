package com.pages.Page7;

import com.constants.Constants;
import com.gui.Gui;
import com.gui.ImagePanel;
import com.pages.Pages;
import org.apache.commons.io.FileUtils;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Page7 extends JPanel implements Pages {

  private JFrame gui;
  private JPanel tab7;
  private JTabbedPane imgTabPane;
  private ImagePanel originalPanel;
  private ImagePanel filteredPanel;
  private ImagePanel contoursPanel;
  private ImagePanel platePanel;
  private ImagePanel filteredPlatePanel;
  private JButton openBtn = new JButton("Open");
  private JButton recognizeBtn = new JButton("Recognize");
  private JList jList;
  private JTextField licenseNumberTextField = new JTextField("");
  private DefaultListModel<ImgObject> data = new DefaultListModel<>();
  private Recognizer recognizer;
  private String result;
  private ImgObject selectedObject;
  private JSlider thrashSlider;
  private JSlider blurSlider;
  private JSlider shearAngleSlider;
  private int width = 400;
  private int height = 300;

  // Constructor
  public Page7(Gui gui) {
    this.gui = gui;
    tab7 = gui.getTab7();
    initComponents();
    addListeners();
    clearFolder(Constants.imgPath + "lpr\\");
    recognizer = new Recognizer();
  }

  // Add listeners to UI components
  private void addListeners() {

    //open btn
    openBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(Constants.imgPath + "cars\\");
        fc.setMultiSelectionEnabled(true);
        if (fc.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {
          File[] files = fc.getSelectedFiles();
          for (File file : files) {
            ImgObject imgFile = new ImgObject(file);
            data.addElement(imgFile);
          }
        }
        if (!data.isEmpty()) {
          jList.setSelectedIndex(0);
          recognizeBtn.setEnabled(true);
        }
      }
    });

    //recognize btn
    recognizeBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        recognizeBtn.setEnabled(false);
        if (selectedObject != null) {
          ImgObject result = recognizer.recognize(selectedObject.getFile(), thrashSlider.getValue(), shearAngleSlider.getValue()*0.1);
          if (result.getFiltered() != null) {
            Mat filtered = new Mat();
            result.getFiltered().copyTo(filtered);
            selectedObject.setFiltered(filtered);
          }
          if (result.getContours() != null) {
            Mat contours = new Mat();
            result.getContours().copyTo(contours);
            selectedObject.setContours(contours);
          }
          if (result.getPlate() != null) {
            Mat plate = new Mat();
            result.getPlate().copyTo(plate);
            selectedObject.setPlate(plate);
          }
          if (result.getFilteredPlate() != null) {
            Mat filteredPlate = new Mat();
            result.getFilteredPlate().copyTo(filteredPlate);
            selectedObject.setFilteredPlate(filteredPlate);
          }
          selectedObject.setLicenseNumber(result.getLicenseNumber());
          updateImages();
        }
      }
    });

    //select list item
    jList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent arg0) {
        if (!arg0.getValueIsAdjusting()) {
          recognizeBtn.setEnabled(true);
          selectedObject = data.get(jList.getSelectedIndex());
          updateImages();
          imgTabPane.setSelectedIndex(0);
        }
      }
    });

    thrashSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        recognizeBtn.setEnabled(true);
      }
    });

    blurSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        recognizeBtn.setEnabled(true);
      }
    });
    shearAngleSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        recognizeBtn.setEnabled(true);
      }
    });

  }

  // Update UI
  private void updateImages() {
    if (selectedObject.getOriginal() != null) {
      originalPanel.loadMatImage(selectedObject.getOriginal());
    } else {
      originalPanel.clear();
    }
    if (selectedObject.getFiltered() != null) {
      filteredPanel.loadMatImage(selectedObject.getFiltered());
    } else {
      filteredPanel.clear();
    }
    if (selectedObject.getContours() != null) {
      contoursPanel.loadMatImage(selectedObject.getContours());
    } else {
      contoursPanel.clear();
    }
    if (selectedObject.getPlate() != null) {
      platePanel.loadMatImage(selectedObject.getPlate());
    } else {
      platePanel.clear();
    }
    if (selectedObject.getFilteredPlate() != null) {
      filteredPlatePanel.loadMatImage(selectedObject.getFilteredPlate());
    } else {
      filteredPlatePanel.clear();
    }
    licenseNumberTextField.setText(selectedObject.getLicenseNumber());
  }

  // Initialize UI components
  private void initComponents() {
    JPanel top = new JPanel();
    JPanel bottom = new JPanel();
    JPanel topLeft = new JPanel();
    JPanel topRight = new JPanel();
    JPanel bottomLeft = new JPanel();
    JPanel bottomRight = new JPanel();
    top.add(topLeft);
    top.add(topRight);
    bottom.add(bottomLeft);
    bottom.add(bottomRight);
    tab7.add(top);
    tab7.add(bottom);
    topLeft.setPreferredSize(new Dimension((int) (Constants.FRAME_WIDTH * 0.7), height + 40));
    topRight.setPreferredSize(new Dimension((int) (Constants.FRAME_WIDTH * 0.3), height + 40));
    bottomLeft.setPreferredSize(new Dimension((int) (Constants.FRAME_WIDTH * 0.7), 200));
    bottomRight.setPreferredSize(new Dimension((int) (Constants.FRAME_WIDTH * 0.3), 200));
    imgTabPane = new JTabbedPane();
    originalPanel = new ImagePanel(width, height);
    filteredPanel = new ImagePanel(width, height);
    contoursPanel = new ImagePanel(width, height);
    platePanel = new ImagePanel(width, height);
    filteredPlatePanel = new ImagePanel(width, height);
    imgTabPane.add("Original", originalPanel);
    imgTabPane.add("Filtered", filteredPanel);
    imgTabPane.add("Contours", contoursPanel);
    imgTabPane.add("LPlate", platePanel);
    imgTabPane.add("FilteredPlate", filteredPlatePanel);
    topLeft.add(imgTabPane);
    topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));
    topRight.setBorder(new EmptyBorder(10, 10, 10, 10));
    bottomLeft.setBorder(new EmptyBorder(0, 10, 0, 0));
    jList = new JList((ListModel) data);
    jList.setFixedCellWidth(20);
    jList.setVisibleRowCount(15);
    jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(jList);
    jList.setFixedCellWidth(120);
    topRight.add(scrollPane);
    JLabel resultLabel = new JLabel("RESULT: ");
    topRight.add(resultLabel);
    topRight.add(licenseNumberTextField);
    JPanel btns = new JPanel();
    btns.add(openBtn);
    btns.add(recognizeBtn);
    topRight.add(btns);
    bottomLeft.setLayout(new GridLayout(3, 2));

    thrashSlider = new JSlider(JSlider.HORIZONTAL, 0, 250, 80);
    thrashSlider.setMinorTickSpacing(10);
    thrashSlider.setMajorTickSpacing(50);
    thrashSlider.setPaintTicks(true);
    thrashSlider.setPaintLabels(true);
    bottomLeft.add(thrashSlider);

    bottomRight.add(new JLabel("Transformation - Shear angle"));
    shearAngleSlider = new JSlider(JSlider.HORIZONTAL, -10, 10, -6);
    shearAngleSlider.setMinorTickSpacing(1);
    shearAngleSlider.setMajorTickSpacing(5);
    shearAngleSlider.setPaintTicks(true);
    shearAngleSlider.setPaintLabels(true);
    bottomRight.add(shearAngleSlider);

    bottomLeft.add(new JLabel("Thresh"));
    blurSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
    blurSlider.setMinorTickSpacing(1);
    blurSlider.setMajorTickSpacing(5);
    blurSlider.setPaintTicks(true);
    blurSlider.setPaintLabels(true);
    bottomLeft.add(blurSlider);

    bottomLeft.add(new JLabel("Blur"));
    recognizeBtn.setEnabled(false);
  }

  // Clear folder from old files
  public void clearFolder(String path) {
    try {
      FileUtils.deleteDirectory(new File(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
    new File(path).mkdirs();
  }
}
