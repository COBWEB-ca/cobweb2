package org.cobweb.cobweb2.ui;

import java.io.IOException;
import java.io.PrintWriter;

import org.cobweb.cobweb2.ui.GridStats.CellStats;


public class RegionStatsReporter {

	public static void report(PrintWriter file, GridStats stats) throws IOException {
		file.print("grid column"); file.print('\t');
		file.print("grid row"); file.print('\t');

		file.print("x"); file.print('\t');
		file.print("y"); file.print('\t');

		file.print("width"); file.print('\t');
		file.print("height"); file.print('\t');

		file.print("area"); file.print('\t');

		for (int t = 0; t < stats.types; t++) {
			file.print("agent " + (t + 1) + " count"); file.print('\t');
			file.print("food " + (t + 1) + " count"); file.print('\t');
		}
		file.print("agent total count"); file.print('\t');
		file.print("food total count");
		file.println();

		for (int col = 0; col < stats.cellStats.length; col++) {
			CellStats[] colCells = stats.cellStats[col];

			for (int row = 0; row < colCells.length; row++) {
				CellStats cell = colCells[row];

				file.print(col + 1); file.print('\t');
				file.print(row + 1); file.print('\t');

				file.print(cell.xb + 1); file.print('\t');
				file.print(cell.yb + 1); file.print('\t');

				file.print(cell.w); file.print('\t');
				file.print(cell.h); file.print('\t');

				file.print(cell.area()); file.print('\t');

				for (int t = 0; t < stats.types; t++) {
					file.print(cell.agentCount[t]); file.print('\t');
					file.print(cell.foodCount[t]); file.print('\t');
				}
				file.print(cell.totalAgents()); file.print('\t');
				file.print(cell.totalFood());
				file.println();
			}
		}
	}
}
