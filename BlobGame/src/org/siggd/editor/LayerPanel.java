package org.siggd.editor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LayerPanel extends JPanel implements ActionListener {
	private JLabel leLayerLabel;
	public Integer[] lesLayers;
	public ArrayList<Integer> noShow;
	
	
	/**
	 * Constructor
	 */
	public LayerPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		noShow = new ArrayList<Integer>();
	}
	
	public void updateLayers() {
		this.removeAll();
		leLayerLabel = new JLabel("Layer");
		add(leLayerLabel);
		for(int leLayer : lesLayers) {
			JCheckBox leSeeLayer = new JCheckBox("" + leLayer);
			add(leSeeLayer);
			if(!noShow.contains(leLayer)) {
				leSeeLayer.setSelected(true);
			}
			leSeeLayer.addActionListener(this);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		JCheckBox temp = (JCheckBox) e.getSource();
		if(temp.isSelected()) {
			noShow.remove(Integer.parseInt(temp.getText()));
		}
		else {
			noShow.add(Integer.parseInt(temp.getText()));
		}
	}
	
	
	
	
}
