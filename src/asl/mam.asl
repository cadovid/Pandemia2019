// Agent mam in project pandemic_local

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */


// Checks current city and moves to adjacent cell
// Es como un roomba roto
+!wander : 	turn &
			at(CITY) &
			infectionLVL(CITY, CURR_CITY_INFECTION_LVL) &
			disease(TYPE, SPREADS_LEFT) &
			actions_left(ALEFT) &
			ALEFT > 0
			<-
			// First considered city to wander to is the city in which the player currently is
			// Evaluates its infection level, or considers no infection at all if belief doesn't exists
			-wander_worst(_, _);
			+wander_worst(CITY, CURR_CITY_INFECTION_LVL);
			
			// Evaluates infection level of adjacent cities and updates preferred city to wander to depending on the infection level
			for(adjacent(CITY, ANY_OTHER_CITY)){
				if(infectionLVL(ANY_OTHER_CITY, INFECTION_LEVEL) &
					INFECTION_LEVEL > CURR_CITY_INFECTION_LVL){	
					-wander_worst(_,_);
					+wander_worst(ANY_OTHER_CITY, INFECTION_LEVEL)
				}
				
				//.print("adjacent city belief: ", adjacent(CITY, ANY_OTHER_CITY))
			}
			
			// If already in worst city, reduces infection, else, moves to adjacent city
			// Move action is handled by the environment (beliefs must be updated from environment)
			if(wander_worst(WORST_CITY,_) & CITY \== WORST_CITY){
				.print("Trying to move to ", WORST_CITY);
				moveto(WORST_CITY);
				.print("END MOVING");
				-actions_left(_);
				+actions_left(ALEFT - 1)
			}
			else{
				// Heals city if any infection found
				// RAWR: Should specify which disease to heal (also, it always heals a unit)
				if(infectionLVL(CITY, INFECTION_LVL) & INFECTION_LVL > 0){
					.print("Trying to heal ", CITY);
					heal(CITY);
					.print("END HEALING");
					-actions_left(_);
					+actions_left(ALEFT - 1)
				}
				
				// Moves to random adjacent cell if no infection
				else{
					moveto_adjacentRandom(CITY);
					.print("END WANDERING TO RANDOM");
					-actions_left(_);
					+actions_left(ALEFT - 1)
				}
			}
			
			// Agent continues wandering until all diseases are erradicated
			!wander.

// If no diseases left, agent stops wandering			
+!wander : turn & not disease(_,_) <- .print("No diseases left!").			
+!wander : true <- .print("Fail event. This should never be reached!").


// Whenever a player starts its turn, the number of actions left is updated
+turn : true <- .print("New turn");
				!wander.

// When the agent runs out of actions, tells the environment
// Depending on the game control, the MAS system stops until some feedback has been received from the player or after some timeout
+actions_left(ALEFT) : ALEFT == 0 &
						(control_timeout(T) | control_manual)
						<-
						turnover;
						if(control_manual){
							.print("**********Awaiting feedback...**********");
							.wait({+control_run})
						}
						else{ 
							.wait(T)
						}.

// Game over belief. Drops all intentions
+gameover : true <- .print("I lost, so I'm dropping all intentions..."); .drop_all_intentions.
