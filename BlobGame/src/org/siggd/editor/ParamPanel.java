package org.siggd.editor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.actor.Actor;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * This class contains controls to set actor parameters
 * @author mysterymath
 *
 */
public class ParamPanel extends JPanel implements TableModelListener, ActionListener{
	private JTable properties;
	private Actor target;		//Actor for which the values are represented
	private DefaultTableModel defaultModel;
	private String[] bodyTypes = { "Static", "Kinematic", "Dynamic" };
	private String[] staticFilterKeys = {"Density", "Momentum X", "Momentum Y","Velocity X","Velocity Y"};	//props not to show if actor is static
	private JComboBox bodyType;
	private JScrollPane model;
	private boolean flag = false;	//< flag in action performed for combo box
	/**
	 * Constructor
	 */
	public ParamPanel() {
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
		bodyType = new JComboBox(bodyTypes);
		bodyType.setSelectedIndex(0);
		add(bodyType);

		bodyType.addActionListener(this);
	}
	/**
	 * Fills table with an Actor's properties
	 * @param a Actor to pull the properties from
	 */
	public void setActor(Actor a){
		if(a==null) {
			return;
		}
		boolean newActor = false;
		if(a!=target){
			newActor = true;
			flag = true;
			BodyType bt = a.getMainBody().getType();
			bodyType.setSelectedIndex(bt.getValue());
			flag = false;
		}
		target = a;
		HashMap<String, Object> props = a.getProperties();
		ArrayList<String> keys = new ArrayList<String>();
		for(String key : props.keySet()){
			if(key.equals("BodyType")){
				continue;					//this is handled by dropdown, don't include
			}else if(Convert.getInt(target.getProp("BodyType")) == 0 
					&& Arrays.asList(staticFilterKeys).contains(key)){		//filter props for static Actors that are non-essential
				continue;
			}
			keys.add(key);
		}
		Collections.sort(keys);
		
		Object[][] data = new Object[keys.size()][2]; //-1 for not including bodytype
		Vector<Vector<Object>> info;
		if(newActor){
			info = new Vector<Vector<Object>>();	//new actor wipe away props
		}else{	
			info= defaultModel.getDataVector();		//old actor use its pevious vals
			for(int i = 0; i<info.size(); i++){
				Vector<Object> prop = info.get(i);
				if(Arrays.asList(staticFilterKeys).contains(prop.get(0))
						&& Convert.getInt(target.getProp("BodyType")) == 0){
					info.remove(prop);
				}
			}
		}
		if(info.size()<=0){
			Vector<String> cols = new Vector<String>();
			cols.add("Property");
			cols.add("Value");
			defaultModel.setDataVector(info,cols);
		}

		int index = 0;
		boolean tableChanged = false;
		for(String key : keys){
			int ind = -1;
			index = 0;
			for(Object o : info){
				Vector<Object> v = (Vector<Object>)o;	//check for old vals
				if(v.contains(key)){
					ind = index;
					break;
				}
				index++;
			}
			if(ind!=-1){								//only  true when !newActor
				Vector<Object> temp = info.get(ind);
				Object o = temp.get(1);
				Object p = a.getProp(key);				
				if(!(o.equals(p))){					//property is updated
					temp.set(1, p);
					tableChanged = true;
					defaultModel.fireTableCellUpdated(ind, 1);
				}
			}else{
				ArrayList<Object> row = new ArrayList<Object>();	//this is for a new actor entirely
				row.add(key);
				row.add(a.getProp(key));
				info.add(new Vector<Object>(row));
			}
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if(e==null)
			return;
		int row = e.getFirstRow();
		int col = e.getColumn();
		if(row>=0 && col==1){
			String key = (String) defaultModel.getValueAt(row, 0);
			try {
				if(!target.getProp(key).equals(defaultModel.getValueAt(row, 1))){ //Only if value has changed
					final PropertyChangeCommand pcc = new PropertyChangeCommand(target, key, target.getProp(key), defaultModel.getValueAt(row, 1));
					
					// Perform edit in game thread
					final Editor edit = Game.get().getEditor();
					edit.getQueue().add(new Runnable() {
						@Override
						public void run() {
							edit.performEdit(pcc);
						}
					});
				}
			} catch (Exception exception) {
				System.out.println(exception.toString());
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(this.getParent()!=null && ((JTabbedPane)this.getParent()).getSelectedIndex()==1){	//if the param panel is currently open
			if(e.getSource().equals(bodyType)){
				if(target!=null){
					if(flag) {	//don't change the bodytype if this is caused by a programatic change to dropdown
						return;
					}
					int sel = bodyType.getSelectedIndex();
					switch(sel){
					case 0:	target.setProp("BodyType", 0);
					break;
					case 1:	target.setProp("BodyType", 1);			//setProp sets changes the Body automatically
					break;
					case 2:	target.setProp("BodyType", 2);
					break;
					}
					setActor(target);
					defaultModel.fireTableChanged(new TableModelEvent(defaultModel));
				}
			}
		}
	}
}
