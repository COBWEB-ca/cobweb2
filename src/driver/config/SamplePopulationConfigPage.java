package driver.config;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class SamplePopulationConfigPage extends JFrame {

	private final class OkButtonListener implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent evt) {


		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -989030369586898897L;

	public static void main (String [] args) {

		new SamplePopulationConfigPage();

	}

	Container thePanel;

	public SamplePopulationConfigPage() {
		initPanel();
	}

	public void initPanel(){

		thePanel = this.getContentPane();
		JButton ok = new JButton("OK");
		ok.setMaximumSize(new Dimension(80, 20));
		ok.addActionListener(new OkButtonListener());

		thePanel.setLayout(new GridLayout(0, 1));

		JPanel buttonPanel = new JPanel();
		JButton popupDialogButton = new JButton("Show Option Pane(s)");
		//popupDialogButton.addActionListener(this);
		buttonPanel.add(popupDialogButton);
		thePanel.add(buttonPanel);

		//
		//		String bigList[] = new String[30];
		//
		//		bigList[0] = "Replace current population";
		//		bigList[1] = "Merge with current population";
		//
		//		JOptionPane.showInputDialog(null, "Pick a printer", "Input", JOptionPane.QUESTION_MESSAGE,
		//				null, bigList, "Titan");

		JRadioButton b1 = new JRadioButton("Option 1");
		JRadioButton b2 = new JRadioButton("Option 2");
		JRadioButton b3 = new JRadioButton("Option 3");
		b1.setSelected(true);

		ButtonGroup group = new ButtonGroup();
		group.add(b1);
		group.add(b2);
		group.add(b3);

		JTextField name = new JTextField(30);

		Object[] array = {
				new JLabel("Select an option:"),
				b1,
				b2,
				b3,
				new JLabel("Enter a name:"),
				name
		};


		int res = JOptionPane.showConfirmDialog(null, array, "Select", 
				JOptionPane.OK_CANCEL_OPTION);

		System.out.println(res);

	}

	public void validateUI() throws IllegalArgumentException {

	}






}
