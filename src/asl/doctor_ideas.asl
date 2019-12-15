/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

									
// Si tiene que descartarse una carta se descarta la primera que encuentra (TODO: mejorar esto)
+cardMustBeenDiscarded : myCard(CARD) <- discardCard(CARD, doctor); -cardMustBeenDiscarded.


// Si tiene 5 cartas de una enfermedad y no se ha descubierto cura descubrir cura o ir al CI mas cercano para curarla
+myCardsNumber(DISEASE, X): X >= 5 & findCIToReach(CITY) <- !getCure(DISEASE,CITY).
+!getCure(DISEASE, CITY): turn & myCity(CITY) & myCardsNumber(VIRUS, X) & X >= 5 <- discoverCure(DISEASE).
+!getCure(DISEASE, CITY): turn & myCardsNumber(VIRUS, X) & X >= 5 <- moveToFarObjective(CITY); !getCure(DISEASE, CITY).
// Si ha dejado de tener 5 cartas solicitar carta (TODO: encontrar a quien solicitarla)
+!getCure(DISEASE, CITY): myCardsNumber(VIRUS, X) & X < 5 <- true.



// Si le piden curar una ciudad y no esta yendo a curar una ya va a curarla y se lo dice a los demas
+!heal(CITY, VIRUS, QUANTITY) : not willHeal(CITY, VIRUS, QUANTITY) <-
    +willHeal(CITY, VIRUS, QUANTITY);
    .send(genetist, tell, willHeal(CITY, VIRUS, QUANTITY));
    .send(analist, tell, willHeal(CITY, VIRUS, QUANTITY));
    .send(op_expert, tell, willHeal(CITY, VIRUS, QUANTITY)).

// Si le piden curar una enfermedad y esta curando otra pero de menos cubos deja la anterior y va a esta
+!heal(CITY, VIRUS, QUANTITY) : willHeal(CITY, VIRUS2, QUANTITY2) & QUANTITY2 < QUANTITY <- 
    -willHeal(CITY, VIRUS2, QUANTITY2);
    +willHeal(CITY, VIRUS, QUANTITY);
    .send(genetist, tell, willHeal(CITY, VIRUS, QUANTITY));
    .send(analist, tell, willHeal(CITY, VIRUS, QUANTITY));
    .send(op_expert, tell, willHeal(CITY, VIRUS, QUANTITY)).
    
 // En cualquier otro caso si le piden curar una enfermedad la rechaza
 +!heal(CITY, VIRUS, QUANTITY)[source(AG)] : willHeal(CITY, VIRUS, QUANTITY) <- -heal(CITY, VIRUS, QUANTITY);
    .send(AG, tell, disagreeHeal(CITY, VIRUS, QUANTITY)).
    
// comunicar a todos cuando deja de perseguir virus
-willHeal(CITY, VIRUS2, QUANTITY2) : true <- .send(genetist, untell, willHeal(CITY, VIRUS, QUANTITY));
    .send(analist, untell, willHeal(CITY, VIRUS, QUANTITY));
    .send(op_expert, untell, willHeal(CITY, VIRUS, QUANTITY));.    

// Si tiene que ir a curar una enfermedad dirigirse hacia la ciudad
+willHeal(CITY, VIRUS, QUANTITY) : turn & not myCity(CITY) & infected(CITY, VIRUS, _) <- moveToFarObjective(CITY,VIRUS).
// Si esta en una ciudad donde hay cubos y tiene acciones disponibles curar enfermedad
// (si se ha descubierto la cura no habra cubos y simplemente se quita la creencia)
+willHeal(CITY, VIRUS, QUANTITY) : turn & myCity(CITY) <- treatDisease(VIRUS).
// Si tiene la creencia de curar enfermedad y no hay enfermedad eliminarla
+willHeal(CITY, VIRUS, QUANTITY) : not infected(CITY, VIRUS, _) <- -willHeal(CITY, VIRUS, QUANTITY).

// Si le piden una carta entregarla siempre a no ser que la necesite para viajar a la ciudad que esta intentando curar
+!cityCard(CITY)[source(AG)] : not willHeal(CITY, VIRUS, QUANTITY) <- .send(AG, tell, agreeCityCard(CITY));
    +willShareInfo(CITY, AG);
    .send(genetist, tell, willShareInfo(CITY, AG));
    .send(analist, tell, willShareInfo(CITY, AG));
    .send(op_expert, tell, willShareInfo(CITY, AG)).
+!cityCard(CITY)[source(AG)] : willHeal(CITY, VIRUS, QUANTITY) <- .send(AG, tell, disagreeCityCard(CITY)).

// Si va a compartir carta dirigirse a la ciudad donde compartir (TODO: no debe dar la carta que quiere compartir para desplazarse)
+willShareInfo(CITY, AG) : turn <- moveToFarObjective(CITY,VIRUS).


// Si no tiene ninguna accion pendiente y tiene acciones disponibles buscar la ciudad mas cercana con cubos e ir hacia alli (TODO)


// TODO: Dar prioridad a las ciudades con cura descubierta porque se curan sin gastar acciones

// TODO: Dar prioridad a las ciudades que tienen mas cubos porque con una unica accion cura todos

// TODO: Evaluar cuando debe solicitar una carta para viajar a una ciudad (si esta al borde del desbordamiento)

// TODO: Evaluar cuando debe solicitar carta para descubrir cura (cuando solo le queda una) (TODO: encontrar a quien solicitarla)
+left_actions(N) : N > 0 & myCardsNumber(VIRUS, X) <- true.
