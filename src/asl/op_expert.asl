// Agent mam in project pandemic_local

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// Si tiene que descartarse una carta se descarta la primera que encuentra (TODO: mejorar esto)
+cardMustBeenDiscarded : hasCard(op_expert, CARD) <- discardCard(CARD, op_expert); -cardMustBeenDiscarded.

// +myCity(CITY): isCIreachable(4) <- +toCreateCI(CITY); .print("Building CI in current city").

// Checks current city and moves to adjacent cell
// Es como un roomba roto
+!wander :  turn &
            myCity(CITY) &
            infectionLVL(CITY, CURR_CITY_INFECTION_LVL) &
            disease(TYPE, SPREADS_LEFT) &
            left_actions(ALEFT) &
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
                moveAdjacentCity(WORST_CITY);
                .print("END MOVING");
            }
            else{
                // Heals city if any infection found
                // RAWR: Should specify which disease to heal (also, it always heals a unit)
                if(infectionLVL(CITY, INFECTION_LVL) & INFECTION_LVL > 0){
                    +max_dis_lvl(0);
                    +worst_dis(null);
                    for (infected(CITY, DIS, DIS_LVL)) {
                        if (max_dis_lvl(MAX_LVL) & DIS_LVL > MAX_LVL) {
                            -max_dis_lvl(_);
                            -worst_dis(_);
                            +max_dis_lvl(DIS_LVL);
                            +worst_dis(DIS);
                        }
                    }
                    if (worst_dis(DIS) & DIS \== null) {
                        treatDisease(DIS);
                        .print("Trying to heal ", CITY, " disease ", DIS);
                        .print("END HEALING");
                    }
                    else {
                        moveAdjacentRandom;
                        .print("END WANDERING TO RANDOM");
                    }
                }
                
                // Moves to random adjacent cell if no infection
                else{
                    moveAdjacentRandom;
                    .print("END WANDERING TO RANDOM");
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

// Game over belief. Drops all intentions
+gameover : true <- .print("I lost, so I'm dropping all intentions..."); .drop_all_intentions.
