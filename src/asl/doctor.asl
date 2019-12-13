// Agent p1 in project pandemic

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// Informs ready state to supplicant. Also adds ready state to its belief base
+ack[source(supplicant)] : true <- .send(supplicant, tell, ack);
									+ready.
									
// Si tiene que descartarse una carta se descarta la primera que encuentra (TODO: mejorar esto)
+cardMustBeenDiscarded : hasCard(doctor, CARD) <- discardCard(CARD, doctor); -cardMustBeenDiscarded.

// Si le piden curar una ciudad y no esta yendo a curar una ya va a curarla y se lo dice a los demas
+!heal(city, virus, quantity) : not willHeal(CITY, VIRUS, QUANTITY) <- -heal(city, virus, quantity);
    +willHeal(city, virus, quantity); .send(AG, tell, willHeal(city, virus, quantity)).

// Si le piden curar una enfermedad y esta curando otra pero de menos cubos deja la anterior y va a esta
+!heal(city, virus, quantity) : willHeal(CITY, VIRUS, QUANTITY) & QUANTITY < quantity <- -heal(city, virus, quantity);
    -willHeal(CITY, VIRUS, QUANTITY);
    +willHeal(city, virus, quantity); .send(AG, tell, willHeal(city, virus, quantity)).
    
 // En cualquier otro caso si le piden curar una enfermedad la rechaza
 +!heal(city, virus, quantity)[source(AG)] : willHeal(CITY, VIRUS, QUANTITY) <- -heal(city, virus, quantity);
    .send(AG, tell, disagreeHeal(city, virus, quantity)).
    
// Si tiene que ir a curar una enfermedad dirigirse hacia la ciudad (TODO)
+willHeal(CITY, VIRUS, QUANTITY) : true <- true.

// Si le piden una carta entregarla siempre a no ser que la necesite para viajar a la ciudad que esta intentando curar (TODO)

// Si no tiene ninguna accion pendiente y tiene acciones disponibles buscar la ciudad mas cercana con cubos e ir hacia alli (TODO)

// Si esta en una ciudad donde hay cubos y tiene acciones disponibles curar enfermedad (TODO)

// TODO: Dar prioridad a las ciudades con cura descubierta porque se curan sin gastar acciones

// TODO: Dar prioridad a las ciudades que tienen mas cubos porque con una unica accion cura todos

// TODO: Evaluar cuando debe solicitar una carta para viajar a una ciudad (si esta al borde del desbordamiento)