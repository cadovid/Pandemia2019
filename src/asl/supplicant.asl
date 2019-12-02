// Agent supplicant in project pandemic

/* Initial beliefs and rules */


/* Initial goals */
!persistent.

//!doRound.

/* Plans */

				
+!checkPlayers : .count(player(_), 2) & not playersReady <- .print("All players ready!!"); +playersReady.
+!checkPlayers : not .count(player(_), 2) <- .print("Some players are waiting").

// Recursive percept
/*
+!persistent : true <- .print("Doing round...");
						!persistent.
*/

+init : true <- .print("Starting, b8");
				.broadcast(tell, ack);
				.print("Broadcasting acknowledgment to all agents");
				-init.
				
+ack[source(AGENT)] : true <- .print("Received acknowledgment from ", AGENT);
								+player(AGENT);
								!checkPlayers.

//+!doRound <- .print(round); !doRound.

/*
+!start : true <- .print("Supplicant fired.").
+!start : true <- init.
*/
