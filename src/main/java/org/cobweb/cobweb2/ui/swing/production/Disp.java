package org.cobweb.cobweb2.ui.swing.production;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.cobweb.cobweb2.core.Scheduler;
import org.cobweb.cobweb2.production.ProductionMapper;
import org.cobweb.swingutil.WaitableJComponent;

public class Disp extends JFrame implements Scheduler.Client {
	/**
	 * 
	 */
	private final ProductionMapper productionMapper;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8153897860751883610L;
	int x;
	int y;

	WaitableJComponent display;

	Disp(ProductionMapper productionMapper, int x, int y) {

		super("Production Map");
		this.productionMapper = productionMapper;
		this.x = x;
		this.y = y;


		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		display = new ProductionPanel();
		this.setSize(400, 400);
		this.add(display);

		setVisible(true);
	}

	public void refresh() {
		if (display.isReadyToRefresh())
			display.refresh(false);
	}

	class ProductionPanel extends WaitableJComponent {
		private static final long serialVersionUID = -9218406889029231853L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponents(g);

			int w = getWidth();
			int h = getHeight();
			final int tw = w / x;
			final int th = h / y;

			int shiftX = (w - x * tw) / 2;
			int shiftY = (h - y * th) / 2;

			g.translate(shiftX, shiftY);

			final Color[][] tiles = Disp.this.productionMapper.getTileColors(x, y);
			for (int xc = 0; xc < x; xc++) {
				for (int yc = 0; yc < y; yc++) {
					g.setColor(tiles[xc][yc]);
					g.fillRect(xc * tw, yc * th, tw, th);
				}
			}

		}
	}

	@Override
	public void tickNotification(long time) {
		refresh();
	}

	@Override
	public void tickZero() {
		// do nothing
	}
}