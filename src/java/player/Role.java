package player;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import _aux.Options;
import _aux.CustomTypes;

/*
  Role class
    Base mehtod for a role definition.
    Each role should use this as a superclass.
*/
public class Role {
	public String name;
	public String alias;
	private Player player = null;
	private boolean assigned = false;

	// Constructor
	public Role(String name, String alias) {
		this.name = name;
		this.alias = alias;
	}

	/*
	 * parseRoles Initializes roles from a datafile. Expected format (csv): <Role
	 * full name>;<alias>
	 * 
	 * Alias must be unique. It'll be used as identifier.
	 ** 
	 * PARAMETERS: datafile: String; path to file.
	 */
	public static Hashtable<String, Role> parseRoles(String datafile) {
		Hashtable<String, Role> roles = new Hashtable<String, Role>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(datafile));
			String line;
			String[] role_data;
			String role_name;
			String role_alias;

			// Reads roles data from subsequent lines (Must follow the expected disease
			// format)
			while ((line = br.readLine()) != null) {
				role_data = line.split(";");

				// Gets data from splitted line elements
				role_name = role_data[0];
				role_alias = role_data[1];

				// Ideally, here must be resolved the specific role subclass by checking the
				// role alias.
				/*
				 * Role prol; if(role_alias.equals(sci)){ prol = new Role_Sci(); ... }
				 */

				// Performs redundancy check, if possible
				if (roles.containsKey(role_alias)) {
					if (Options.LOG.ordinal() >= CustomTypes.LogLevel.WARN.ordinal())
						System.out.printf("[Role] WARN: Role \"%s\" duplicated. Ignoring...\n");

					continue;
				}

				// Creates Role object and adds it to the dictionary
				else {
					Role role = new Role(role_name, role_alias);
					roles.put(role_alias, role);

					// Dummy dump
					if (Options.LOG.ordinal() >= CustomTypes.LogLevel.INFO.ordinal())
						System.out.printf("[Role] INFO: New role generated\n");

					if (Options.LOG.ordinal() >= CustomTypes.LogLevel.DUMP.ordinal())
						role.dump();
				}
			}
			br.close();
		} catch (Exception e) {
			if (Options.LOG.ordinal() >= CustomTypes.LogLevel.CRITICAL.ordinal())
				System.out.printf("[Role] CRITICAL: Exception while parsing\n");

			System.err.println(e.getMessage());
			System.exit(0);
		}

		// Binds game roles
		return roles;
	}

	// Locks a role to a player
	public void bindPlayer(Player p) {
		this.player = p;
		this.assigned = true;
	}

	// Dummy method to print role data
	public void dump() {
		System.out.printf(">>Printing role data (%s, alias: %s)\n", this.name, this.alias);

		if (player != null) {
			System.out.printf(".Player: %s\n", this.player.alias);
		}
		System.out.printf(".Is assigned: %b\n", this.assigned);

		System.out.println();
	}
}
