package board;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import city.City;
import _aux.Options;
import _aux.CustomTypes;
import _aux.CustomTypes.Direction;

/*
  Board class
    Mimics the game board. Composed by individual cells disposed in bidimensional arrays
*/
public class Board {
	private int[][] map_threat;
	private Cell[][] map;
	private int n_cols;
	private int n_rows;

	// Constructors
	public Board(Cell[][] map, int w, int h) {
		this.map = map;
		this.n_cols = w;
		this.n_rows = h;

		// Threat map. Initially zeroed across all cells
		this.map_threat = new int[h][w];
	}

	/*
	 * Constructor Loads map from csv file. Expected format:
	 * 
	 * <number of rows>;<number of columns> [<city alias or 0 if none>;...] [...]
	 ** 
	 * PARAMETERS: datapath: String; path to map valid_cities: Dictionary; Contains
	 * the valid city aliases as dictionary keys. Each value is the actual City
	 * object
	 */
	public Board(String datapath, Hashtable<String, City> valid_cities) {

		// Read csv lines creating stream from file
		try {
			BufferedReader br = new BufferedReader(new FileReader(datapath));
			// Reads map data from subsequent lines (Must follow the expected map format)
			// Layout must be regular! (same number of columns across all rows)
			String line;
			String[] line_data;

			// Control list. Cities can only be set once
			ArrayList<String> picked_cities = new ArrayList<String>();

			// Iterable indices
			int n_row = 0;
			int n_col = 0;

			// Tries to read map dimensions from first line
			line_data = br.readLine().split(";");
			if (line_data.length != 2) {
				if (Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
					System.out
							.printf("[Board] CRITICAL: Can't load map from file. Expected dimensions in first line.\n");

				System.exit(0);
			}

			try {
				// Sets board dimensions
				this.n_rows = Integer.parseInt(line_data[0]);
				this.n_cols = Integer.parseInt(line_data[1]);

				// Builds static map array
				this.map = new Cell[n_rows][n_cols];

				// Loads actual map reading line by line the input file
				while ((line = br.readLine()) != null) {
					line_data = line.split(";");
					Cell cell;

					if (line_data.length != n_cols) {
						if (Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
							System.out.printf(
									"[Board] CRITICAL: Can't load map from file. Wrong number of columns at row %d. Expected %d, found %d.\n",
									n_row, n_cols, line_data.length);

						System.exit(0);
					}

					// Iterates through every line element and checks if contains a valid city alias
					for (String cell_text : line_data) {
						cell = new Cell(n_col, n_row);

						// Creates city and cross reference between itself and the assigned cell
						if (valid_cities.containsKey(cell_text) && !picked_cities.contains(cell_text)) {
							City city = valid_cities.get(cell_text);
							cell.setCity(city);
							city.setCell(cell);

							// City won't be loaded twice
							picked_cities.add(cell_text);
						} else if (picked_cities.contains(cell_text)) {
							if (Options.LOG.ordinal() >= CustomTypes.LogLevel.WARN.ordinal())
								System.out.printf("[Board] WARN: City %s already set. Ignoring...\n", cell_text);
						}

						// Assigns cell to map (either empty or with an actual city)
						this.map[n_row][n_col++] = cell;
					}

					n_row++;
					n_col = 0;
				}

				// Threat map. Initially zeroed across all cells
				this.map_threat = new int[this.n_rows][this.n_cols];

				// Updates valid_cities dictionary. Only used cities are retained (updates the
				// main city map in the game!!)
				valid_cities.keySet().retainAll(picked_cities);

			} catch (NumberFormatException e) {
				if (Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
					System.out
							.printf("[Board] CRITICAL: Can't load map from file. Expected dimensions in first line.\n");

				System.err.println(e.getMessage());
				System.exit(0);
			}

			br.close();
		} catch (Exception e) {
			if (Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
				System.out.printf("[Board] CRITICAL: Error while parsing\n");

			System.err.println(e.getMessage());
			System.exit(0);
		}

	}

	// Setters/getters
	public int[] getDimensions() {
		return new int[] { this.n_rows, this.n_cols };
	}

	public Cell getCell(int row, int col) {
		return this.map[row][col];
	}

	// TODO
	/*
	 * Resolves adjacent cities
	 */
	public static void resolveAdjacentCities(Board board, Hashtable<String, City> cities) {
		Cell current, adjacent;
		int pos_x, pos_y, new_x, new_y;
		for (City city : cities.values()) {
			Hashtable<Direction, City> local_adjacents = new Hashtable<Direction, City>();
			current = city.getCell();
			pos_x = current.x;
			pos_y = current.y;

			// Adjacent city, left
			if (pos_x - 1 < 0) {
				new_x = board.n_cols - 1;
			} else {
				new_x = pos_x - 1;
			}
			adjacent = board.map[new_x][pos_y];
			local_adjacents.put(Direction.LEFT, adjacent.getCity());

			// Adjacent city, right
			if (pos_x + 1 >= board.n_cols) {
				new_x = 0;
			} else {
				new_x = pos_x + 1;
			}
			adjacent = board.map[new_x][pos_y];
			local_adjacents.put(Direction.RIGHT, adjacent.getCity());

			// Adjacent city, up
			if (pos_y - 1 < 0) {
				new_y = board.n_rows - 1;
			} else {
				new_y = pos_y - 1;
			}
			adjacent = board.map[pos_x][new_y];
			local_adjacents.put(Direction.UP, adjacent.getCity());

			// Adjacent city, down
			if (pos_y + 1 >= board.n_rows) {
				new_y = 0;
			} else {
				new_y = pos_y + 1;
			}
			adjacent = board.map[pos_x][new_y];
			local_adjacents.put(Direction.DOWN, adjacent.getCity());
			city.setNeighbors(local_adjacents);
		}

		return;
	}

	// TODO
	/*
	 * Returns a sorted list of Cell objects, which represents the shortest path
	 * between two cells in the board
	 */
	public ArrayList<Cell> getPath(Cell origin, Cell target) {
		return null;
	}

	// Dummy method to print board data
	public void dump() {
		for (Cell[] c_row : map) {
			for (Cell c : c_row) {
				if (c.isEmpty()) {
					System.out.printf("NIL;");
				} else {
					System.out.printf("%s;", c.getCity().alias);
				}
			}
			System.out.println();
		}
	}
}
