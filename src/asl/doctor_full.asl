/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
                             
// Si tiene que descartarse una carta se descarta la primera que encuentra a no ser que tenga que usarla para curar una enfermedad
+cardMustBeenDiscarded : hasCard(doctor, CITY) <- 
                for(hasCard(doctor, CITY)){
                    if(not willHeal(CITY, DIS, QUANTITY)){
                        discardCard(CITY, doctor);
                    }
                }
                -cardMustBeenDiscarded.

// Si tiene 5 cartas de una enfermedad y no se ha descubierto cura descubrir cura o ir al CI mas cercano para curarla
+myCardsNumber(DISEASE, X): X >= 5 & findCIToReach(CITY) & turn <- findCIToReach(DISEASE).
+irACIE(CITY) : true 
            <- 
            .send(analist,tell,willDiscoverCure(DISEASE)); 
            .send(op_expert,tell,willDiscoverCure(DISEASE)); 
            .send(doctor,tell,willDiscoverCure(DISEASE)); 
            !getCure(CITY,DISEASE).



// Estoy en el centro de investigación, así que descubro la cura
+!getCure(CITY,DISEASE) : turn & 
                        myCity(CITY) & 
                        irACIE(CITY) &
                        not isCured(DISEASE)
                    <- 
                    discoverCure(DISEASE); 
                    -irACIE(CITY); 
                    .send(analist,untell,willDiscoverCure(DISEASE)); 
                    .send(op_expert,untell,willDiscoverCure(DISEASE)); 
                    .send(doctor,untell,willDiscoverCure(DISEASE)).
    
// No estoy en el centro de investigación, así que me acerco a él                    
+!getCure(CITY,DISEASE) : not atCity(genetist,CITY) & 
                        turn &
                        myCardsNumber(VIRUS, X) & 
                        X >= 5
                        <- moveToFarObjective(CITY); 
                        !getCure(CITY,DISEASE).

// Si falta una carta para descubrir una cura
+!getCure(DISEASE, CITY): 
            myCardsNumber(VIRUS, X) &
            X == 4 
            <- 
            for(hasCard(analist, CITY)){
                if(infected(CITY, DIS, QUANTITY) &
                    DIS == DISEASE){
                    .send(analist, askOne, cityCard(CITY));
                    +flag;
                }
            }
            
            for(hasCard(genetist, CITY)){
                if(flag){
                    -flag;
                }
                elif(infected(CITY, DIS, QUANTITY) &
                    DIS == DISEASE){
                    .send(genetist, askOne, cityCard(CITY));
                }
            }
            
            for(hasCard(op_expert, CITY)){
                if(flag){
                    -flag;
                }
                elif(infected(CITY, DIS, QUANTITY) &
                    DIS == DISEASE){
                    .send(op_expert, askOne, cityCard(CITY));
                }
            }
            
            if(flag){
                -flag;
                -getCure(DISEASE, CITY);
            }
            !getCure(DISEASE, CITY).


// Si le piden curar una ciudad y no esta yendo a curar una ya va a curarla y se lo dice a los demas
+!heal(CITY, VIRUS, QUANTITY) : not willHeal(_, _, _) <-
    +willHeal(CITY, VIRUS, QUANTITY);
    .send(genetist, tell, willHeal(CITY, VIRUS, QUANTITY));
    .send(analist, tell, willHeal(CITY, VIRUS, QUANTITY));
    .send(op_expert, tell, willHeal(CITY, VIRUS, QUANTITY)).

// Si le piden curar una enfermedad y esta curando otra pero de menos cubos deja la anterior y va a esta
+!heal(CITY, VIRUS, QUANTITY) : willHeal(_, VIRUS2, QUANTITY2) & QUANTITY2 < QUANTITY <-
    -willHeal(CITY, VIRUS2, QUANTITY2);
    +willHeal(CITY, VIRUS, QUANTITY);
    .send(genetist, tell, willHeal(CITY, VIRUS, QUANTITY));
    .send(analist, tell, willHeal(CITY, VIRUS, QUANTITY));
    .send(op_expert, tell, willHeal(CITY, VIRUS, QUANTITY)).
    
 // En cualquier otro caso si le piden curar una enfermedad la rechaza
 +!heal(CITY, VIRUS, QUANTITY)[source(AG)] : willHeal(_, _, _) <- -heal(CITY, VIRUS, QUANTITY);
    .send(AG, tell, disagreeHeal(CITY, VIRUS, QUANTITY)).
    
// comunicar a todos cuando deja de perseguir virus
-willHeal(CITY, VIRUS, QUANTITY) : true <- .send(genetist, untell, willHeal(CITY, VIRUS, QUANTITY));
    .send(analist, untell, willHeal(CITY, VIRUS, QUANTITY));
    .send(op_expert, untell, willHeal(CITY, VIRUS, QUANTITY));.    

// Si tiene que ir a curar una enfermedad dirigirse hacia la ciudad
+willHeal(CITY, VIRUS, QUANTITY) : turn & not myCity(CITY) & infected(CITY, VIRUS, _) <- moveToFarObjective(CITY,whatever).
// Si esta en una ciudad donde hay cubos y tiene acciones disponibles curar enfermedad
// (si se ha descubierto la cura no habra cubos y simplemente se quita la creencia)
+willHeal(CITY, VIRUS, QUANTITY) : turn & myCity(CITY) <- treatDisease(VIRUS).
// Si tiene la creencia de curar enfermedad y no hay enfermedad eliminarla
+willHeal(CITY, VIRUS, QUANTITY) : not infected(CITY, VIRUS, _) <- -willHeal(CITY, VIRUS, QUANTITY).

// Si le piden una carta entregarla siempre a no ser que la necesite para viajar a la ciudad que esta intentando curar
+!cityCard(CITY)[source(AG)] : myCard(CITY) & not willHeal(CITY, VIRUS, QUANTITY) <- .send(AG, tell, agreeCityCard(CITY));
    +willShareInfo(CITY, AG);
    .send(genetist, tell, willShareInfo(CITY, AG));
    .send(analist, tell, willShareInfo(CITY, AG));
    .send(op_expert, tell, willShareInfo(CITY, AG)).
+!cityCard(CITY)[source(AG)] : myCard(CITY) & not myCity(CITY) & willHeal(CITY, VIRUS, QUANTITY) <- .send(AG, tell, disagreeCityCard(CITY)).
+!cityCard(CITY)[source(AG)] : not myCard(CITY) <- .send(AG, tell, disagreeCityCard(CITY)).

// Si va a compartir carta dirigirse a la ciudad donde compartir (TODO: no debe dar la carta que quiere compartir para desplazarse)
+willShareInfo(CITY, doctor) : true <- -willShareInfo(CITY, doctor). 
+willShareInfo(CITY, AG) : turn & not myCity(CTIY) & at(CITY, VIRUS, X, Y) <- moveToFarObjective(CITY, VIRUS); -willShareInfo(CITY, AG); +willShareInfo(CITY, AG).
+willShareInfo(CITY, AG) : turn & myCity(CITY) & at(AG, CITY) <- shareInfo(AG, CITY, true); -willShareInfo(CITY, AG); .send(AG, untell, willShareInfo(CITY, AG)).
+willShareInfo(CITY, AG): turn & myCity(CITY) <- passTurn.

// Si no hay planes, busca una ciudad con cura descubierta para tratarla, y si no, busca la ciudad con mayor numero de cubos
+!worstCity :  turn &
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
            
            // Comprobamos si hay cubos en una ciudad con cura descubierta
            for(CITY){
                if(infected(CITY, DIS, QUANTITY) &
                    infectionLVL(CITY, INFECTION_LEVEL) &
                    isCured(DIS) &
                    INFECTION_LEVEL > 0 &
                    DIS \== null){
                    +willHeal(CITY, DIS, QUANTITY);
                    .send(genetist, tell, willHeal(CITY, VIRUS, QUANTITY));
                    .send(analist, tell, willHeal(CITY, VIRUS, QUANTITY));
                    .send(op_expert, tell, willHeal(CITY, VIRUS, QUANTITY));
                    +flag;
                }
            }
            
            
            if(flag){
                -flag;
            }
            else{
                // Encuentra la ciudad con mayor numero de cubos
                for(CITY){
                    if(infectionLVL(CITY, INFECTION_LEVEL) &
                        wander_worst(_, CURR_CITY_INFECTION_LVL) &
                        INFECTION_LEVEL > CURR_CITY_INFECTION_LVL){ 
                        -wander_worst(_,_);
                        +wander_worst(CITY, INFECTION_LEVEL);
                    }
                }
                
                // Si ya estamos en la ciudad
                if(wander_worst(CITY, _) &
                    myCity(CITY) &
                    infected(CITY, DIS, QUANTITY) &
                    DIS \== null){
                    +willHeal(CITY, DIS, QUANTITY);
                    .send(genetist, tell, willHeal(CITY, VIRUS, QUANTITY));
                    .send(analist, tell, willHeal(CITY, VIRUS, QUANTITY));
                    .send(op_expert, tell, willHeal(CITY, VIRUS, QUANTITY));
                    //treatDisease(DIS);
                }
                else{
                    // Si tenemos la carta para viajar
                    if(hasCard(doctor, CITY)){
                        directFlight(CITY);
                    }
                    else{
                        if(hasCard(op_expert, CITY)){
                            .send(op_expert, askOne, cityCard(CITY));
                        }
                        
                        elif(hasCard(genetist, CITY)){
                            .send(genetist, askOne, cityCard(CITY));
                        }
                        
                        elif(hasCard(analist, CITY)){
                            .send(analist, askOne, cityCard(CITY));
                        }
                        else{
                            +willHeal(CITY, DIS, QUANTITY);
                            .send(genetist, tell, willHeal(CITY, VIRUS, QUANTITY));
                            .send(analist, tell, willHeal(CITY, VIRUS, QUANTITY));
                            .send(op_expert, tell, willHeal(CITY, VIRUS, QUANTITY));
                            //moveToFarObjective(CITY, DIS);
                        }
                    }
                }
            }
            !worstCity.

+disagreeCityCard(CITY)[source(AG)]: turn &
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
            !worstCity. 

+left_actions(N) : N > 0 & myCardsNumber(VIRUS, X) <- passTurn.
+turn: willShareInfo(CITY, AG) <- -willShareInfo(CITY, AG); +willShareInfo(CITY, AG).
+turn : true <- passTurn.
