package ui;

import cs.cube555.Tools;
import cs.cube555.Search;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.event.*;

import java.io.*;

public class MainProgram extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
	private JButton[][] facelet = new JButton[6][25];
	private final JButton[] colorSel = new JButton[6];
	private final int FSIZE = 27;
	private final int[] XOFF = { 5, 10, 5, 5, 0, 15 };// Offsets for facelet display
	private final int[] YOFF = { 0, 5, 5, 10, 5, 5 };
	private final Color[] COLORS = { Color.white, Color.red, Color.green, Color.yellow, Color.orange, Color.blue };
	private JTextPane ResultTextPane;
	private JCheckBox checkBoxShowStr;
	private JButton buttonRandom;
	private JCheckBox checkBoxUseSep;
	private JCheckBox checkBoxInv;
	private JCheckBox checkBoxShowLen;

	private JButton Solve;
	private JLabel jLabel2;
	private JLabel jLabel1;
	private JSpinner spinnerMaxMoves;
	private JSpinner spinnerTimeout;
	private Color curCol = COLORS[0];
	private int maxDepth = 21, maxTime = 5;
	boolean useSeparator = true;
	boolean showString = false;
	boolean inverse = true;
	boolean showLength = true;
	Search search = new Search();
	cs.min2phase.Search search333 = new cs.min2phase.Search();

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static void main(String[] args) {
		Search.init();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainProgram inst = new MainProgram();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}

	public MainProgram() {
		super();
		initGUI();
	}

	private void initGUI() {

		getContentPane().setLayout(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle("Three-Phase-Reduction Package GUI-Example");

		Solve = new JButton("Solve Cube");
		getContentPane().add(Solve);
		Solve.setBounds(422, 64, 114, 48);
		Solve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				solveCube(evt);
			}
		});

		{
			checkBoxUseSep = new JCheckBox("Use Separator", true);
			getContentPane().add(checkBoxUseSep);
			checkBoxUseSep.setBounds(12, 320, 121, 20);
			checkBoxUseSep.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					useSeparator = checkBoxUseSep.isSelected();
				}
			});
		}

		{
			checkBoxShowStr = new JCheckBox("Show String", false);
			getContentPane().add(checkBoxShowStr);
			checkBoxShowStr.setBounds(12, 343, 121, 20);
			checkBoxShowStr.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					showString = checkBoxShowStr.isSelected();
				}
			});
		}
		{
			checkBoxShowLen = new JCheckBox("Show Length", true);
			getContentPane().add(checkBoxShowLen);
			checkBoxShowLen.setBounds(12, 366, 121, 20);
			checkBoxShowLen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					showLength = checkBoxShowLen.isSelected();
				}
			});
		}

		// ++++++++++++++++++++++++++++++++++ Set up Random Button ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		{
			buttonRandom = new JButton("Random Cube");
			getContentPane().add(buttonRandom);
			buttonRandom.setBounds(422, 17, 114, 22);
			buttonRandom.setText("Scramble");
			buttonRandom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					// +++++++++++++++++++++++++++++ Call Random function from package org.kociemba.twophase ++++++++++++++++++++
					String r = Tools.randomCube();
					ResultTextPane.setText(r);
					// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
					for (int i = 0; i < 6; i++)
						for (int j = 0; j < 25; j++) {
							switch (r.charAt(25 * i + j)) {
							case 'U':
								facelet[i][j].setBackground(COLORS[0]);
								break;
							case 'R':
								facelet[i][j].setBackground(COLORS[1]);
								break;
							case 'F':
								facelet[i][j].setBackground(COLORS[2]);
								break;
							case 'D':
								facelet[i][j].setBackground(COLORS[3]);
								break;
							case 'L':
								facelet[i][j].setBackground(COLORS[4]);
								break;
							case 'B':
								facelet[i][j].setBackground(COLORS[5]);
								break;
							}
						}
				}
			});
		}
		{
			ResultTextPane = new JTextPane();
			getContentPane().add(ResultTextPane);
			ResultTextPane.setText("ResultTextPane");
			ResultTextPane.setBounds(12, 417, 520, 63);
		}

		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 25; j++) {
				facelet[i][j] = new JButton();
				getContentPane().add(facelet[i][j]);
				facelet[i][j].setBackground(Color.gray);
				facelet[i][j].setRolloverEnabled(false);
				facelet[i][j].setOpaque(true);
				facelet[i][j].setBounds(FSIZE * XOFF[i] + FSIZE * (j % 5), FSIZE * YOFF[i] + FSIZE * (j / 5), FSIZE, FSIZE);
				facelet[i][j].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						((JButton) evt.getSource()).setBackground(curCol);
					}
				});
			}
		String[] txt = { "U", "R", "F", "D", "L", "B" };

		for (int i = 0; i < 6; i++) {
			colorSel[i] = new JButton();
			getContentPane().add(colorSel[i]);
			colorSel[i].setBackground(COLORS[i]);
			colorSel[i].setOpaque(true);
			colorSel[i].setBounds(FSIZE * (XOFF[1] + 1) + FSIZE * i, FSIZE * (YOFF[3] + 1), FSIZE,
			                      FSIZE);
			colorSel[i].setName("" + i);
			colorSel[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					curCol = COLORS[Integer.parseInt(((JButton) evt.getSource()).getName())];
				}
			});

		}
		pack();
		this.setSize(556, 530);
	}

	private void solveCube(ActionEvent evt) {
		StringBuffer s = new StringBuffer(150);

		for (int i = 0; i < 150; i++) {
			s.insert(i, '-');
		}

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 25; j++) {
				if (facelet[i][j].getBackground() == COLORS[0]) {
					s.setCharAt(25 * i + j, 'U');
				} else if (facelet[i][j].getBackground() == COLORS[1]) {
					s.setCharAt(25 * i + j, 'R');
				} else if (facelet[i][j].getBackground() == COLORS[2]) {
					s.setCharAt(25 * i + j, 'F');
				} else if (facelet[i][j].getBackground() == COLORS[3]) {
					s.setCharAt(25 * i + j, 'D');
				} else if (facelet[i][j].getBackground() == COLORS[4]) {
					s.setCharAt(25 * i + j, 'L');
				} else if (facelet[i][j].getBackground() == COLORS[5]) {
					s.setCharAt(25 * i + j, 'B');
				}
			}
		}

		String facelet = s.toString();
		System.out.println("Cube Definition String: " + facelet);
		if (showString) {
			JOptionPane.showMessageDialog(null, "Cube Definition String: " + facelet);
		}
		int mask = 0;
		mask |= useSeparator ? Search.USE_SEPARATOR : 0;
		// mask |= inverse ? Search.INVERSE_SOLUTION : 0;
		// mask |= showLength ? Search.APPEND_LENGTH : 0;
		long t = System.nanoTime();

		String[] ret = search.solveReduction(facelet, mask);
		String result = ret[0];
		if (!result.contains("Error")) {
			String solution333 = search333.solution(ret[1], 21, Integer.MAX_VALUE, 500, mask);
			result += solution333;
			if (showLength) {
				int length = 0;
				for (int i = 0; i < result.length(); i++) {
					if ("URFDLBurfdlb".indexOf(result.charAt(i)) != -1) {
						length++;
					}
				}
				result += String.format("(%df)", length);
			}
		}
		t = System.nanoTime() - t;
		// +++++++++++++++++++ Replace the error messages with more meaningful ones in your language ++++++++++++++++++++++
		if (result.contains("Error")) {
			switch (result.charAt(result.length() - 1)) {
			case '1':
				result = "There are not exactly 25 facelets of each color!";
				break;
			case '2':
				result = "There are not exactly 4 Tcenters of each color!";
				break;
			case '3':
				result = "There are not exactly 4 Xcenters of each color!";
				break;
			case '4':
				result = "Not all 12 edges exist exactly once!";
				break;
			case '5':
				result = "Not all 24 wedges exist exactly once!";
				break;
			case '6':
				result = "Not all 8 corners exist exactly once!";
				break;
			case '7':
				result = "Flip error: One edge has to be flipped!";
				break;
			case '8':
				result = "Twist error: One corner has to be twisted!";
				break;
			case '9':
				result = "Parity error: Two corners or two edges have to be exchanged!";
				break;
			}
		} else {
			ResultTextPane.setText(String.format("%s\n", result) + ResultTextPane.getText());
			ResultTextPane.requestFocusInWindow();
			ResultTextPane.select(0, result.length());
		}
		System.out.println("Result: " + result);
		JOptionPane.showMessageDialog(null, result, Double.toString((t / 1000) / 1000.0) + "ms", JOptionPane.INFORMATION_MESSAGE);
	}
}