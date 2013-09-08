package org.siggd.editor;


import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.siggd.Game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;

public class WorldPanel extends JPanel implements TableModelListener{
	private JTable properties;
	private DefaultTableModel defaultModel;
	private JScrollPane model;
	private JLabel mouseOutput;
	private JLabel framerates;
	/**
	 * Constructor
	 */
	public WorldPanel() {
		defaultModel = new DefaultTableModel();
		defaultModel.addColumn("Property");
		defaultModel.addColumn("Value");
		defaultModel.addTableModelListener(this);
		properties = new JTable(defaultModel);
		properties.setPreferredScrollableViewportSize(new Dimension(300, 130));
		model = new JScrollPane(properties);
		model.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		model.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(model);
		mouseOutput = new JLabel();
		mouseOutput.setText("(X,Y) = \n");
		add(mouseOutput);
		framerates = new JLabel();
		int fps = getFramerate();
		framerates.setText("FPS: " + fps);
		add(framerates);
		setWorldProperties();
	}
	@Override
	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int col = e.getColumn();
		if(row>=0 && col==1){
			String key = (String) defaultModel.getValueAt(row, 0);
			try {
				if(!Game.get().getLevel().getProp(key).equals(defaultModel.getValueAt(row, 1))){ //Only if value has changed
					final WorldPropertyChangeCommand wpcc = new WorldPropertyChangeCommand(key, Game.get().getLevel().getProp(key), defaultModel.getValueAt(row, 1));
					
					// Perform edit in game thread
					final Editor edit = Game.get().getEditor();
					edit.getQueue().add(new Runnable() {
						@Override
						public void run() {
							edit.performEdit(wpcc);
						}
					});
				}
			} catch (Exception exception) {
				System.out.println(exception.toString());
			}
		}
		
	}
	
	/**
	 * Sets cursor to coordinate values, based on reference point.That point
	 * being the center of the level.
	 * @param x abscissa based on reference point.
	 * @param y ordinate based on reference point.
	 */
	public void setCoordinateValues(float x, float y) {
		mouseOutput.setText ("(X,Y) = (" + String.format("%.2f", x) + ", " + String.format("%.2f",y) + ")\n");
	}
	
	/**
	 * Fills table with the World's properties.
	 * @param a Actor to pull the properties from
	 */
	public void setWorldProperties(){
		HashMap<String, Object> props = Game.get().getLevel().getProps();
		
		String[] keys = new String[props.keySet().size()];
		
		int index = 0;
		
		for(String key : props.keySet()) {
			keys[index] = key;
			index++;
		}
		Arrays.sort(keys);
		
		Object[][] data = new Object[props.keySet().size()][2];
		index = 0;
		for(String key : keys) {
			data[index][0] = key;
			data[index][1] = Game.get().getLevel().getProp(key);
			index++;
		}
		defaultModel.setDataVector(data,new Object[] { "Property", "Value" });
	}
	
	public void setFramerate(){
		int fps = getFramerate();
		framerates.setText("FPS: " + fps);
	}
	
	public int getFramerate(){
		return Gdx.graphics.getFramesPerSecond();
	}
}
