package graphics;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import game.Game;
import _aux.CustomTypes;

// Game controls
public class Control extends graphics.Box {
	public Control(int w, int h, Game g) {
		super(w, h, new GridLayout(1, 2, 0, 0));
		JButton turn = new JButton("Run turn");
		JButton timestamp = new JButton("Autoplay");

		// Button actions (triggers a flag in the game object)
		turn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (g.gm != CustomTypes.GameMode.TURN) {
					g.gm = CustomTypes.GameMode.TURN;
					System.out.println("[Control] - Game mode switched to \"TURN\"");
				}

				g.runTurn = true;
			}
		});

		timestamp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (g.gm != CustomTypes.GameMode.TIMESTAMP) {
					g.gm = CustomTypes.GameMode.TIMESTAMP;
					System.out.println("[Control] - Game mode switched to \"TIMESTAMP\"");
				}
			}
		});

		this.add(turn);
		this.add(timestamp);
	}
}
