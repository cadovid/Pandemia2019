// Agent p1 in project pandemic

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// Informs ready state to supplicant. Also adds ready state to its belief base
+ack[source(supplicant)] : true <- .send(supplicant, tell, ack);
									+ready.
