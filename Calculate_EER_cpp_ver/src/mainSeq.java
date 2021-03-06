import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.LayoutFocusTraversalPolicy;

public class mainSeq {
	static double thresholdTerm = 0.0;
	static CCFarFrr cfr;
	public static void main(String[] args) {

		double[] value = new double[2];
		String notInput = "input Threshold Term";
				
		Frame mFrame = new Frame();
		Panel tPanel = new Panel(new FlowLayout());
		Button btn1 = new Button("Load HD DB");
		Button btn2 = new Button("Run");
		TextField tf1 = new TextField(notInput);
		FileDialog fd = new FileDialog(mFrame);

		mFrame.setLayout(new BorderLayout());
		mFrame.setSize(400, 100);
		mFrame.add(tPanel, BorderLayout.CENTER);

		mFrame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				mFrame.setVisible(false);
				mFrame.dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				mFrame.setVisible(false);
				mFrame.dispose();

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}
		});
		btn1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String dir;
				fd.setVisible(true);
				dir = fd.getDirectory() + fd.getFile();
				
				cfr = new CCFarFrr(dir);

				cfr.separateAccount();
				cfr.countRecognition();
				cfr.separateHisto();
			}
		});

		btn2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == btn2) {
					double[] value = new double[4];
					double minEER = 100.0;
					double minFAR = 100.0;
					double minFRR = 100.0;
					double minDPrime = 100.0;
					double minEERThreshold = 100.0;
					double minDPrimeThreshold = 100.0;
					double minFARThreshold = 100.0;
					double minFRRThreshold = 100.0;
					Dialog resultView;
					TextArea ta = new TextArea();
					
					if(!tf1.getText().equals(notInput)) {
						thresholdTerm = Double.parseDouble(tf1.getText());
						for(double f = 0; f < 1; f += thresholdTerm) {
							cfr.setThreshold(f);
							// return Value [0] = EER, [1] = dPrime
							value = cfr.calculateEER();
							
							if(value[0] < minEER) {
								minEERThreshold = f;
								minEER = value[0];
							}
							
							if(value[1] < minDPrime) {
								minDPrimeThreshold = f;
								minDPrime = value[1];
							}
							
							if(value[2] < minFAR) {
								minFARThreshold = f;
								minFAR = value[2];
							}
							
							if(value[3] < minFRR) {
								minFRRThreshold = f;
								minFRR = value[3];
							}
							cfr.setThreshold(thresholdTerm);
							cfr.calculateEER();
						}


						
						resultView = new Dialog(mFrame);
						resultView.setSize(400, 500);
						resultView.setLayout(new BorderLayout());
						resultView.addWindowListener(new WindowListener() {
							
							@Override
							public void windowOpened(WindowEvent e) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void windowIconified(WindowEvent e) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void windowDeiconified(WindowEvent e) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void windowDeactivated(WindowEvent e) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void windowClosing(WindowEvent e) {
								resultView.setVisible(false);
								resultView.dispose();
							}
							
							@Override
							public void windowClosed(WindowEvent e) {
								resultView.setVisible(false);
								resultView.dispose();
								
							}
							
							@Override
							public void windowActivated(WindowEvent e) {
								// TODO Auto-generated method stub
								
							}
						});
						
						ta.setColumns(3);
						ta.append("minFARThrehsold : "+ minFARThreshold);
						ta.append("\n");
						ta.append("minFAR : "+minFAR);
						ta.append("\n");
						ta.append("-----------------------\n\n");
						ta.append("minFRRThrehsold : "+ minFRRThreshold);
						ta.append("\n");
						ta.append("minFRR : "+minFRR);
						ta.append("\n");
						ta.append("-----------------------\n\n");
						ta.append("minEERThreshold : "+minEERThreshold);
						ta.append("\n");
						ta.append("minEER : "+minEER);
						ta.append("\n");
						ta.append("-----------------------\n\n");
						ta.append("minDprimeThreshold : "+minDPrimeThreshold);
						ta.append("\n");
						ta.append("min d' : "+minDPrime);
						ta.append("\n");
						ta.append("-----------------------\n\n");
						
						resultView.add(ta);
						resultView.setVisible(true);
					}
				}
			}
		});

		tPanel.add(btn1);
		tPanel.add(tf1);
		tPanel.add(btn2);

		mFrame.setVisible(true);

	}
}
