package org.cobweb.cobweb2.ui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


public class CreditsDialog {

	/**
	 * Creates a dialog box with contact information about a specified person
	 * in the credits menu.
	 *
	 * @param parentDialog The credits dialog box that invoked the creation of this dialog box
	 * @param S The contact information that will be shown in the dialog box.
	 * @param length The length of the dialog box in pixels
	 * @param width The width of the dialog box in pixels
	 */
	private static void creditDialog(JDialog parentDialog, String[] S, int length, int width) {

		final javax.swing.JDialog creditDialog = new javax.swing.JDialog(parentDialog,
				"Click on Close to continue", true);

		JPanel credit = new JPanel();
		for (int i = 0; i < S.length; ++i) {
			credit.add(new JLabel(S[i]), "Center");
		}

		JPanel term = new JPanel();
		JButton close = new JButton("Close");
		term.add(close);
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				creditDialog.setVisible(false);
			}
		});

		creditDialog.add(credit, "Center");
		creditDialog.add(term, "South");

		creditDialog.setSize(length, width);
		creditDialog.setVisible(true);

	}

	/**
	 * The credits dialog box that is created when the user selects "Credits"
	 * located under "Help" in the main tool bar.  It contains a list of
	 * buttons for important people that can be contacted for more information
	 * about Cobweb.  The information can be accessed by clicking on the buttons.
	 */
	public static void show() {
		// TODO? update info
		final javax.swing.JDialog theDialog = new javax.swing.JDialog();
		theDialog.setTitle("Credits");
		theDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel credit = new JPanel();
		JButton brad = new JButton("Brad Bass, PhD");
		JButton jeff = new JButton("Jeff Hill");
		JButton jin = new JButton("Jin Soo Kang");
		credit.add(new JLabel("Coordinator"));
		credit.add(brad);
		credit.add(new JLabel("_______________"));
		credit.add(new JLabel("Programmers"));
		credit.add(jeff);
		credit.add(jin);

		JPanel term = new JPanel();
		JButton close = new JButton("Close");
		term.add(close);

		brad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String[] S = { "Brad Bass, PhD", "Adaptations and Impacts Research Group",
						"Environment Canada at Univ of Toronto", "Inst. for Environmental Studies",
						"33 Willcocks Street", "Toronto, Ont M5S 3E8 CANADA",
						"TEL: (416) 978-6285  FAX: (416) 978-3884", "brad.bass@ec.gc.ca" };
				creditDialog(theDialog, S, 300, 300);
			}
		});

		jeff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String[] S = { "Main Structural Programming By", "", "Jeff Hill", "oni1@home.com" };

				creditDialog(theDialog, S, 250, 150);
			}
		});

		jin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String[] S = { "Update & Additional Programming By", "", "Jin Soo Kang",
						"Undergraduate, Computer Science", "University of Toronto", "jin.kang@utoronto.ca",
				"[2000 - 2001]" };

				creditDialog(theDialog, S, 300, 250);
			}
		});

		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				theDialog.setVisible(false);
			}
		});

		theDialog.add(credit, "Center");
		theDialog.add(term, "South");
		theDialog.setSize(150, 265);
		theDialog.setVisible(true);
	}

}
