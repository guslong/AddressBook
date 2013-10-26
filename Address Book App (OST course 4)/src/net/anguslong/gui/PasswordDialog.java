package net.anguslong.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class PasswordDialog extends JDialog implements ActionListener {
	private JTextField user;
	private JPasswordField password;
	private String username, passwd;
	private static String[] info;
	private static boolean set = false;
	private int attempts = 1;

	public PasswordDialog(final JFrame owner) {
		// set the dialog title and size

		super(owner, "Login", true);

		setSize(280, 150);
		user = new JTextField(10);
		user.addActionListener(this);
		password = new JPasswordField(10);
		password.addActionListener(this);
		// Create the center panel which contains the fields for entering
		// information
		JPanel center = new JPanel();
		center.setLayout(new GridLayout(3, 2)); // 3 rows leaves a nice space
												// between
		center.add(new JLabel(" Enter UserName:"));
		center.add(user);
		center.add(new JLabel(" Enter Password:"));
		center.add(password);

		// Create the south panel which contains the buttons
		JPanel south = new JPanel();
		JButton submitButton = new JButton("Submit");
		submitButton.setActionCommand("SUBMIT");
		submitButton.addActionListener(this);

		JButton helpButton = new JButton("Help");
		south.add(submitButton);
		south.add(helpButton);
		// Add listeners to the buttons
		helpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) { 
				JOptionPane
						.showMessageDialog(
								owner,
								"Your username and password are the same as those\n"
										+ "you use to access your O'Reilly School of Technology courses.\n"
										+ "You have " + (4 - attempts)
										+ " login attempts left.\n");
			}
		});
		// Add the panels to the dialog window
		Container contentPane = getContentPane();
		contentPane.add(center, BorderLayout.CENTER);
		contentPane.add(south, BorderLayout.SOUTH);

	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("SUBMIT".equals(cmd)) { // Process the inputs.
			username = user.getText();
			char[] input = password.getPassword();

			attempts++; // increment the number of attempts stored

			if (attempts > 3) {
				// show alert dialog and close the application
				JOptionPane.showMessageDialog(this,
						"Too many login attempts.\n"
								+ "Please contact the sysadmin for help.");
				
				System.exit(0);
				
			}

			passwd = new String(input);
			// to verify it is working, print the name and password--remove this
			// line later!
			System.out.println("User is " + username + ", password is "
					+ passwd);
			info = new String[2];
			info[0] = username;
			info[1] = passwd;
			// check username and password
//			if (username.equals("along") && passwd.equals("AL***JAVA")) {
				set = true; // now can send info back
				dispose();
//			}
//			else {
//			JOptionPane.showMessageDialog(this,
//					"Incorrect password.\n"
//							+ "Try again.");
//				set = false;
//			}
		}
	}

	public static String[] login(Object sender) { // object who requested login
													// is the sender;
		JFrame frame = new JFrame(); // attempt is to make as reusable as possible
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final PasswordDialog addPassword = new PasswordDialog(frame);
		addPassword.setVisible(true);
		while (!set)
			// wait until user has put information in before returning values
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		;
		return info;
	}

	
}