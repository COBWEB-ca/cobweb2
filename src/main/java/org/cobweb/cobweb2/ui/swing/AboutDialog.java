package org.cobweb.cobweb2.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.cobweb.util.Versionator;


public class AboutDialog {

	/**
	 * Creates the about dialog box, which contains information pertaining
	 * to the Cobweb version being used, and the date it was last modified.
	 */
	public static void show() {
		final javax.swing.JDialog whatDialog = new javax.swing.JDialog();
		whatDialog.setTitle("About COBWEB");
		whatDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel info = new JPanel();
		info.setAlignmentX(Component.CENTER_ALIGNMENT);
		info.add(new JLabel("<html><center>COBWEB2 2003/2011<br/>version: <br/>"
				+  Versionator.getVersion().replace(" ", "<br/>")
				+ "</center></html>"));

		JPanel term = new JPanel();
		JButton close = new JButton("Close");
		term.add(close);

		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				whatDialog.setVisible(false);
			}
		});

		whatDialog.setLayout(new BorderLayout());
		whatDialog.add(info, BorderLayout.CENTER);
		whatDialog.add(term, BorderLayout.SOUTH);
		whatDialog.setSize(300, 150);
		//whatDialog.pack();
		whatDialog.setVisible(true);
	}

}
