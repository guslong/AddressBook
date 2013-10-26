package net.anguslong.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import net.anguslong.controller.AddListingListener;
import net.anguslong.controller.PhoneDocumentListener;
import net.anguslong.controller.PhoneFocusListener;
import net.anguslong.db.DatabaseManager;
import net.anguslong.model.ListingsTableModel;
/* class PhoneBookFrame */
/**
* This class represents the SimplePhoneBook user interface. PhoneBookFrame includes the application
* window as well as the components for retrieving, adding, displaying, and deleting phone number listings
* for the user.
*
* @author David Green
* @version 1.0
*/
public class PhoneBookFrame extends JFrame {
	/** The initial user interface width, in pixels */
	private static final int WIDTH = 577;
	/** The initial user interface height, in pixels */
	private static final int HEIGHT = 466;
	/** Provides methods for displaying a SQL result set in a JTable */

	private ListingsTableModel tblModel;
	/** Used to display the SQL result set in a cell format */
	private JTable table;
	/** A scrollable view for the SQL result set */
	private JScrollPane scrollPane;
	/** A text field for entering the phone listing's last name */
	private JTextField lNameField = new JTextField(10);
	/** A text field for entering the phone listing's first name */
	private JTextField fNameField = new JTextField(10);
	/**
	 * A text field for entering the phone listing's area code. The value in
	 * parentheses is the number of columns (NOT necessarily characters) to
	 * allow for the field.
	 */
	private JTextField areaCodeField = new JTextField(2);
	/** A text field for entering the phone listing's prefix */
	private JTextField prefixField = new JTextField(2);
	/** A text field for entering the phone listing's extension */
	private JTextField suffixField = new JTextField(3);
	/** The menu bar */
	private JMenuBar menu;
	/** boolean to track the input of wildcard characters - used in inner class GetListener */
	private boolean wildcard;
	/** Database Operations */
	private DatabaseManager myDB;

	/* PhoneBookFrame */
    /**
     * The PhoneBookFrame constructor.
     */
	public PhoneBookFrame() {
		String[] info = PasswordDialog.login(this); // static login so can call
													// from class


		// create and initialize the listings table
		myDB = new DatabaseManager(info[0], info[1]);

		// should have access so make GUI
		JButton getButton = new JButton("Get"); // get the listing
		JButton add = new JButton("+"); // add a listing
		JButton rem = new JButton("-"); // remove a listing
		JLabel space = new JLabel(" ");

		// make a menu bar with an exit option - delegates creation of the menubar to the makeMenu() method
		setJMenuBar(makeMenu());

		// set the window size and title
		setTitle("Simple Phone Book");
		setSize(WIDTH, HEIGHT);
		// if user presses Enter, get button pressed
		getRootPane().setDefaultButton(getButton);
		// create the panel for looking up listing
		JPanel south = new JPanel();
		south.setLayout(new FlowLayout(FlowLayout.LEFT));

		south.add(new JLabel("Last:"));
		south.add(lNameField);
		south.add(new JLabel(" First:"));
		south.add(fNameField);
		south.add(new JLabel("  Phone:  ("));
		south.add(areaCodeField);
		south.add(new JLabel(") "));
		south.add(prefixField);
		south.add(new JLabel("-"));
		south.add(suffixField);
		south.add(new JLabel("   "));
		south.add(getButton);
		// create the panel for adding and deleting listings
		JPanel east = new JPanel();
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		east.setLayout(gb);
		add.setFont(new Font("SansSerif", Font.BOLD, 12));
		rem.setFont(new Font("SansSerif", Font.BOLD, 12));

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gb.setConstraints(add, gbc);
		gb.setConstraints(space, gbc);
		gb.setConstraints(rem, gbc);
		east.setLayout(gb);
		east.add(add);
		east.add(space);
		east.add(rem);

		// add the panels
		Container contentPane = getContentPane();
		contentPane.add(south, BorderLayout.SOUTH);
		contentPane.add(east, BorderLayout.EAST);
		// Add listeners
		// When the application closes, drop the Listings table and close the
		// connection to MySQL
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent wEvent) {
				myDB.close(false); // We will want to save our additions to the
									// PhoneBook, so don't drop table
			}
		});
		areaCodeField.addFocusListener(new PhoneFocusListener());
		areaCodeField.getDocument().addDocumentListener(
				new PhoneDocumentListener(areaCodeField, 3));

		prefixField.addFocusListener(new PhoneFocusListener());
		prefixField.getDocument().addDocumentListener(
				new PhoneDocumentListener(prefixField, 3));

		suffixField.addFocusListener(new PhoneFocusListener());
		suffixField.getDocument().addDocumentListener(
				new PhoneDocumentListener(suffixField, 4));

		add.addActionListener(new AddListingListener(this)); // add (+)
																// listener--define
																// in own class

		// remove (-) listener--delete the highlighted listing from the result
		// set and database
		rem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				try {
					int selected = table.getSelectedRow();
					ResultSet rset = myDB.getResultSet();
					if (selected != -1 && selected < tblModel.getRowCount()) {
						rset.absolute(table.getSelectedRow() + 1);
						rset.deleteRow();
						table.repaint();
						table.clearSelection();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		getButton.addActionListener(new GetListener()); // Add the listener for
														// the getButton
														// (GetListener inner
														// class defined below)
		// when the UI first displays, do an empty lookup so the center panel
		// doesn't look funny
		getButton.doClick();
		lNameField.requestFocus(); // set focus to last name field (most common
									// lookup)
	}

	/** creates a JMenuBar with "File" menu and "Quit" menu item
	 * If Quit is selected, a dialog asks the user if they want to save the database state.
	 * @return the JMenuBar */
	public JMenuBar makeMenu() {
		menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem itemQuit = new JMenuItem("Quit");
		itemQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// make a dialog asking if the user wants to save their database
				int response = JOptionPane.showConfirmDialog(PhoneBookFrame.this,
						"Save database before quitting?", "Quit application", JOptionPane.YES_NO_OPTION);
				switch (response) {
				case JOptionPane.YES_OPTION:
					myDB.close(false);
					System.exit(0);
					
				case JOptionPane.NO_OPTION:
					myDB.close(true); // drop the database when qutting
					System.exit(0);
					
				}
			}
		});
						
		fileMenu.add(itemQuit);

		menu.add(fileMenu);
		return menu;

	}

	public DatabaseManager getDBManager() {
		return myDB;
	}

	/* inner class GetListener */
	 /**
     * An inner class for handling the event when the user clicks the "Get" button.
     *
     * @author David Green
     * @version 1.0
     */
	class GetListener implements ActionListener { 
		 /* actionPerformed */
	    /**
	     * This method builds a select Query and executes it against the Listings table to retrieve 
	     * records. This method creates a select string based on what the user has entered in the 
	     * fields for Last Name, First Name, Area Code, Prefix, and Extension. The user may look up a 
	     * record in the Listings table based on any combination of data entered in the text fields. 
	     * The actionPerformed method builds the query string based on the user's input, executes the 
	     * query, and displays it in a scrollable cell format. All data entered in the text fields 
	     * is converted to upper case and any single quote character is replaced with a space 
	     * character before the query is executed.
	     * If a wildcard * is used, all the records are selected.
	     * <pre>
	     * PRE:  A connection to the database has been established. All text fields can be empty. A wildcard * can be used in firstname and lastname
	     * POST: A select string is created based on what was entered, the query is executed and the
	     *      results are displayed.
	     * </pre>
	     * @param aEvent an event generated as a result of the "Get" button being clicked
	     */
		public void actionPerformed(ActionEvent aEvent) {
			// Get whatever the user entered, trim any white space and change to
			// upper case
			String last = lNameField.getText().trim().toUpperCase();
			String first = fNameField.getText().trim().toUpperCase();
			String ac = areaCodeField.getText().trim().toUpperCase();
			String pre = prefixField.getText().trim().toUpperCase();
			String sfx = suffixField.getText().trim().toUpperCase();

			// Replace any single quote chars w/ space char or SQL will think
			// the ' is the end of the string
			last = last.replace('\'', ' ');
			first = first.replace('\'', ' ');
			ac = ac.replace('\'', ' ');
			pre = pre.replace('\'', ' ');
			sfx = sfx.replace('\'', ' ');
			// Get rid of the last result displayed if there is one
			if (scrollPane != null)
				getContentPane().remove(scrollPane);
			
			// check for correct entry in the number fields
			if (ac.matches("\\d*")
					&& pre.matches("\\d*") 
					&& sfx.matches("\\d*")) {
				System.out.println("Regex ok");
				
				// Only execute the query if one or more fields have data, else just
				// display an empty table
				if (last.length() > 0 || first.length() > 0 || ac.length() > 0
						|| pre.length() > 0 || sfx.length() > 0) {
					
					// check if firstname or lastname contain wildcard
					if (last.contains("*") || first.contains("*")) {
						wildcard = true;
					}
							
					// if wildcard, then perform a select all
					if (wildcard) {
						myDB.doGetQuery("SELECT * FROM Listings");
					}
					else {
					myDB.doGetQuery(buildQuery(last, first, ac, pre, sfx));
					}
					
					ResultSet rset = myDB.getResultSet();
					tblModel = new ListingsTableModel(rset);
					table = new JTable(tblModel);
				} else {
					table = new JTable();
				}
				// Allows the user to only delete one record at a time
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				// Add the table with the results to the contentPane and display it.
				scrollPane = new JScrollPane(table);
				getContentPane().add(scrollPane, BorderLayout.CENTER);
				pack();
				doLayout();
			}
			
			// else show a dialog if the phone number doesn't pass validation
			else {
				System.out.println("Regex failed");
				//dialog for failed input
				JOptionPane.showMessageDialog(PhoneBookFrame.this, "You entered incorrect values for the phone number", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			
		}
		/* buildQuery */
	    /**
	     * This method builds a simple select statement for retrieving records from the Listings table.
	     * The select statement is returned as a string. The select statement includes the last, first, ac,
	     * pre, and sfx parameters as the search strings in the where clause of the select statement.
	     * If more than one parameter has data, buildQuery will combine them with an "AND" in the where
	     * clause.
	     * <pre>
	     * PRE:  One or more parameters has length > 0.
	     * POST: A SQL select statement is returned that selects records from the Listings table.
	     * </pre>
	     * @param last  create a SQL query that searches Listings where LAST_NAME = last.
	     * @param first create a SQL query that searches Listings where FIRST_NAME = first.
	     * @param ac    create a SQL query that searches Listings where AREA_CODE = ac.
	     * @param pre   create a SQL query that searches Listings where PREFIX = pre.
	     * @param sfx   create a SQL query that searches Listings where SUFFIX = sfx.
	     * @return a SQL select statement that selects records from the Listings table
	     */
		public String buildQuery(String last, String first, String ac,
				String pre, String sfx) {
			String whereClause = " where";
			// Build the where clause
			if (last.length() > 0)
				whereClause += (" LAST_NAME = '" + last + "'");

			if (first.length() > 0) {
				if (whereClause.length() > 6)
					whereClause += " AND";
				whereClause += (" FIRST_NAME = '" + first + "'");
			}

			if (ac.length() > 0) {
				if (whereClause.length() > 6)
					whereClause += " AND";
				whereClause += (" AREA_CODE = '" + ac + "'");
			}

			if (pre.length() > 0) {
				if (whereClause.length() > 6)
					whereClause += " AND";
				whereClause += (" PREFIX = '" + pre + "'");
			}

			if (sfx.length() > 0) {
				if (whereClause.length() > 6)
					whereClause += " AND";
				whereClause += (" SUFFIX = '" + sfx + "'");
			}

			return "select ID, LAST_NAME, FIRST_NAME, AREA_CODE, PREFIX, SUFFIX from Listings"
					+ whereClause;
		}
	} // End GetListener inner class
} // End PhoneBookFrame class