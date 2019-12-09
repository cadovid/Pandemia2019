package game;

import java.util.*;

import _aux.Options;
import board.Board;
import board.Cell;
import card.Card;
import card.CityCard;
import city.City;
import dis.Disease;
import player.Player;

public final class Distance {
	public final int UP = 0;
	public final int RIGHT = 1;
	public final int DOWN = 2;
	public final int LEFT = 3;

	public static int[] manhattanDistanceTorus(int[] initialPosition, int[] finalPosition, int nRows, int nCols) {
		int movH, movV;
		// Calculo numero de casillas movimiento horizontal
		int hor = initialPosition[1] - finalPosition[1];
		if (hor < 0) {
			movH = 1;
			hor = hor * (-1);
		} else
			movH = 3;
		int finalHor = Math.min(hor, nCols - hor);
		// Calculo numero de casillas movimiento vertical
		int ver = initialPosition[0] - finalPosition[0];
		if (ver < 0) {
			movV = 2;
			ver = ver * (-1);
		} else
			movV = 0;
		int finalVer = Math.min(ver, nRows - ver);
		// Calculo direccion inicial del movimiento
		int mov = movH;
		if (finalHor == 0) {
			if (ver != finalVer) {
				movV = (movV + 2) % 4;
			}
			mov = movV;
		} else if (hor != finalHor) {
			mov = (movH + 2) % 4;
		}
		return new int[] { finalHor + finalVer, mov };
	}

	public static String[] routeChoice(Game game, String name, int[] finalPosition) {

		int[] initialPosition = null;
		ArrayList<CityCard> hand = null;
		for (Player player : game.players.values()) {
			if (name.equals(player.alias)) {
				hand = (ArrayList<CityCard>) player.getHand().values();
				City playerCity = player.getCity();
				Cell playerCell = playerCity.getCell();
				initialPosition = playerCell.getCoordinates();
			}
		}
		Disease diseaseIAmFollowing = null; // Acceder a las creencias y sacar la enfermedad que estoy persiguiendo

		// Calculo el coste de caminar entre los puntos
		int[] walkDistance = manhattanDistanceTorus(initialPosition, finalPosition, game.gs.board.n_rows,
				game.gs.board.n_cols);
		int minimumCostRoute = walkDistance[0];
		String nextAction = "" + walkDistance[1];
		boolean usoCarta = false;

		// Calculo el coste de utilizar un vuelo directo(Puedo volar hasta esa ciudad)
		for (Card card : hand) {
			Disease diseaseCard = card.getDisease();
			if (!diseaseIAmFollowing.equals(diseaseCard)) {
				City city = card.getCity();
				Cell cell = city.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
				int[] position = cell.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CELL
				int[] totalDistanceDirectFlight = manhattanDistanceTorus(position, finalPosition, game.gs.board.n_rows,
						game.gs.board.n_cols);
				int aux = totalDistanceDirectFlight[0] + 1;
				if (aux < minimumCostRoute) {
					minimumCostRoute = aux;
					nextAction = 4 + "----" + city.alias;
					usoCarta = true;
				}
			}
		}

		// Calculo el coste de utilizar un vuelo charter(Puedo volar a cualquier parte
		// desde esa ciudad)
		for (Card card : hand) {
			Disease diseaseCard = card.getDisease();
			if (!diseaseIAmFollowing.equals(diseaseCard)) {
				City city = card.getCity();
				Cell cell = city.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
				int[] position = cell.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CELL
				int[] totalDistanceDirectFlight = manhattanDistanceTorus(initialPosition, position,
						game.gs.board.n_rows, game.gs.board.n_cols);
				int aux = totalDistanceDirectFlight[0] + 1;
				if (aux < minimumCostRoute) {
					minimumCostRoute = aux;
					if (aux == 1) {
						for (City ciudad : game.cities.values()) {
							Cell cellCiudad = ciudad.getCell();
							int[] posicionesCoordenadas = cellCiudad.getCoordinates();
							if (posicionesCoordenadas[0] == position[0] && posicionesCoordenadas[1] == position[1]) {
								nextAction = 5 + "----" + ciudad.alias;
							}
						}

					} else {
						nextAction = "" + totalDistanceDirectFlight[1];
					}
					usoCarta = true;
				}
			}
		}

		// Calculo el coste de utilizar un air bridge(Puedo volar entre centros de
		// investigacion)
		ArrayList<City> cityList = new ArrayList<City>();
		for (City city : game.cities.values()) { // ESTA FUNCION SE DEBE IMPLEMENTAR EN LA CLASE GAME
			if (city.canResearch()) {
				cityList.add(city);
			}
		}
		for (City cityF : cityList) {
			boolean usoCartaCIF = false;
			Cell cellF = cityF.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
			int[] positionF = cellF.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CELL
			int[] totalDistanceToResearchCentreF = manhattanDistanceTorus(positionF, finalPosition,
					game.gs.board.n_rows, game.gs.board.n_cols);
			int distanceToResearchCentreF = totalDistanceToResearchCentreF[0];
			for (Card card : hand) {
				Disease diseaseCard = card.getDisease();
				if (!diseaseIAmFollowing.equals(diseaseCard)) {
					City city = card.getCity();
					Cell cell = city.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
					int[] position = cell.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CELL
					int[] totalDistanceDirectFlight = manhattanDistanceTorus(positionF, position, game.gs.board.n_rows,
							game.gs.board.n_cols);
					int aux = totalDistanceDirectFlight[0] + 1;
					if (aux < distanceToResearchCentreF) {
						distanceToResearchCentreF = aux;
						usoCartaCIF = true;
					}
				}
			}

			for (City cityI : cityList) {
				boolean usoCartaCII = false;
				if (!cityI.equals(cityF)) {

					Cell cellI = cityI.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
					int[] positionI = cellI.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CELL
					int[] totalDistanceToResearchCentreI = manhattanDistanceTorus(initialPosition, positionI,
							game.gs.board.n_rows, game.gs.board.n_cols);
					int distanceToResearchCentreI = totalDistanceToResearchCentreI[0];
					if (distanceToResearchCentreI == 0) {
						// totalDistanceToResearchCentreI[1] = 6 + "----" + cityF.alias;
						totalDistanceToResearchCentreI[1] = 6;
						break;
					}
					for (Card card : hand) {
						Disease diseaseCard = card.getDisease();
						if (!diseaseIAmFollowing.equals(diseaseCard)) {
							City city = card.getCity();
							Cell cell = city.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
							int[] position = cell.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE
																	// CELL
							int[] totalDistanceDirectFlight = manhattanDistanceTorus(positionI, position,
									game.gs.board.n_rows, game.gs.board.n_cols);
							int aux = totalDistanceDirectFlight[0] + 1;
							if (aux < distanceToResearchCentreI) {
								distanceToResearchCentreI = aux;
								// totalDistanceToResearchCentreI[1] = 4 + "----" + city.alias;
								totalDistanceToResearchCentreI[1] = 4;
								usoCartaCII = true;
							}
						}
					}
					int costeTotalVuelosEntreCI = distanceToResearchCentreI + 1 + distanceToResearchCentreF;
					if (costeTotalVuelosEntreCI < minimumCostRoute) {
						minimumCostRoute = costeTotalVuelosEntreCI;
						nextAction = "" + totalDistanceToResearchCentreI[1];
					} else if (costeTotalVuelosEntreCI == minimumCostRoute && usoCarta && !usoCartaCII
							&& !usoCartaCIF) {
						minimumCostRoute = costeTotalVuelosEntreCI;
						nextAction = "" + totalDistanceToResearchCentreI[1];
						usoCarta = false;
					}
				}
			}
		}
		return new String[] { "" + minimumCostRoute, nextAction };
	}

	public static String mostRepeatedDisease(Player agent, Game game) {
		Hashtable<Disease, Integer> tableDiseasesCounter = new Hashtable<Disease, Integer>();
		ArrayList<Disease> diseases = (ArrayList<Disease>) game.diseases.values();
		for (Disease disease : diseases) {
			if (disease.getCure()) {
				tableDiseasesCounter.put(disease, -1);
			} else {
				tableDiseasesCounter.put(disease, 0);
			}
		}
		ArrayList<CityCard> hand = (ArrayList<CityCard>) agent.getHand().values();
		for (Card card : hand) {
			int aux = tableDiseasesCounter.get(card.getDisease());
			if (aux != -1)
				tableDiseasesCounter.put(card.getDisease(), ++aux);
		}
		Enumeration e = tableDiseasesCounter.keys();
		Disease clave;
		int valor;
		int maxValue = -1;
		Disease maxDisease = null;
		while (e.hasMoreElements()) {
			clave = (Disease) e.nextElement();
			valor = tableDiseasesCounter.get(clave);
			if (valor > maxValue) {
				maxValue = valor;
				maxDisease = clave;
			}
		}
		return maxDisease.alias;
	} // AÑADIR COMO CREENCIA PERSEGUIR ESTA ENFERMEDAD AL AGENTE QUE INVOCA LA
		// FUNCION

	public static void findPlayerToAsk(String name, Game game, ArrayList<CityCard> cartasYaPreguntadas) {
		// En la inicializacion del juego es necesario crear un ArrayList vacio llamado
		// CartasYaPreguntadas
		// Al cambiar de turno el ArrayList CartasYaPreguntadas se vacia de nuevo

		Disease desiredDisease = null; // Acceder a las creencias y sacar la enfermedad deseada
		Player desiredPlayer = null;
		CityCard desiredCard = null;
		int minimumDistanceToCard = Integer.MAX_VALUE;
		for (Player player : game.players.values()) {
			ArrayList<CityCard> hand = (ArrayList<CityCard>) player.getHand().values();
			for (CityCard card : hand) {
				boolean ignorada = false;
				for (CityCard cardYaPreguntada : cartasYaPreguntadas) {
					if (cardYaPreguntada.equals(card)) {
						ignorada = true;
					}
				}
				if (!ignorada) {
					Disease diseaseCard = card.getDisease();
					String diseaseAlias = diseaseCard.alias;
					if (diseaseAlias.equals(desiredDisease.alias)) {
						City cityCard = card.getCity();
						Cell cityCell = cityCard.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
						int[] cardPosition = cityCell.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA
																		// CLASE CELL
						String[] calculusDistanceToCard = routeChoice(game, name, cardPosition);
						int distanceToCard = Integer.parseInt(calculusDistanceToCard[0]);
						Player possibleDesiredPlayer = player;
						if (distanceToCard < minimumDistanceToCard) {
							desiredPlayer = possibleDesiredPlayer;
							desiredCard = card;
						}
					}
				}
			}
		}
		cartasYaPreguntadas.add(desiredCard);
	} // Añadir como creencia que hay que preguntar a desiredPlayer que te facilite la
		// carta desiredCard

	public static void findCIToReach(Game game, String name) {
		int costNearestCI = Integer.MAX_VALUE;
		City nearestCI = null;
		for (City city : game.cities.values()) { // ESTA FUNCION SE DEBE IMPLEMENTAR EN LA CLASE GAME
			if (city.canResearch()) {
				Cell cell = city.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
				int[] positionCI = cell.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CELL
				String[] aux = routeChoice(game, name, positionCI);
				if (costNearestCI > Integer.parseInt(aux[0])) {
					costNearestCI = Integer.parseInt(aux[0]);
					nearestCI = city;
				}
			}
		}
		// Añadir la creencia de que tengo que ir al centro de investigación nearestCI
	}

	public static String[] shortRouteChoice(Game game, String name, int[] finalPosition) {

		int[] initialPosition = null;
		for (Player player : game.players.values()) { // ESTA FUNCION SE DEBE IMPLEMENTAR EN LA CLASE GAME
			if (name.equals(player.alias)) {
				City playerCity = player.getCity();
				Cell playerCell = playerCity.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
				initialPosition = playerCell.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CELL
				break;
			}
		}
		// Calculo el coste de caminar entre los puntos
		int[] walkDistance = manhattanDistanceTorus(initialPosition, finalPosition, game.gs.board.n_rows,
				game.gs.board.n_cols);
		int minimumCostRoute = walkDistance[0];
		String nextAction = "" + walkDistance[1];
		return new String[] { "" + minimumCostRoute, nextAction };
	}

	public static void adjacentCube(Game game, String name) {
		boolean existAdjacentCube = false;
		City initialCity = null;
		for (Player player : game.players.values()) { // ESTA FUNCION SE DEBE IMPLEMENTAR EN LA CLASE GAME
			if (name.equals(player.alias)) {
				initialCity = player.getCity();
				break;
			}
		}
		for (City city : initialCity.getNeighbors().values()) {
			if (city.getEpidemics() != null) {
				existAdjacentCube = true;
				break;
			}
		}
		if (existAdjacentCube) {
			// Añadir la creencia de que tengo al menos un cubo en una posición adyacente.
		} else {
			// Añadir la creencia de que tengo no tengo en una posición adyacente.
		}
	}

	public static void findNearestCube(Game game, String name) {
		int nearestCubeDistance = Integer.MAX_VALUE;
		City cityNearestCube = null;
		for (City city : game.cities.values()) {
			if (city.getEpidemics() != null) {
				Cell cell = city.getCell();
				int[] finalPosition = cell.getCoordinates();
				String[] aux = shortRouteChoice(game, name, finalPosition);
				if (Integer.parseInt(aux[0]) < nearestCubeDistance) {
					nearestCubeDistance = Integer.parseInt(aux[0]);
					cityNearestCube = city;
				}
			}
		}
		// Añadir la creencia de que tengo que ir a la ciudad cityNearestCube a limpiar
		// cubos.
	}

	public static void moveToFarObjective(Game game, String name, int[] finalPosition) {
		String[] route = routeChoice(game, name, finalPosition);
		String nextAction = route[1];
		String[] partsAction = nextAction.split("----");
		switch (partsAction[0]) {
		case "0":
			// mover el agente "name" hacia arriba
			break;
		case "1":
			// mover el agente "name" hacia la derecha
			break;
		case "2":
			// mover el agente "name" hacia abajo
			break;
		case "3":
			// mover el agente "name" hacia la izquierda
			break;
		case "4":
			// mover el agente "name" hacia la ciudad con alias partsAction[1] con vuelo
			// normal
			break;
		case "5":
			// mover el agente "name" hacia la ciudad con alias partsAction[1] con vuelo
			// charter
			break;
		case "6":
			// mover el agente "name" hacia la ciudad con alias partsAction[1] con vuelo
			// entre CI
			break;
		}
	}

	public static void moveToNearObjective(Game game, String name, int[] finalPosition) {
		String[] nextAction = shortRouteChoice(game, name, finalPosition);
		switch (nextAction[1]) {
		case "0":
			// mover el agente "name" hacia arriba
			break;
		case "1":
			// mover el agente "name" hacia la derecha
			break;
		case "2":
			// mover el agente "name" hacia abajo
			break;
		case "3":
			// mover el agente "name" hacia la izquierda
			break;
		}
	}

	public void pass(String name) {
		// Poner a 0 el número de acciones que me quedan por realizar y pasar el turno.
	}

	public void smartDiscard(Game game, String name) {
		int savedCardsNumber = 0;
		Disease diseaseIAmFollowing = null; // Acceder a las creencias y sacar la enfermedad que estoy persiguiendo
		int[] initialPosition = null;
		ArrayList<CityCard> hand = null;
		for (Player player : game.players.values()) {
			if (name.equals(player.alias)) {
				hand = (ArrayList<CityCard>) player.getHand().values();
				City playerCity = player.getCity();
				Cell playerCell = playerCity.getCell(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CITY
				initialPosition = playerCell.getCoordinates(); // ESTA FUNCION SE TIENE QUE IMPLEMENTAR EN LA CLASE CELL
			}
		}
		for (Card card : hand) {
			Disease diseaseCard = card.getDisease();
			if (!diseaseIAmFollowing.equals(diseaseCard)) {
				hand.remove(card);
				++savedCardsNumber;
				if (savedCardsNumber == Options.PLAYER_MAX_CARDS) {
					for (Card eliminatedCard : hand) {
						// Descartar carta eliminatedCard
					}
					return;
				}
			}
		}

		/*
		 * Errores de compilacion aqui if (/*tengo creencia de quiero ir a una posición
		 *//*
			 * ){ //Obtengo las coordenadas de la posición que quiero alcanzar. //En un
			 * bucle hasta que la posición inicial sea igual a la final: //Llamo a
			 * routeChoice. //Si routeChoice me da como acción 4-x significa que va a usar
			 * la carta parteneciente a la posición x para volar /*hand.remove(/* card
			 * perteneciente a x
			 *//*
				 * ); ++savedCardsNumber; if (savedCardsNumber == Options.PLAYER_MAX_CARDS) {
				 * for (Card eliminatedCard : hand){ // Descartar carta eliminatedCard } return;
				 * } //Si routeChoice me da como acción 5-x significa que va a usar la carta
				 * parteneciente a la posición actual para volar hand.remove(/* card
				 * perteneciente a la posición actual
				 *//*
					 * ); ++savedCardsNumber; if (savedCardsNumber == Options.PLAYER_MAX_CARDS{ for
					 * (Card eliminatedCard : hand){ // Descartar carta eliminatedCard } return; }
					 * //Actualizo las coordenadas posición actual dependiendo de la acción
					 * realizada }
					 */

		for (Card card : hand) {
			hand.remove(card);
			++savedCardsNumber;
			if (savedCardsNumber == Options.PLAYER_MAX_CARDS) {
				for (Card eliminatedCard : hand) {
					// Descartar carta eliminatedCard
				}
				return;
			}
		}
	}

	public static void main(String[] args) {

	}
}