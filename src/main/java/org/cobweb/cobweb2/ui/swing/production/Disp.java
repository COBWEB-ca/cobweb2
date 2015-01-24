package org.cobweb.cobweb2.ui.swing.production;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.cobweb.cobweb2.plugins.production.ProductionMapper;
import org.cobweb.cobweb2.ui.UpdatableUI;
import org.cobweb.swingutil.WaitableJComponent;
import org.cobweb.util.ArrayUtilities;

public class Disp extends JFrame implements UpdatableUI {
	/**
	 *
	 */
	private final ProductionMapper productionMapper;
	/**
	 *
	 */
	private static final long serialVersionUID = 8153897860751883610L;

	private WaitableJComponent display;
	private float[][] tiles;
	private float max;

	Disp(ProductionMapper productionMapper) {
		super("Production Map");
		this.productionMapper = productionMapper;

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		display = new ProductionPanel();
		this.setSize(400, 400);
		this.add(display);

		setVisible(true);
	}

	class ProductionPanel extends WaitableJComponent {
		private static final long serialVersionUID = -9218406889029231853L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponents(g);
			float[][] myTiles = tiles;
			float myMax = max;

			int width = myTiles.length;
			int height = myTiles[0].length;

			int screenW = getWidth();
			int screenH = getHeight();
			final int tw = screenW / width;
			final int th = screenH / height;

			int shiftX = (screenW - width * tw) / 2;
			int shiftY = (screenH - height * th) / 2;

			g.translate(shiftX, shiftY);

			for (int xc = 0; xc < width; xc++) {
				for (int yc = 0; yc < height; yc++) {
					int amount = 255 - (int) ((Math.min(myTiles[xc][yc], myMax) / myMax) * 255f);
					Color c = new Color(amount / 2 + 127, amount, 255);
					g.setColor(c);
					g.fillRect(xc * tw, yc * th, tw, th);
				}
			}

		}
	}

	@Override
	public void update(boolean synchronous) {
		tiles = ArrayUtilities.clone(productionMapper.getValues());
		max = productionMapper.getMax();

		if (synchronous || display.isReadyToRefresh())
			display.refresh(synchronous);
	}

	@Override
	public boolean isReadyToUpdate() {
		return true;
	}

	@Override
	public void onStopped() {
		update(true);
	}

	@Override
	public void onStarted() {
		// Nothing
	}
}