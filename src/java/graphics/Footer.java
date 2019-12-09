package graphics;

import java.awt.Color;
import javax.swing.*;

// Header data
import java.util.*;

public class Footer extends graphics.Box {

	// Constructors
	public Footer(int width, int height) {
		super(width, height);

		this.add(new JLabel("[Footer] - Should show current turn data..."));
		this.setBackground(Color.gray);
	}
}
