// Agent mam in project pandemic_local

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// Si tiene que descartarse una carta se descarta la primera que encuentra (TODO: mejorar esto)
+cardMustBeenDiscarded : hasCard(op_expert, CARD) <- discardCard(CARD, op_expert); -cardMustBeenDiscarded.

//Vamos a descubrir la cura
+!getCure(DISEASE): turn & myCity(CURRENT_CITY) & hasCI(CURRENT_CITY) <- .print("Cure has been discovered"); discoverCure(DISEASE); -middlePlan("GET_CURE"); passTurn.
+!getCure(DISEASE): turn & myCity(CURRENT_CITY) & closestCI(CURRENT_CITY, CLOSEST_CI) <- .print("Trying to reach closest CI to discover cure");
                    moveToFarObjective(CLOSEST_CI, DISEASE); !getCure(DISEASE); +middlePlan("GET_CURE").
+!getCure(DISEASE): not turn <- !getCure(DISEASE); +middlePlan("GET_CURE").

//Compartimos Informacion
+!shareInfo(CITY, AG): turn & myCity(CITY) &  atCity(AG, CITY)<- shareInfo(AG, CITY, true);
                     -middlePlan(SHARE_INFO); -willShareInfo(CITY, AG); 
                     .broadcast(untell, willShareInfo(CITY, AG)); passTurn.
                     
+!shareInfo(CITY, AG): turn & not myCity(CITY) & at(CITY, VIRUS, _, _) <- moveToFarObjective(CITY, VIRUS); !shareInfo(CITY, AG).

+!shareInfo(CITY, AG): turn & myCity(CITY) &  not atCity(AG, CITY) <-!shareInfo(CITY, AG); passTurn.

+!shareInfo(CITY, AG): not turn <- !shareInfo(CITY, AG).


//Miramos que ciudad es la peor
+!checkWorstCity: turn & myCity(CURRENT_CITY) <-
                    +noRisk;
                    for (infected(CITY, DIS, 3)) {
                        if (noRisk & distance(CURRENT_CITY, CITY, DISTANCE) & not willHeal(CITY, DIS, 1)){
                            +worstCity(CITY, DISTANCE, DIS);
                            -noRisk;
                        }elif(distance(CURRENT_CITY, CITY, DISTANCE) & worstCity(WORST_CITY, WORST_DISTANCE, DIS) & not healing(DIS, CITY) & (DISTANCE < WORST_DISTANCE)){
                            -worstCity(_,_,_);
                            +worstCity(CITY, DISTANCE, DIS);    
                        }
                    }
                    if (worstCity(CITY, DISTANCE, DIS)){
                        if (noRisk){
                            -noRisk;
                            !noRiskPlan;
                        }else{
                            !goToHeal(CITY, DISTANCE, DIS);
                            .broadcast(tell, willHeal(CITY, DIS, 1));
                            +willHeal(CITY, DIS, 1);
                            +middlePlan("GO_TO_CURE");
                        }
                        
                    }
                    ;
                    .print("CHECKING WORST CITY");.
                    
+!checkWorstCity: not turn <- !checkWorstCity.

//Si no hay ciudades en riesgo de brote, miramos si se necesita un CI
+!noRiskPlan: turn <- .print("NO RISK IN THE CITY"); !checkIfNeedCI; +middlePlan(BUILD_CI).
+!noRiskPlan: not turn <- !noRiskPlan.

//Si hay ciudades en riesgo de brote, vamos pa tratarla
+!goToHeal(CITY, DISTANCE, DIS): turn & myCity(CITY) <- treatDisease(DIS); -worstCity(CITY, DISTANCE, DIS);                                 
.broadcast(untell, willHeal(CITY, DIS, 3)); -middlePlan("GO_TO_CURE"); -willHeal(CITY, DIS, 1); passTurn.
+!goToHeal(CITY, DISTANCE, DIS): turn & not myCity(CITY) <- moveToFarObjective(CITY, DIS); !goToHeal(CITY, DISTANCE, DIS).
+!goToHeal(CITY, DISTANCE, DIS): not turn <- !goToHeal(CITY, DISTANCE, DIS).

//Planes de construir un CI
+!buildCI(CITY): turn & not myCity(CITY) & needCI(CITY) <- moveToFarObjective(CITY, DIS); !buildCI(CITY).
+!buildCI(CITY): turn & myCity(CITY) & needCI(CITY) <- buildCI; .print("Building CI in current city");
                                                     -middlePlan(BUILD_CI); -needCI(CITY); .broadcast(untell, agreeBuildCI(CITY)); passTurn.
+!buildCI(CITY): turn & myCity(City) & not needCI(CITY) <- -middlePlan(BUILD_CI); passTurn.
+!checkIfNeedCI: turn & myCity(CITY) <- isCIreachable(4); !buildCI(CITY).

//Si tenemos 5 cartas, vamos a descubrir la cura
+myCardsNumber(DISEASE,5): not isCured(DISEASE) <- !getCure(DISEASE).

// Si le solicitan curar una ciudad y no esta yendo a curar otra, acepta
+!heal(CITY, VIRUS, QUANTITY) : not willHeal(_, _, _) <-
    !goToHeal(CITY, DISTANCE, DIS);
    +willHeal(CITY, VIRUS, QUANTITY);
    +middlePlan("GO_TO_CURE");
    .broadcast(tell, willHeal(CITY, VIRUS, QUANTITY)).
    
    
// En otro caso, lo rechaza
 +!heal(CITY, VIRUS, QUANTITY)[source(AG)] : true <-
    .send(AG, tell, disagreeHeal(CITY, VIRUS, QUANTITY)).
    
// Si le solicitan construir un CI, y el agente solicitante esta más lejos de 2 casillas de uno, acepta.
+!canBuildCI(CITY)[source(AG)]: atCity(AG, CITY) & closestCI(CITY, CLOSEST_CI) & 
                                distance(CITY, CLOSEST_CI, DISTANCE) & DISTANCE > 2 <- 
                                .send(AG, tell, agreeBuildCI(CITY));
                                +needCI(CITY);
                                !buildCI(CITY);
                                +middlePlan(BUILD_CI).
                                
// Si le solicitan construir un C, y el agente solicitante esta a menos de 2 casillas de uno, rechaza.
+!canBuildCI(CITY)[source(AG)]: true <- .send(AG, tell, disagreeBuildCI(CITY)).

// Si le solicitan una carta que no tiene o alguien ya acepto deniega esta
+!cityCard(CITY)[source(AG)] : not myCard(CITY) <- .send(AG, tell, disagreeCityCard(CITY)).
+!cityCard(CITY)[source(AG)]: willShareInfo(CITY, AG) <- .send(AG, tell, disagreeCityCard(CITY)).
// En cualquier otro caso, acepta entregarla
+!cityCard(CITY)[source(AG)] : true <- .send(AG, tell, agreeCityCard(CITY));
    !shareInfo(CITY, AG);
    +middlePlan(SHARE_INFO);
    .broadcast(tell, willShareInfo(CITY, AG));.
        
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
+turn : middlePlan(PLAN) <- .print("New turn").
+turn : true <- .print("New turn");
                !checkWorstCity.

// Game over belief. Drops all intentions
+gameover : true <- .print("I lost, so I'm dropping all intentions..."); .drop_all_intentions.

+turn: true <- passTurn.
