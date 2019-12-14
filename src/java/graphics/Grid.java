package graphics;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import board.Board;
import board.Cell;
import city.City;

public class Grid extends Box {

	private int n_rows;
	private int n_cols;
	private Board board;
	private GridCell[][] grid_cells;
	private Hashtable<String, GridCell> grid_cityCells; // Used for quick cell update when an action occurs in a
														// specific city

	// Actual panel dimensions (pixels)
	private int cell_size;

	// Constructors
	public Grid(Board board, Renderer r, int cell_size) {
		this.r = r;
		this.cell_size = cell_size;
		this.grid_cityCells = new Hashtable<String, GridCell>();
		this.board = board;
		int[] dimensions = board.getDimensions();
		this.n_rows = dimensions[0];
		this.n_cols = dimensions[1];

		this.width = this.cell_size * this.n_cols;
		this.height = this.cell_size * this.n_rows;

		this.grid_cells = new GridCell[this.n_rows][this.n_cols];

		this.updateSize();

		this.setLayout(new GridLayout(this.n_rows, this.n_cols, 0, 0));
		this.drawCells();
	}

	/*
	 * Refresh the grid cells containing the specified city aliases
	 */
	public void refresh(ArrayList<String> city_aliases) {
		for (String city_alias : city_aliases) {
			// Retrieves relevant grid cell and calls its update method
			GridCell gcell = this.grid_cityCells.get(city_alias);
			if (gcell == null) {
				System.out.printf("conflict with city %s\n", city_alias);
			}
			gcell.refresh();
			// System.out.println("updated city "+city_alias);
		}
	}

	/*
	 * Resolves the grid graphics
	 */
	public void drawCells() {

		// Draws grid cells one by one
		for (int row = 0; row < this.n_rows; row++) {
			// System.out.printf(">>>>>>row %d\n", row);
			for (int col = 0; col < this.n_cols; col++) {
				// System.out.printf(">>col %d\n", col);
				// Initializes graphic object and adds it to the main panel
				// Also updates the control array (grid_cells)
				Cell cell_board = this.board.getCell(row, col);
				City cell_city = cell_board.getCity();
				GridCell gcell = new GridCell(this.cell_size, cell_board, this.r);

				// If cell contains a city, save data to hashtable
				if (cell_city != null) {
					String cell_cityAlias = cell_city.alias;

					grid_cityCells.put(cell_cityAlias, gcell);
				}

				this.grid_cells[row][col] = gcell;
				this.add(gcell);
			}
		}
	}
}
