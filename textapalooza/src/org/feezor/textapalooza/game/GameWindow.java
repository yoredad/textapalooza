package org.feezor.textapalooza.game;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.feezor.textapalooza.game.domain.Action;
import org.feezor.textapalooza.game.domain.CommandAction;
import org.feezor.textapalooza.game.domain.CommandAction.Command;
import org.feezor.textapalooza.game.domain.Door;
import org.feezor.textapalooza.game.domain.Game;
import org.feezor.textapalooza.game.domain.Item;
import org.feezor.textapalooza.game.domain.Player;
import org.feezor.textapalooza.game.domain.Room;
import org.feezor.textapalooza.utils.FileUtil;
import org.feezor.textapalooza.utils.JSONUtils;

public class GameWindow {

	private JFrame frame;
	private JTextField textField;
	private JTextArea textArea;
	private JButton submitBtn;
	private JLabel lblNewLabel;
	private static String gameFile = "resources\\game.json";
	private Game game = null;
	private Room curRoom = null;
	private Player player = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if(args.length>0) {
						if(args.length<2) {
							System.out.println("invalid arguments");
							System.exit(-1);
						}
						if("-f".equals(args[0])) {
							gameFile = args[1];
						}
					}
					GameWindow window = new GameWindow(new File(gameFile));
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(e.getMessage());
					System.exit(-1);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GameWindow() {
		super();
	}
	
	public GameWindow(File f) {
		initialize(f);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(File f) {
		game = JSONUtils.jsonToObject(FileUtil.readFile(f), Game.class);
		
		player = new Player();
		curRoom = game.getRooms().get(0);
		
		frame = new JFrame();
		frame.setBounds(100, 100, 986, 652);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(40, 60, 869, 463);
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		
		textField = new JTextField();
		textField.setBounds(22, 557, 646, 26);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		//TODO: add a listener to capture the enter key and call parse commands
		javax.swing.Action action = new AbstractAction()
		{
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	parseCommands(textField.getText());
		    }
		};

		textField.addActionListener( action );
		
		submitBtn = new JButton("Submit");
		submitBtn.setBounds(678, 554, 105, 32);
		submitBtn.addActionListener(action);
		
		frame.getContentPane().add(submitBtn);
		
		lblNewLabel = new JLabel(game.getTitle());
		lblNewLabel.setBounds(40, 11, 862, 38);
		frame.getContentPane().add(lblNewLabel);
		
		// init the display
		textArea.setText(curRoom.getDescription());
		
		// give player starting items
		if(game.getStartingItems()!=null) {
			player.setItems(game.getStartingItems());
		}
	}
	
	private void parseCommands(String text) {
		if(text==null) return;
		String s = text.trim();
		if(s.length()==0) return;
		String[] vals = s.split(" "); //TODO: cleanup tokens and remove empty ones
//		this.textArea.setText(this.textArea.getText()+"\n"+vals[0]);
		String cmd = vals[0].trim().toLowerCase();
		switch (cmd) {
		case "look":
			this.textArea.setText(curRoom.getDescription());
			this.textArea.setText(this.textArea.getText()+"\nYou see:\n");
			if(curRoom.getItems()!=null && curRoom.getItems().size()>0) {
				for(Item item : curRoom.getItems()) {
					this.textArea.setText(this.textArea.getText()+"\n"+item.getDescription()+ " ["+item.getId()+"]");
				}
			}else {
				this.textArea.setText(this.textArea.getText()+"\nThe room is empty\n");
			}
			if(curRoom.getDoors()!=null && curRoom.getDoors().size()>0) {
				for(Door door : curRoom.getDoors()) {
					this.textArea.setText(this.textArea.getText()+"\n"+door.getDescription() + " on the " + door.getOrientation().getName()+" wall\n");
				}
			}else {
				this.textArea.setText(this.textArea.getText()+"\nThe room has no doors\n");
			}
			break;
		case "inventory":
			this.textArea.setText(curRoom.getDescription());
			this.textArea.setText(this.textArea.getText()+"\nYou have:\n");
			if(player.getItems()!=null && player.getItems().size()>0) {
				for(Item item : player.getItems()) {
					this.textArea.setText(this.textArea.getText()+"\n"+item.getDescription()+ " ["+item.getId()+"]");
				}
			}else {
				this.textArea.setText(this.textArea.getText()+"\nYou have no items\n");
			}
			break;
		case "help":
			this.textArea.setText(this.game.getHelp());
			break;
		case "credits":
			this.textArea.setText(this.game.getCredits());
			break;
		case "search":
			this.textArea.setText(curRoom.getDescription());
			String thing = "";
			if (vals.length>1) thing = vals[1].trim();
			if(curRoom.getItems()!=null && curRoom.getItems().size()>0) {
				boolean found = false;
				for(Item item : curRoom.getItems()) {
					if(item.getId().equalsIgnoreCase(thing)) {
						found=true;
						if(item.hasItems()) {
							this.textArea.setText(this.textArea.getText()+"\nYou find:\n");
							for(Item item2 : item.getItems()) {
								this.textArea.setText(this.textArea.getText()+"\n"+item2.getDescription()+ " ["+item2.getId()+"]");
							}
						}else {
							this.textArea.setText(this.textArea.getText()+"\n" + item.getDescription() + " is empty\n");
						}
						break;
					}
					
				}
				if(!found) {
					this.textArea.setText(this.textArea.getText()+"\nI don't see that item: "+thing+"\n");
				}
			}else {
				this.textArea.setText(this.textArea.getText()+"\nI don't see that item: "+thing+"\n");
			}
			break;
		case "loot":
			this.textArea.setText(curRoom.getDescription());
			String container = "";
			if (vals.length>1) container = vals[1].trim();
			if(curRoom.getItems()!=null && curRoom.getItems().size()>0) {
				boolean found = false;
				for(Item item : curRoom.getItems()) {
					if(item.getId().equalsIgnoreCase(container)) {
						found=true;
						if(item.hasItems()) {
							this.textArea.setText(this.textArea.getText()+"\nYou took:\n");
							boolean took1 = false ;
							for(Item item2 : item.getItems()) {
								if (item2.isCanTake()) {
									this.textArea.setText(this.textArea.getText()+"\n"+item2.getDescription()+ " ["+item2.getId()+"]");
									took1 = true ;
									player.addItem(item2);
								}
							}
							if (!took1) {
								this.textArea.setText(this.textArea.getText()+"\nnothing:\n");
							}
							List <Item> newItems = new ArrayList <Item>() ; //newItems is the items that aren't takeable
							for(Item item2 : item.getItems()) {
								if (!item2.isCanTake()) {
									newItems.add(item2) ;				
								}
							}
							item.setItems(newItems); 
							
						}else {
							this.textArea.setText(this.textArea.getText()+"\n" + item.getDescription() + " is empty\n");
						}
						break;
					}
					
				}
				if(!found) {
					this.textArea.setText(this.textArea.getText()+"\nI don't see that item: "+container+"\n");
				}
			}else {
				this.textArea.setText(this.textArea.getText()+"\nI don't see that item: "+container+"\n");
			}
			break;
		case "talk" :
			this.textArea.setText(curRoom.getDescription());
			String person = "" ;
			if (vals.length>1) person = vals[1].trim();
			if(curRoom.getItems()!=null && curRoom.getItems().size()>0) {
				boolean found = false;
				for(Item roomItem : curRoom.getItems()) {
					if(roomItem.getId().equalsIgnoreCase(person)) {
						found=true;
						if (roomItem.getCommandActions() != null) {
							boolean check = false ;
							for (CommandAction ca : roomItem.getCommandActions()) {
								if (cmd.equalsIgnoreCase(ca.getCommand().getName())) {
									check = true ;
									this.textArea.setText(this.textArea.getText()+"\n" + roomItem.getName() + " says:\n") ;
									for (Action act : ca.getActions()) {
										this.textArea.setText(this.textArea.getText()+"\n"+ act.getText()+"\n");
									}
								}
								
							}
							if (!check) {
								this.textArea.setText(this.textArea.getText()+"\nThere was no response.\n");
							}
						}
						else {
							this.textArea.setText(this.textArea.getText()+"\nThere was no response.\n");
						}
					}
				}
				if (!found) {
					this.textArea.setText(this.textArea.getText()+"\nThat person doesn't exist.\n");
				}
			}
			else {
				this.textArea.setText(this.textArea.getText()+"\nThat person doesn't exist.\n");
			}
			break ;
		case "use" :
			this.textArea.setText(curRoom.getDescription());
			thing = ""; // object we have
			String target = ""; //what we're using it on
			if (vals.length>1) thing = vals[1].trim();
			if (vals.length>2) target = vals[2].trim();
			if (thing == null || thing.length() == 0) {
				this.textArea.setText(this.textArea.getText() + "\nYou do not have" + thing + " in your inventory.\n");
				break ;
			}
			if (target == null || target.length() == 0) { 
				this.textArea.setText(this.textArea.getText() + "\nI can not see " + thing + " in the room.\n");
				break ;
			}
			
			if (!doesListContainItem(player.getItems(), thing)) { //if it's not in the inventory
				this.textArea.setText(this.textArea.getText() + "\nYou do not have " + thing + " in your inventory.\n");
				break ;
			}
			if (!doesListContainItem(curRoom.getItems(), target)) { //if it's not in the room
				this.textArea.setText(this.textArea.getText() + "\nI can not see " + thing + " in the room.\n");
				break ;
			}
			Item targetItem = getItemFromList(curRoom.getItems(), target) ; //gives us our item
			//iterate over the command actions for the TARGET item, and look for the 'use' command
			//then, iterate over the actions for that command and see if one of the item identifiers is equal to thing
			//see if the use has been used already. if not-
			//if the target item has a reward id
			//then remove the reward from target's inventory
			//and give the reward item to the player
			//display the text if the action has text
			//look at the thing and see if it has a one time use indicator- if so, then remove it from the player's inventory
			//if its one time use, then set the action completed to true
			boolean foundAction = false ;
			for (CommandAction ca : targetItem.getCommandActions()) {
				if (Command.USE == ca.getCommand()) { //if the item's command action list contains use
					for (Action action : ca.getActions()) { //looking through the action list of the object
						if (thing.equalsIgnoreCase(action.getItemId()) && !action.isActionCompleted()) {
							foundAction = true ;
							if (action.getRewardId() != null) {
								Item rewardItem = getItemFromList (targetItem.getItems(), action.getRewardId()) ; //gives us the reward item
								player.addItem(rewardItem); //gives item to player
								this.textArea.setText(this.textArea.getText()+ "\nYou receive the " + rewardItem.getDescription() + " !\n") ;
								removeItemFromList(targetItem.getItems(), action.getRewardId()) ; //removes item from target inventory								
							}
							if (action.getText() != null) {
								this.textArea.setText(this.textArea.getText() + "\n" + action.getText() + "\n") ;
							}
							Item thingItem = getItemFromList (player.getItems(), thing) ;
							if (thingItem.isOneTimeUse()) { //if one time use
								removeItemFromList (player.getItems(), thing) ; //remove it
							}
							if (action.isOneTimeUse()) {
								action.setActionCompleted(true) ;																			
							}
						}
					}
				}
			}
			
			if (foundAction == false) {
				this.textArea.setText(this.textArea.getText()+ "\n it didn't seem very effective...\n" ) ;
			}
			
			break ;
		default:
			this.textArea.setText(curRoom.getDescription());
			this.textArea.setText(this.textArea.getText()+"\nI don't understand the command: "+cmd+"\n");
		}
	}
	private boolean doesListContainItem (List<Item> items, String id) { //checks if an item is within a list, then return true/false
		boolean found = false ;
		for (Item item : items) { 
			if (item.getId().equalsIgnoreCase(id)) { //if we have a match
				found = true ;
				break ;
			}
		}
		
		return found ;
	}
	
	private Item getItemFromList (List<Item> items, String id) { //returns an item if it's within the list
		for (Item item : items) { 
			if (item.getId().equalsIgnoreCase(id)) { //if we have a match
				return item ;
			}
		}		
		return null ;
	}
	
	private void removeItemFromList (List<Item> items, String id) {
		for (int i=0; i<items.size(); i++) {
			if (items.get(i).getId().equalsIgnoreCase(id)) {
				items.remove(i) ;
				break ;
			}
		}
	}
	
}
