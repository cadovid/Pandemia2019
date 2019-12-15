package graphics;

import java.awt.*;
import javax.swing.*;
import static javax.swing.ScrollPaneConstants.*;

// Header data
import java.util.*;
import player.Player;
import dis.Disease;

public class Header extends graphics.Box{

  // Constructors
  public Header(int width, int height, int pHeight, int dHeight, Renderer r){
    super(width, height, null);

    this.setLayout(new GridLayout(1, 2, 0, 0));

    // Creates general containers (flow layout)
    JPanel player_pane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    JPanel dis_pane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    player_pane.setPreferredSize(new Dimension(width / 2, r.g.n_players * pHeight));
    player_pane.setMaximumSize(new Dimension(width / 2, r.g.n_players * pHeight));
    dis_pane.setPreferredSize(new Dimension(width / 2, r.g.n_diseases * dHeight));
    dis_pane.setMaximumSize(new Dimension(width / 2, r.g.n_diseases * dHeight));

    // Scrollable header elements
    JScrollPane player_scroll = new JScrollPane();
    JScrollPane dis_scroll = new JScrollPane();

    // RAWR - Dirty fix to remove horizontal scroll!! (Should be done by preventing the overflow in the inner elements...)
    //player_scroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
    //dis_scroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);

    player_scroll.setViewportView(player_pane);
    dis_scroll.setViewportView(dis_pane);

    // Populates player pane
    for(Player p : r.players){
      JPanel player_entry = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      player_entry.setPreferredSize(new Dimension(width / 2, pHeight));
      player_entry.setMaximumSize(new Dimension(width / 2, pHeight));

      // Creates player info items
      // Player color
      JPanel player_color = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      player_color.setPreferredSize(new Dimension(pHeight, pHeight));
      player_color.setMaximumSize(new Dimension(pHeight, pHeight));
      player_color.setBackground(r.color_players.get(p.alias));

      // Player name and alias
      JLabel player_info = new JLabel(p.alias + " (" + p.getRole().name + ")");
      //player_info.setPreferredSize(new Dimension(width / 2, pHeight));
      //player_info.setMaximumSize(new Dimension(width / 2, pHeight));
      
      // Adds objects
      player_entry.add(player_color);
      player_entry.add(player_info);
      player_pane.add(player_entry);
    }

    // Populates disease pane
    for(Disease d : r.diseases){
      JPanel disease_entry = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      disease_entry.setPreferredSize(new Dimension(width / 2, dHeight));
      disease_entry.setMaximumSize(new Dimension(width / 2, dHeight));

      // Creates disease info items
      // Disease color
      JPanel disease_color = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      disease_color.setPreferredSize(new Dimension(dHeight, dHeight));
      disease_color.setMaximumSize(new Dimension(dHeight, dHeight));
      disease_color.setBackground(r.color_diseases.get(d.alias));

      // RAWR - This decals should be specified in the updater
      // If cure available, then adds a border
      /*
      if(d.erradicated)
        disease_color.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(3.0f), Color.green));

      else if(!d.cure)
        disease_color.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, new float[] {2.0f}, 0.0f), Color.red));
      */

      JLabel disease_spreads = new JLabel(String.valueOf(d.spreads_left));
      disease_color.add(disease_spreads);

      // Disease name and global info
      JLabel disease_info = new JLabel(d.name);

      // Adds objects
      disease_entry.add(disease_color);
      disease_entry.add(disease_info);
      dis_pane.add(disease_entry);
    }

    this.add(player_scroll);
    this.add(dis_scroll);

    // Splits canvas horizontally to show player and diseases data
  }
}
