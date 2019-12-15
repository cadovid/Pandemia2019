//Objetivo inicial: 
!encontrarVirusPerseguir.


+!perseguirVirusObjetivo(VIRUS): cardMustBeenDiscarded & myCard(CITY) & not at(CITY,VIRUS,X,Y) <- discardCard(CITY,genetist); !perseguirVirusObjetivo(VIRUS).


//EN CUALQUIER TURNO ==> Si no tengo el objetivo de perseguir ningún virus, elijo qué virus perseguir dependiendo de cuál es el color que más se repite en mi mano y que no haya sido ya descubierta y añado el objetivo de perseguir ese virus. 
+!encontrarVirusPerseguir: myCardsNumber(VIRUS,N) & not isCured(VIRUS) <- +creoQuePersigo(VIRUS,N); +comprobado(VIRUS); !encontrarVirusPerseguir2.
+!encontrarVirusPerseguir2: creoQuePersigo(VIRUS,N) & myCardsNumber(VIRUS2,N2) & not isCured(VIRUS2) & not comprobado(VIRUS2)  & (N<N2) <- -creoQuePersigo(VIRUS,N); +creoQuePersigo(VIRUS2,N2); +comprobado(VIRUS2); !encontrarVirusPerseguir2.
+!encontrarVirusPerseguir2: isCured(VIRUS) & not comprobado(VIRUS) <- +comprobado(VIRUS); !encontrarVirusPerseguir2.
+!encontrarVirusPerseguir2: creoQuePersigo(VIRUS,N) & myCardsNumber(VIRUS2,N2) & not isCured(VIRUS2) & not comprobado(VIRUS2) &  (N>=N2) <- +comprobado(VIRUS2); !encontrarVirusPerseguir2.
+!encontrarVirusPerseguir2: comprobado(ban) & comprobado(dia) & comprobado(ebo) & comprobado(lup) & creoQuePersigo(VIRUS,N) <- -creoQuePersigo(VIRUS,N); +perseguirVirus(VIRUS); -comprobado(ban); -comprobado(dia); -comprobado(ebo); -comprobado(lup); !perseguirVirusObjetivo(VIRUS).
	
	

//EN MI TURNO ==> Si tengo el objetivo de perseguir un virus, no tengo suficientes cartas para descubrir la cura y nadie ha decidido darme una, detecto al más cercano (b) y le pido que me dé su carta. 
+!perseguirVirusObjetivo(VIRUS) : not cardMustBeenDiscarded & myCardsNumber(VIRUS,X) & (X<4) & turn <- .print("persigo ", VIRUS); findPlayerToAsk(VIRUS).
+soliciteCardE(AGENTE,CIUDAD) : true <-  +debesPreguntar(AGENTE,CIUDAD); .send(AGENTE,achieve,cityCard(CIUDAD)).
+!perseguirVirusObjetivo(VIRUS) : not cardMustBeenDiscarded & not turn <- +ibaPor(perseguirVirusObjetivo).
+turn : ibaPor(perseguirVirusObjetivo) & perseguirVirus(VIRUS) <- -ibaPor(perseguirVirusObjetivo); !perseguirVirusObjetivo(VIRUS).


//Si no me la quiere dar, pido al siguiente más cercano. 
+disagreeCityCard(CITY)[source(AG)] : debesPreguntar(AG,CITY) <- -debesPreguntar(AG,CITY); findPlayerToAsk(VIRUS).
	
	
	
//Si me la quiere dar, añado el objetivo de ir a por esa carta y declaro la intención de intercambiar carta. 
+agreeCityCard(CITY)[source(AG)] : debesPreguntar(AG,CITY) <- -debesPreguntar(AG,CITY); +tratoIntercambioCon(AG,CITY); .send(analist,tell,willShareInfo(CITY,AG)); .send(doctor,tell,willShareInfo(CITY,AG)); .send(op_expert,tell,willShareInfo(CITY,AG)); !irACiudad(CITY).



//Si nadie me quiere dar nada, añado el objetivo de quitar cubitos, calculando dónde están los cubitos más cercanos. 
//+nobodySharesE : true <- .print("nadieQuiereDarmeCarta..."); passTurn; !perseguirVirusObjetivo(VIRUS).
+nobodySharesE : true <- findNearestCube.
+cityNearestCubeE(CITY) : true <- !irAQuitarCubos(CITY).



//EN MI TURNO ==> Si tengo el objetivo de ir a un sitio para que me den una carta, calculo una buena ruta que no desperdicie una carta del color que persigo (a) y me muevo hacia allí dependiendo del objetivo que me devuelva la ruta. 
+!irACiudad(CITY) : turn & perseguirVirus(VIRUS) & not atCity(genetist,CITY) <- moveToFarObjective(CITY,VIRUS) ; !irACiudad(CITY).

+!irACiudad(CITY) : not turn <- +ibaPor(irACiudad,CITY).
+turn : ibaPor(irACiudad,CITY) <- -ibaPor(irACiudad,CITY); !irACiudad(CITY).



//EN MI TURNO ==> Si estoy en el destino junto con el otro agente, que me dé su carta, quito objetivo de intercambiar carta (intercambiar es una acción) y hago untell de la intención. 
+!irACiudad(CITY) : turn & atCity(genetist,CITY) & tratoIntercambioCon(AG,CITY) & at_AG(AG,CITY) <- -tratoIntercambioCon(AG,CITY); .send(analist,untell,willShareInfo(CITY,AG)); .send(op_expert,untell,willShareInfo(CITY,AG)); .send(doctor,untell,willShareInfo(CITY,AG)); shareInfo(AG,CITY,genetist); -perseguirVirus(VIRUS); !encontrarVirusPerseguir.



//EN MI TURNO ==> Si estoy en el destino, sigo teniendo el objetivo de intercambiar carta, no está el otro agente y hay cubos en mi posición, quito cubos y declaro intención de quitar cubos en mi posición y después hago untell cuando quite el cubo (en la misma sentencia). 
+!irACiudad(CITY) : turn & atCity(genetist,CITY) & tratoIntercambioCon(AG,CITY) & not atCity(AG,CITY) & infected(CITY,VIRUS,N) & (N>=1) <-treatDisease(VIRUS); !irACiudad(CITY).



//EN MI TURNO ==> Si estoy en el destino, sigo teniendo el objetivo de intercambiar carta, no está el otro agente, no hay cubos en mi posición, hay cubos en una posición adyacente y me quedan 3 o 4 acciones, añado objetivo de quitar cubos en esa posición. 
//EN MI TURNO ==> Si tengo el objetivo de intercambiar carta y de quitar cubos, no estoy en el destino y me queda una acción, elimino el objetivo de quitar cubos en esa posición. 
+!irACiudad(CITY) : turn & atCity(genetistCITY) & tratoIntercambioCon(AG,CITY) & not atCity(AG,CITY) & infected(CITY,VIRUS,N) & (N=0) & adjacent(CITY,CITY2) & left_actions(4) & infected(CITY2,VIRUS2,Qtd2) & (Qtd2 >1) <- moveToNearObjective(CITY2); treatDisease(VIRUS2);treatDisease(VIRUS2);moveToNearObjective(CITY);!irACiudad(CITY).
+!irACiudad(CITY) : turn & atCity(genetistCITY) & tratoIntercambioCon(AG,CITY) & not atCity(AG,CITY) & infected(CITY,VIRUS,N) & (N=0) & adjacent(CITY,CITY2) & left_actions(4) & infected(CITY2,VIRUS2,1) & infected(CITY2,VIRUS3,1) & not (VIRUS2=VIRUS3) <- moveToNearObjective(CITY2); treatDisease(VIRUS2);treatDisease(VIRUS3);moveToNearObjective(CITY);!irACiudad(CITY).

+!irACiudad(CITY) : turn & atCity(genetistCITY) & tratoIntercambioCon(AG,CITY) & not atCity(AG,CITY) & infected(CITY,VIRUS,N) & (N=0) & adjacent(CITY,CITY2) & left_actions(numActions) & (numActions>=3) & infectionLVL(CITY2,1) & infected(CITY2,VIRUS2,1) <- moveToNearObjective(CITY2); treatDisease(VIRUS1);moveToNearObjective(CITY);!irACiudad(CITY).
+!irACiudad(CITY) : turn & atCity(genetistCITY) & tratoIntercambioCon(AG,CITY) & not atCity(AG,CITY) & infected(CITY,VIRUS,N) & (N=0) & adjacent(CITY,CITY2) & left_actions(numActions) & (numActions>=3) & infectionLVL(CITY2,0) <- passTurn; !irACiudad(CITY).
+!irACiudad(CITY) : turn & atCity(genetistCITY) & tratoIntercambioCon(AG,CITY) & not atCity(AG,CITY) & infected(CITY,VIRUS,N) & (N=0) & left_actions(numActions) & (numActions<3) <- passTurn; !irACiudad(CITY).




//EN CUALQUIER TURNO ==> Si tengo el objetivo de perseguir un virus, tengo suficientes cartas y no tengo el objetivo de ir a un centro de investigación, detecto el centro de investigación más cercano (definiendo la cercanía de forma similar que lo comentado en (b)) y añado el objetivo de moverme a ese centro de investigación y la intención de descubrir cura. Si además he declarado intención de intercambiar carta, hago untell de la intención.
+!perseguirVirusObjetivo(VIRUS) : myCardsNumber(VIRUS,X) & X>=4 & turn <- findCIToReach(VIRUS).
+irACIE(CITY) : true <- +irACI(CITY); .send(analist,tell,willDiscoverCure(VIRUS)); .send(op_expert,tell,willDiscoverCure(VIRUS)); .send(doctor,tell,willDiscoverCure(VIRUS)); !irADescubrirCura(CITY,VIRUS).



//EN MI TURNO ==> Si tengo el objetivo de ir a un centro de investigación y no estoy en ese centro de investigación, obtengo el siguiente objetivo cercano (de forma similar a (a)) y me muevo. 
+!irADescubrirCura(CITY,VIRUS) : not atCity(genetist,CITY) & turn <- moveToFarObjective(CITY) ; !irADescubrirCura(CITY,VIRUS).



//EN MI TURNO ==> Si tengo el objetivo de ir a un centro de investigación y estoy en ese centro de investigación, descubro la cura y quito el objetivo de ir al centro de investigación, quito el objetivo de perseguir un virus y hago untell de la intención de descubrir cura. 
+!irADescubrirCura(CITY,VIRUS) : turn & atCity(genetist,CITY) & irACI(CITY) <- discoverCure(VIRUS) ; -irACI(CITY); .send(analist,untell,willDiscoverCure(VIRUS)); .send(op_expert,untell,willDiscoverCure(VIRUS)); .send(doctor,untell,willDiscoverCure(VIRUS)); -perseguirVirus(VIRUS); !encontrarVirusPerseguir.

+!irADescubrirCura(CITY,VIRUS) : not turn <- +ibaPor(irADescubrirCura,CITY,VIRUS).
+turn : ibaPor(irADescubrirCura,CITY,VIRUS) <- -ibaPor(irADescubrirCura,CITY,VIRUS) ; !irADescubrirCura(CITY,VIRUS).


//EN CUALQUIER TURNO ==> Si tengo muchas cartas, descarto aquellas cartas que no son del color que persigo y que no estoy utilizando para moverme (a). 
+cardMustBeenDiscarded :  not turn & perseguirVirus(VIRUS) & myCard(CITY) & not at(CITY,VIRUS,X,Y) <- discardCard(CITY,genetist).



//EN MI TURNO ==> Si tengo el objetivo de quitar cubitos en un sitio y no estoy en ese sitio, me muevo a la casilla adyacente próxima a ese sitio, y si además es mi cuarta acción, quito el objetivo. 
//EN MI TURNO ==> Si tengo el objetivo de quitar cubitos en un sitio, estoy en ese sitio y hay cubitos en ese sitio, declaro intención de quitar cubitos en ese sitio, quito cubito de ese sitio y hago untell de la intención de quitar cubos en ese sitio, y si además es mi cuarta acción, quito el objetivo de quitar cubitos. 
//EN CUALQUIER TURNO ==> Si tengo el objetivo de quitar cubitos en un sitio, estoy en ese sitio y no hay cubitos en ese sitio, quito el objetivo de quitar cubitos en ese sitio. 
+!irAQuitarCubos(CITY) : not turn & perseguirVirus(VIRUS) <- !perseguirVirusObjetivo(VIRUS).
+!irAQuitarCubos(CITY) : turn & not atCity(genetist,CITY) <- moveToNearObjective(CITY); !irAQuitarCubos(CITY).
+!irAQuitarCubos(CITY) : turn & atCity(genetist,CITY) & infected(CITY,VIRUS,N) & (N>0) <- treatDisease(VIRUS); findNearestCube.



//Si me piden carta de ciudad, lo rechazo, porque yo quiero tener cartas y no perder tiempo dándoselas a otros que no las necesitan, en principio, tanto como yo. Que se las pidan a otro. 
+!cityCard(CITY)[source(AG)] : true <- .send(AG,tell,disagreeCityCard(CITY)).



//Si me piden que construya centro de investigación, lo rechazo, porque eso es mejor que lo haga el constructor. 
+!buildCI(CITY)[source(AG)] : true <- .send(AG,tell,disagreebuildCI(CITY)).



//Si me piden que quite cubito, lo rechazo, pues yo decido cuando quitar el cubito. 
+!heal(CITY,VIRUS,Qnt)[source(AG)] : true <- .send(AG,tell,disagreeHeal(CITY,VIRUS)).



//Si me dicen que van a quitar cubos en un sitio, me da igual. 
+willHeal(CITY,VIRUS,Qnt)[source(AG)] : true <- true.



//Si me dicen que ya no van a quitar cubos en un sitio, me da igual. 
-willHeal(CITY,VIRUS,Qnt)[source(AG)] : true <- true.



//Si me dicen que van a construir CI en un sitio, me da igual, pues llamo a la función de las rutas cada vez que me quiero mover a un sitio. Si vuelve a ser mi turno, se vuelven a calcular las rutas. No tengo en cuenta el tiempo que puede tardar en ir y construirlo porque no me fio de que esté lo antes posible, pues puede cambiar de objetivo. 
+willBuildCI(CITY)[source(AG)] : true <- true.



//Si me dicen que ya no van a construir CI en un sitio, me da igual. 
-willBuildCI(CITY)[source(AG)] : true <- true.



//Si me dicen que van a intercambiar carta con otro agente, me da igual. 
+willShareInfo(CITY,AG)[source(AG2)] : true <- true.



//Si me dicen que ya no van a intercambiar carta con un agente y no soy yo, me da igual. 
-willShareInfo(CITY,AG)[source(AG2)] : not (genetist==AG) <- true.



//Si me dicen que ya no van a intercambiar carta conmigo, SI ME IMPORTA (lo indico posteriormente). 
//- 



//Si me dicen que van a ir a descubrir cura y un untell, SI ME IMPORTA (lo indico posteriormente). 
//- 



//1) Que otro agente vaya a descubrir la cura. 
	//1.1) Si el virus no lo persigo, lo doy ya por descubierta (creencia) para no perseguirla en un futuro. 
+willDiscoverCure(VIRUS)[source(AG)] : perseguirVirus(VIRUS2) & not (VIRUS == VIRUS2) <- +isCured(VIRUS).
	
	
	
	//1.2) Si el virus es el que estoy persiguiendo y tengo suficientes cartas, lo ignoro. 
+willDiscoverCure(VIRUS)[source(AG)] : perseguirVirus(VIRUS) & myCardsNumber(VIRUS,X) & (X>=4) <- true.
	
	
	
	//1.3) Si el virus es el que estoy persiguiendo y no tengo suficientes cartas, lo doy por descubierto, y elimino los objetivos de perseguir ese virus e intercambiar la carta (si los hubiera). 
+willDiscoverCure(VIRUS)[source(AG)] : perseguirVirus(VIRUS) & myCardsNumber(VIRUS,X) & (X<4) & not tratoIntercambioCon(AG2,CITY) <- +isCured(VIRUS); -perseguirVirus(VIRUS); !encontrarVirusPerseguir.
+willDiscoverCure(VIRUS)[source(AG)] : perseguirVirus(VIRUS) & numeroCartas(VIRUS,X) & (X<4) & tratoIntercambioCon(AG2,CITY) <- +isCured(VIRUS); -perseguirVirus(VIRUS); .send(analist,untell,willShareInfo(CITY,AG)); .send(op_expert,untell,willShareInfo(CITY,AG)); .send(doctor,untell,willShareInfo(CITY,AG)); !encontrarVirusPerseguir.
	
	
	
//2) Que otro agente haga untell de descubrir la cura y no lo haya descubierto (si lo ha descubierto, ignoro el mensaje) (OJALÁ NO PASE, EL AGENTE QUE LO HACE NOS PUTEARÍA UN POCO, PORQUE SI SE COMPROMETE, SE COMPROMETE Y YA).  
	//2.1) Si el virus no lo persigo, dejo de darla por descubierta (creencia) para perseguirla en un futuro. 
	//2.2) Si el virus es el que estoy persiguiendo y tengo suficientes cartas, lo ignoro. 
-willDiscoverCure(VIRUS)[source(AG)] : perseguirVirus(VIRUS) & irACI(CITY) & isCured(VIRUS) <- -irACI(CITY); .send(analist,untell,willDiscoverCure(VIRUS)); .send(op_expert,untell,willDiscoverCure(VIRUS)); .send(doctor,untell,willDiscoverCure(VIRUS));  -perseguirVirus(VIRUS); !encontrarVirusPerseguir.
-willDiscoverCure(VIRUS)[source(AG)] : perseguirVirus(VIRUS2) & not (VIRUS1 == VIRUS2) <- -isCured(VIRUS).
	
	
	
//3) Que el agente con el que me voy a encontrar para darme una carta me haga untell de intercambiarme la carta y si tengo el objetivo de intercambiar carta con ese agente (en caso de no tener ya ese objetivo, ignoro el mensaje). Si eso pasa, elimino los objetivos de intercambiar carta con ese agente. 
-willShareInfo(CITY,genetist)[source(AG2)] : tratoIntercambioCon(AG2,CITY) & perseguirVirus(VIRUS) <- -tratoIntercambioCon(AG2,CITY); !perseguirVirusObjetivo(VIRUS).
-willShareInfo(CITY,genetist)[source(AG2)] : not tratoIntercambioCon(AG2,CITY) <- true.
	
	
	
//NOTA: si es última acción quito que debo ir al CI más cercano pero sin volver a enviar mensajes, de forma que recalcule de nuevo el CI más cercano cuando sea mi turno. 
//DONE



//NOTA: si quito cubo debo comprobar que quito también creencia de cubo adyacente. 
//DONE



//NOTA: si ninguna regla se puede ejecutar (por ejemplo, si estoy en el destino y no hay cubos qu quitar y no ha llegado el que me tenía que dar la carta), paso turno mediante la función pass y quito creencia de que no tengo cubo cercano si es que tengo esa creencia. 
//DONE















