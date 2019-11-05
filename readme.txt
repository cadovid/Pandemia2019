*Consideraciones generales:
> Los únicos archivos que puede interesar modificar están en las carpetas {board, card, city, dis, game y player}.
> Cada .java relevante contiene comentarios donde se explica su función y rutinas.
> Todos los comentarios y códigos precedidos por "GRA" están asociados a la parte gráfica (se pueden ignorar...).
> La carpeta _initdata contiene los archivos de inicialización donde se definen las opciones del juego.
> Es recomendable definir las variables adaptables al juego en el archivo _aux/Options.java o _aux/CustomTypes.java (según proceda), y acceder a ellas desde ahí (para no volvernos locos con valores comodín).
> Fragmentos marcados como "TO-DO" son rutinas o funcionalidades que están pendientes de implementar o completar. Actos de voluntad serán bien recibidos.
> La parte gráfica es opcional a rabiar, pero es y será muy útil para ver el estado general del juego en cada iteración (es posible mostrar los mensajes gestionados por los agentes en tiempo real). Procuraré adaptarla a lo que vayan haciendo los grupos (a menos que surja un voluntario), lo que es más una carta de reyes magos que una promesa... Cervezas, alcoholes de alta graduación y/o cafés gratis pueden incentivar un comportamiento eficaz y eficiente en esta -y otras- labores.


################################
*Ejecución del juego
################################

>Desde consola:
>> java game.Game

>Desde Eclipse:
>> Importando la carpeta completa y ejecutando la clase Game (puede no funcionar, no lo he podido probar).

>Compilar desde consola; necesario si se MODIFICA algún .java (en Eclipse, flores):
>> javac -encoding utf-8 @java_sources.txt

>Actualizar paquetes desde consola; necesario si se AÑADE o ELIMINA algún .java del proyecto (en Eclipse, flores):
>> ./list_javas (luego se debe recompilar ^^^)


################################
*Generación de juegos customizados (archivos en _initdata)
################################

>Modificar los archivos csv correspondientes de la carpeta _initdata:
(<> -> obligatorio; [] -> opcional)
>>players.csv
>>> Formato: <alias>;<alias del rol>;<alias de la ciudad de partida>

>>roles.csv
>>> Formato: <nombre completo>;<alias>

>>cities.csv
>>> Formato: <nombre completo>;<alias>;<alias de la enfermedad local>

>>dis.csv (enfermedades)
>>> Formato: <nombre completo>;<alias>;[máximo número de infecciones]

>>map.csv
>>> Formato:
>>>> Primera línea: <num filas>;<num columnas>
>>>> Subsiguientes: <0/alias de ciudad> - Tantas entradas por línea como columnas (son las celdas del mapa); tantas líneas como filas.

>>> Es posible cambiar los sprites desde la carpeta graphics/sprites/cellbg. grass.jpg y water.jpg son los gráficos genéricos para todas las casillas, pero si se incluye una imagen de nombre <alias ciudad>.jpg, el juego mostrará ese gráfico en la ciudad a la que corresponda el alias.


################################
*Cambiar el nivel de verbosidad de la consola (el output al compilar)
################################

Editar el archivo _aux/Options.java. Modificar la variable LOG y asignarle un valor LogLevel válido (ver tipo enum LogLevel en _aux/CustomTypes).

De modificar el código base, sería ideal continuar poniendo comentarios adaptables; ej (la siguiente línea sólo se imprime si se ha habilitado el log tipo INFO o superior en su jerarquía (DUMP y ALL; ver CustomTypes)):
>if(Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
>>System.out.printf("Mensaje informativo");
