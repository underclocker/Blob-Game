package org.siggd.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.siggd.Game;
import org.siggd.actor.Actor;
import org.siggd.actor.Background;

import pong.client.core.BodyEditorLoader.RigidBodyModel;

/**
 * This class contains controls to add actors to the map
 * @author mysterymath
 *
 */
public class ActorPanel extends JPanel implements ActionListener, ListSelectionListener, CaretListener{
	public static enum Action{
		ADD,SELECTMOVE,REMOVE
	}
	int currentAction;
	private JButton rotate;
	private JButton undo;
	private JButton redo;
	private JList actorPanelList;
	private JScrollPane model;
	private ArrayList<String> actorNames;
	public JTextField mSearchField;
	private DefaultListModel defaultActorPanelList;
	JRadioButton addButton;
	JRadioButton selectMoveButton;
	JRadioButton removeButton;
	/**
	 * Constructor
	 */
	public ActorPanel(){
		actorNames = new ArrayList<String>();
		
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));	// make the buttons organize vertically
		
		addButton = new JRadioButton("Add");
		addButton.setActionCommand("0");
		
		selectMoveButton = new JRadioButton("Select and Move");
		selectMoveButton.setActionCommand("1");
		selectMoveButton.setSelected(true);
		currentAction = 1;
		
		removeButton = new JRadioButton("Remove");
		removeButton.setActionCommand("2");
		
		addButton.addActionListener(this);
		selectMoveButton.addActionListener(this);
		removeButton.addActionListener(this);

		ButtonGroup actions = new ButtonGroup();
		actions.add(addButton);
		actions.add(selectMoveButton);
		actions.add(removeButton);
		
		radioPanel.add(addButton);
		radioPanel.add(selectMoveButton);
		radioPanel.add(removeButton);
		
		add(radioPanel);
		
		JPanel actorPanel = new JPanel();
		actorPanel.setLayout(new BoxLayout(actorPanel, BoxLayout.Y_AXIS));	// make the buttons organize vertically
		JLabel actorLabel = new JLabel("Actors");
		actorLabel.setAlignmentX(CENTER_ALIGNMENT);
		actorPanel.add(actorLabel);
		defaultActorPanelList = new DefaultListModel();		//not replaced by array because it needs to be resizeable
				
		// Add the lists of bodies (identified with an asterisk)
		ArrayList<String> bodies = getBodies();
		for (String s : bodies) {
			defaultActorPanelList.addElement("*" + s);
			actorNames.add("*"+s);
		}
		
		for(Actor a: Game.get().getActorEnum().getFakeLevel()){					//to start
			defaultActorPanelList.addElement(a.getClass().getName());
			actorNames.add(a.getClass().getName());
		}
		Object [] temp = defaultActorPanelList.toArray();
		Arrays.sort(temp);
		actorPanelList = new JList(temp);
		actorPanelList.setSelectedIndex(0);						//so there will be a default actor to add
		actorPanelList.addListSelectionListener(this);
		actorPanelList.setFixedCellWidth(200);
		model = new JScrollPane(actorPanelList);
		model.setMinimumSize(model.getSize());
		model.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		model.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		actorPanel.add(model);
		mSearchField = new JTextField();
		mSearchField.addCaretListener(this);
		actorPanel.add(mSearchField);
		add(actorPanel);
		
		
		undo = new JButton("undo");
		undo.addActionListener(this);
		redo = new JButton("redo");
		redo.addActionListener(this);
		rotate = new JButton("rotate");
		rotate.addActionListener(this);
		
		add(undo);
		add(redo);
		add(rotate);
	}
	/**
	 * 
	 * @return Current action represented as an int, compare against ActorPanel.Action
	 */
	public Action getCurrentAction(){
		return Action.values()[currentAction];
	}
	/**
	 * 
	 * @param action Mode desired to switch to
	 */
	public void setCurrentAction(Action action){
		currentAction = action.ordinal();
		addButton.setSelected(false);
		selectMoveButton.setSelected(false);
		removeButton.setSelected(false);
		
		switch(currentAction){ //Resets selected case to true.
		case 0: addButton.setSelected(true);
			break;
		case 1: selectMoveButton.setSelected(true);
			break;
		case 2: removeButton.setSelected(true);
			break;
		}
		
	}
	public Class getCurrentActor(){
		String sel = (String)actorPanelList.getSelectedValue();
		if(sel==null)return null;
		if (sel.charAt(0) == '*') {
			Game.get().getEditor().setBody(sel.substring(1));
			return Background.class;
		}
		try {
			Game.get().getEditor().setBody(null);
			return Class.forName((String) actorPanelList.getSelectedValue());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(undo)){
			Game.get().getEditor().undo();
		}else if(e.getSource().equals(redo)){
			Game.get().getEditor().redo();
		}else if (e.getSource().equals(rotate)){
			Actor a = Game.get().getEditor().getSelectedActor();
			if(a!=null){
				Game.get().getEditor().performEdit(new RotateCommand(a, 10));
			}
		}else{
			int action = Integer.parseInt(e.getActionCommand());
			currentAction = action;
			Game.get().getEditor().setSelected(null);	//when you change edit modes deselect currently selected actor
		}
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		setCurrentAction(Action.ADD);
		addButton.setSelected(true);
		removeButton.setSelected(false);
		selectMoveButton.setSelected(false);
	}
	
	private ArrayList<String> getBodies() {
		ArrayList<String> ret = new ArrayList<String>();
		
		Map<String, RigidBodyModel> m = Game.get().getBodyEditorLoader().getInternalModel().rigidBodies;
		ret.addAll(m.keySet());
		
		Collections.sort(ret);
		return ret;
	}
	@Override
	public void caretUpdate(CaretEvent e) {
		int loc = e.getDot();
		String search = mSearchField.getText();
		defaultActorPanelList.removeAllElements();
		for(String s: actorNames){
			if(s.toLowerCase().contains(search.toLowerCase())){
				defaultActorPanelList.addElement(s);
			}
		}
		Object [] temp = defaultActorPanelList.toArray();
		Arrays.sort(temp);
		actorPanelList.setListData(temp);
		actorPanelList.setSelectedIndex(0);	
		
	}
}