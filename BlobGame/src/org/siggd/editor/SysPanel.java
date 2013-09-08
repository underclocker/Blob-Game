package org.siggd.editor;

import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.JSONException;
import org.json.JSONObject;
import org.siggd.DebugOutput;
import org.siggd.Game;
import org.siggd.Level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;

/**
 * This class contains controls to affect global editor functionality (load,save,play,etc.)
 * @author mysterymath, wattermann
 *
 */
public class SysPanel extends JPanel implements ActionListener, ItemListener {
	private JButton loadButton;			///< The Load Button
	private JButton saveButton;			///< The Save Button
	private JButton newButton;			///< The New Button
	private JButton simulateButton;		///< Toggle game loop
	private JButton resetButton;		///< Reloads current level, with all changes, starts paused
	private JLabel performingAction;	///< A Text Field to tell the user what is currently happening
	private JCheckBox doDebugRender;    ///< A checkbox to control debug renderering
	private JCheckBox doDebugOutput;	///< A checkbox to control debug printlns
	private JCheckBox doHeavyDebugOutput;
	private JCheckBox doFineDebugOutput;
	private JCheckBox doFinerDebugOutput;
	//private JCheckBox doFramerateRender;    ///< A checkbox to control debug renderering

	/**
	 * Constructor
	 */
	public SysPanel() {
		simulateButton = new JButton("Run");
		resetButton = new JButton("Reset");
		loadButton = new JButton("Load");
		saveButton = new JButton("Save");
		newButton = new JButton("New");
		performingAction = new JLabel(" ");
		doDebugRender = new JCheckBox("Debug Render");
		doDebugOutput = new JCheckBox("Light Debug Output");
		doFineDebugOutput = new JCheckBox("Fine Debug Output");
		doFinerDebugOutput = new JCheckBox("Finer Debug Output");
		doHeavyDebugOutput = new JCheckBox("Finest Debug Output");
		//doFramerateRender = new JCheckBox("Framerate Render");
		
		simulateButton.addActionListener(this);
		resetButton.addActionListener(this);
		loadButton.addActionListener(this);
		saveButton.addActionListener(this);
		newButton.addActionListener(this);
		doDebugRender.addItemListener(this);
		doDebugRender.setSelected(true);
		doDebugOutput.addItemListener(this);
		doDebugOutput.setSelected(true);
		doFineDebugOutput.addItemListener(this);
		doFineDebugOutput.setSelected(false);
		
		doFinerDebugOutput.addItemListener(this);
		doFinerDebugOutput.setSelected(false);
		
		doHeavyDebugOutput.addItemListener(this);
		doHeavyDebugOutput.setSelected(false);
		//doFramerateRender.addItemListener(this);
		//doFramerateRender.setSelected(true);

		add(simulateButton);
		add(resetButton);
		add(loadButton);
		add(saveButton);
		add(newButton);
		add(performingAction);
		add(doDebugRender);
		add(doDebugOutput);
		add(doFineDebugOutput);
		add(doFinerDebugOutput);
		add(doHeavyDebugOutput);
		//add(doFramerateRender);
	}

	public void actionPerformed(ActionEvent e) {
		JButton source = (JButton)e.getSource();
		String fileName;

		/**
		 * Brings up a Dialog to confirm if the user wants to activate the game loop. Assumes mIsGameRunning is defaulted to false.
		 */
		if(source == simulateButton) {
			Game.get().getLevel().stopMusic();
			toggleState();
		}
		if(source == resetButton){
			Game.get().getLevel().stopMusic();
			Game.get().setState(Game.EDIT);					// Stops Game
			simulateButton.setText("Run");
			performingAction.setText("Game Stopped");
			try {
				final JSONObject level = Game.get().getLevel().save();
				Editor edit = Game.get().getEditor();
				edit.getQueue().add(new Runnable() {
					@Override
					public void run() {
						Game.get().setLevel(level);
					}
				});
			} catch (JSONException e1) {
				System.out.println("Failed to save and reset level");
			}
		}
		/**
		 * Brings up a Load Map Dialog and loads that map
		 */
		if(source == loadButton) {
			Game.get().setState(Game.EDIT);					// Stops Game
			simulateButton.setText("Run");
			Game.get().getLevel().stopMusic();
			performingAction.setText("Game Stopped");
			JFileChooser fileChooser = new JFileChooser(new File("levels"));	// JFileChooser defaults to the levels/ directory
			int dialogue = fileChooser.showOpenDialog(null);		
			if(dialogue == JFileChooser.APPROVE_OPTION) {
				fileName = fileChooser.getSelectedFile().getName();
				performingAction.setText("Loading " + fileName);		// Updates the user on what is happening
				
				// Load level in game thread
				final String tmpFileName = fileName;
				Editor edit = Game.get().getEditor();
				edit.getQueue().add(new Runnable() {
					@Override
					public void run() {
						Game.get().setLevel(tmpFileName);
					}
				});
			}
		}
		
		/**
		 * Brings up a text field to save the map as
		 */
		if(source == saveButton) {
			Game.get().setState(Game.EDIT);					// Stops Game
			simulateButton.setText("Run");
			performingAction.setText("Game Stopped");
			AssetManager man = Game.get().getAssetManager();
			String suggestedName = man.getAssetFileName(Game.get().getLevel());
			if(suggestedName == null){
				suggestedName = "untitled";
			}
			
			fileName = (String)JOptionPane.showInputDialog(this, "Map Name:", "Save", 3, null, null, "" + suggestedName);
			if(fileName == null || (fileName != null && ("".equals(fileName)))) {
				performingAction.setText("Canceled Save");								// Check if the cancel button was pressed
			} else {
				performingAction.setText("Saving "+fileName+"...");
				try {
					JSONObject level = Game.get().getLevel().save();					// JSON File Loading
					FileHandle fileToSave = Gdx.files.local("levels/" + fileName);
					fileToSave.writeString(level.toString(4), false);
					performingAction.setText("Saved to levels/"+ fileName);
				} catch (JSONException e1) {
					DebugOutput.info(this,e1.toString());
					performingAction.setText("Failed to Save Level");
				}
			}
		}
		
		/**
		 * Loads a new map
		 */
		if(source == newButton) {
			Game.get().getLevel().stopMusic();
			Game.get().setLevel(new Level(null));
		}
	}
	
	/**
	 * This method checks the toggle state, and swaps them.
	 */
	public void toggleState(){
			if(Game.get().getState() == Game.PLAY) {
				Game.get().setState(Game.EDIT);				// Stops Game
				simulateButton.setText("Run");
				performingAction.setText("Game Stopped");
			} else {
				//int doYouWantToSimulate = JOptionPane.showConfirmDialog(this, "Are you sure you want begin simulating?", "Run Game?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);  // Place Holder Message
				//if(doYouWantToSimulate == 0) {				// Clicked Yes
					Game.get().setState(Game.PLAY);			// Runs Game
					simulateButton.setText("Stop");
					performingAction.setText("Running Game");
				//}
			}
	}

	/**
	 * Listener for any event involving item state changes
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		
		// Enable or disable the debug renderer
		if (source == doDebugRender) {
			Game.get().getLevelView().setDebugRender(e.getStateChange() != ItemEvent.DESELECTED);
		}
		if (source == doDebugOutput) {
			if(DebugOutput.isEnabled())
			{
				DebugOutput.disable();
			}
			else
			{
				DebugOutput.enable();
			}
		}
		if (source == doHeavyDebugOutput) {
			if(DebugOutput.isHeavyEnabled())
			{
				DebugOutput.heavyDisable();
			}
			else
			{
				DebugOutput.heavyEnable();
			}
		}
		
		if (source == doFineDebugOutput) {
			if(DebugOutput.isFineEnabled())
			{
				DebugOutput.fineDisable();
			}
			else
			{
				DebugOutput.fineEnable();
			}
		}
		if (source == doFinerDebugOutput) {
			if(DebugOutput.isFinerEnabled())
			{
				DebugOutput.finerDisable();
			}
			else
			{
				DebugOutput.finerEnable();
			}
		}
		// Enable or disable the framerate renderer
		/*if (source == doFramerateRender) {
			Game.get().getLevelView().setFramerateRender(e.getStateChange() != ItemEvent.DESELECTED);
		}*/
	}
}