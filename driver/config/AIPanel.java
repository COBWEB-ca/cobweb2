/**
 *
 */
package driver.config;

import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cwcore.GeneticController;
import cwcore.GeneticControllerParams;
import cwcore.LinearWeightsController;
import cwcore.complexParams.ComplexEnvironmentParams;
import driver.Parser;
import driver.SettingsPanel;

class AIPanel extends SettingsPanel {

		/**
		 *
		 */
		private static final long serialVersionUID = 6045306756522429063L;

		static final String[] AI_LIST = {
			GeneticController.class.getName(),
			LinearWeightsController.class.getName() };

		CardLayout cardSwitch = new CardLayout();
		JPanel inner = new JPanel();

		JComboBox aiSwitch;

		SettingsPanel[] tabs;

		SettingsPanel genPanel = new SettingsPanel() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1139521733160862828L;

			{
				this.add(new JLabel("Nothing to configure"));
			}

			@Override
			public void bindToParser(Parser p) {
				ComplexEnvironmentParams ep = p.getEnvParams();
				if (!ep.controllerParams.getClass().equals(GeneticControllerParams.class)) {
					p.getEnvParams().controllerName = GeneticController.class.getName();
					p.getEnvParams().controllerParams = new GeneticControllerParams();
				}
			}

		};

		public AIPanel() {

		}

		private Parser parser;

		@Override
		public void bindToParser(Parser p) {
			parser = p;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			inner.setLayout(cardSwitch);


			tabs = new SettingsPanel[AI_LIST.length];
			inner.add(genPanel, AI_LIST[0]);
			tabs[0] = genPanel;

			SettingsPanel lWpanel = new LinearAIGUI();
			inner.add(lWpanel, AI_LIST[1]);
			tabs[1] = lWpanel;

			aiSwitch = new JComboBox(AI_LIST);
			aiSwitch.setEditable(false);
			aiSwitch.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					cardSwitch.show(inner, (String) e.getItem());
					tabs[aiSwitch.getSelectedIndex()].bindToParser(parser);
				}
			});

			add(aiSwitch);
			add(inner);

			aiSwitch.setSelectedItem(p.getEnvParams().controllerName);
		}

	}