// Agent mam in project pandemic_local

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// Si tiene que descartarse una carta se descarta la primera que encuentra (TODO: mejorar esto)
+cardMustBeenDiscarded: myHandSize(SIZE) & SIZE <= 7 & myCard(CARD) <- discardCard(CARD, analist); -cardMustBeenDiscarded.
+cardMustBeenDiscarded: myHandSize(SIZE) & SIZE > 7 & myCard(CARD) <- discardCard(CARD, analist); +cardMustBeenDiscarded.

//Vamos a descubrir la cura
+!getCure(DISEASE): turn & myCity(CURRENT_CITY) & hasCI(CURRENT_CITY) <- .print("Cure has been discovered"); discoverCure(DISEASE); -middlePlan("GET_CURE"); passTurn.
+!getCure(DISEASE): turn & myCity(CURRENT_CITY) & closestCI(CURRENT_CITY, CLOSEST_CI) <- .print("Trying to reach closest CI to discover cure");
                    moveToFarObjective(CLOSEST_CI, DISEASE); !getCure(DISEASE); +middlePlan("GET_CURE").
+!getCure(DISEASE): not turn <- !getCure(DISEASE); +middlePlan("GET_CURE").

//Compartimos Informacion
+!shareInfo(CITY, AG): turn & myCity(CITY) &  atCity(AG, CITY)<- shareInfo(AG, CITY, true);
                     -middlePlan("SHARE_INFO"); -willShareInfo(CITY, AG); 
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
                            .print("OPCION A");
                        }
                        
                    }else{
                    	.print("OPCION B");
                    	!noRiskPlan;
                    }
                    ;
                    .print("CHECKING WORST CITY");.
                    
+!checkWorstCity: not turn <- !checkWorstCity.

//Si no hay ciudades en riesgo de brote, miramos si se necesita un CI
+!noRiskPlan: turn <- .print("NO RISK IN THE CITY"); !checkIfNeedCI; +middlePlan("BUILD_CI").
+!noRiskPlan: not turn <- !noRiskPlan.

//Si hay ciudades en riesgo de brote, vamos pa tratarla
+!goToHeal(CITY, DISTANCE, DIS): turn & myCity(CITY) <- treatDisease(DIS); -worstCity(CITY, DISTANCE, DIS);                                 
.broadcast(untell, willHeal(CITY, DIS, 1)); -middlePlan("GO_TO_CURE"); -willHeal(CITY, DIS, 1); passTurn.
+!goToHeal(CITY, DISTANCE, DIS): turn & not myCity(CITY) <- moveToFarObjective(CITY, DIS); !goToHeal(CITY, DISTANCE, DIS).
+!goToHeal(CITY, DISTANCE, DIS): not turn <- !goToHeal(CITY, DISTANCE, DIS).

//Planes de construir un CI
+!buildCI(CITY): turn & not myCity(CITY) & needCI(CITY) <- moveToFarObjective(CITY, DIS); !buildCI(CITY).
+!buildCI(CITY): turn & myCity(CITY) & needCI(CITY) <- buildCI; .print("Building CI in current city");
                                                     -middlePlan("BUILD_CI"); -needCI(CITY); .broadcast(untell, agreeBuildCI(CITY)); passTurn.
+!buildCI(CITY): turn & myCity(City) & not needCI(CITY) <- -middlePlan("BUILD_CI"); passTurn.
+!buildCI(CITY): not turn <- !buildCI(CITY).
+!checkIfNeedCI: not turn <- !checkIfNeedCI.
+!checkIfNeedCI: turn & myCity(CITY) <- isCIreachable(4); !buildCI(CITY).

//Si tenemos 5 cartas, vamos a descubrir la cura
+myCardsNumber(DISEASE,5): not isCured(DISEASE) <- !getCure(DISEASE).

// Si le solicitan curar una ciudad y no esta yendo a curar otra, acepta
+!heal(CITY, VIRUS, QUANTITY) : not willHeal(_, _, _) & myCity(CURRENT_CITY) & distance(CURRENT_CITY, CITY, DISTANCE) <-
    !goToHeal(CITY, DISTANCE, VIRUS);
    +willHeal(CITY, VIRUS, QUANTITY);
    +middlePlan("GO_TO_CURE");
    .print("ACEPTO LA SOLICITUD DE IR A CURAR");
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
                                +middlePlan("BUILD_CI").
                                
// Si le solicitan construir un C, y el agente solicitante esta a menos de 2 casillas de uno, rechaza.
+!canBuildCI(CITY)[source(AG)]: true <- .send(AG, tell, disagreeBuildCI(CITY)).

// Si le solicitan una carta que no tiene o alguien ya acepto deniega esta
+!cityCard(CITY)[source(AG)] : not myCard(CITY) <- .send(AG, tell, disagreeCityCard(CITY)).
+!cityCard(CITY)[source(AG)]: willShareInfo(CITY, AG) <- .send(AG, tell, disagreeCityCard(CITY)).
// En cualquier otro caso, acepta entregarla
+!cityCard(CITY)[source(AG)] : true <- .send(AG, tell, agreeCityCard(CITY));
    !shareInfo(CITY, AG);
    +middlePlan("SHARE_INFO");
    .broadcast(tell, willShareInfo(CITY, AG)).
        
// Whenever a player starts its turn, the number of actions left is updated
//+turn : middlePlan(PLAN) <- .print("New turn").
+turn : true <- .print("New turn");
                !checkWorstCity.

// Game over belief. Drops all intentions
+gameover : true <- .print("I lost, so I'm dropping all intentions..."); .drop_all_intentions.

+turn: true <- passTurn.
