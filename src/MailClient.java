import java.io.*;
//import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/* $Id: MailClient.java,v 1.7 1999/07/22 12:07:30 kangasha Exp $ */

/**
 * A simple mail client with a GUI for sending mail.
 * 
 * @author Jussi Kangasharju
 */
public class MailClient extends Frame {
	
	private static final long serialVersionUID = 1L;
	/* The stuff for the GUI. */
	private Button btSend = new Button("Send");
	private Button btClear = new Button("Clear");
	private Button btQuit = new Button("Quit");

	/**
	 * Add cc field.
	 */
	private Label cclaber = new Label("CC:");
	private TextField ccField = new TextField("", 40);

	private Label toLabel = new Label("To:");
	private TextField toField = new TextField("", 40);
	private Label subjectLabel = new Label("Subject:");
	private TextField subjectField = new TextField("", 40);
	private Label messageLabel = new Label("Message:");
	private TextArea messageText = new TextArea(10, 40);

	/**
	 * Properties. since Yahoo mail will give an app password to third-party user,
	 * and it's hard to remember. I just paste it as fixed property, so the input
	 * password is a fake one only for presentation use.
	 * 
	 */
	private String server = "smtp.mail.yahoo.com";
	private String from;
	private String password = "nhjkeuvnturrerxo";
	private String fakeword;

	/**
	 * Create a new MailClient window with fields for entering all the relevant
	 * information (From, To, Subject, and message).
	 */
	@SuppressWarnings("deprecation")
	public MailClient() {
		super("Java Mailclient");
		
		//Authentication
		from = JOptionPane.showInputDialog("Please enter your email address:");
		fakeword = this.getpassword();

		/*
		 * Create panels for holding the fields. To make it look nice, create an extra
		 * panel for holding all the child panels.
		 */
		/**
		 * Add cc panel
		 */
		Panel ccPanel = new Panel(new BorderLayout());

		Panel toPanel = new Panel(new BorderLayout());
		Panel subjectPanel = new Panel(new BorderLayout());
		Panel messagePanel = new Panel(new BorderLayout());
		ccPanel.add(cclaber, BorderLayout.WEST);
		ccPanel.add(ccField, BorderLayout.CENTER);
		toPanel.add(toLabel, BorderLayout.WEST);
		toPanel.add(toField, BorderLayout.CENTER);
		subjectPanel.add(subjectLabel, BorderLayout.WEST);
		subjectPanel.add(subjectField, BorderLayout.CENTER);
		messagePanel.add(messageLabel, BorderLayout.NORTH);
		messagePanel.add(messageText, BorderLayout.CENTER);
		Panel fieldPanel = new Panel(new GridLayout(0, 1));
		/*
		 * fieldPanel.add(serverPanel); fieldPanel.add(fromPanel);
		 */
		fieldPanel.add(toPanel);
		fieldPanel.add(ccPanel);
		fieldPanel.add(subjectPanel);

		/*
		 * Create a panel for the buttons and add listeners to the buttons.
		 */
		Panel buttonPanel = new Panel(new GridLayout(1, 0));
		btSend.addActionListener(new SendListener());
		btClear.addActionListener(new ClearListener());
		btQuit.addActionListener(new QuitListener());
		buttonPanel.add(btSend);
		buttonPanel.add(btClear);
		buttonPanel.add(btQuit);

		/* Add, pack, and show. */
		add(fieldPanel, BorderLayout.NORTH);
		add(messagePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		pack();
		show();
	}

	static public void main(String argv[]) {
		new MailClient();
	}

	/**
	 * A method that get the password
	 * @return password
	 */
	public String getpassword() {
		JPasswordField jpf = new JPasswordField(24);
		JLabel jl = new JLabel("Enter Your password: ");
		Box box = Box.createHorizontalBox();
		box.add(jl);
		box.add(jpf);
		int x = JOptionPane.showConfirmDialog(null, box, "Password Entry", JOptionPane.OK_CANCEL_OPTION);

		if (x == JOptionPane.OK_OPTION) {
			return jpf.getText();
		}
		return null;
	}

	/* Handler for the Send-button. */
	class SendListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Sending mail");
			
			if ((toField.getText()).equals("")) {
				System.out.println("Need recipient!");
				return;
			}

			/* Create the message */
			Message ccMessage = new Message("", "", "", "");
			Message mailMessage = new Message(from, toField.getText(), subjectField.getText(), messageText.getText());
			if (!ccField.getText().equals("")) {
				ccMessage = new Message(from, ccField.getText(), subjectField.getText(), messageText.getText());
				if (!ccMessage.isValid()) {
					return;
				}
				;
			}

			/*
			 * Check that the message is valid, i.e., sender and recipient addresses look
			 * ok.
			 */
			if (!mailMessage.isValid()) {
				return;
			}

			/*
			 * Create the envelope, open the connection and try to send the message.
			 */
			try {
				Envelope envelope = new Envelope(mailMessage, server, password);

				SMTPConnection connection = new SMTPConnection(envelope);
				// connection.send(envelope);
				// connection.close();

				if (!ccField.getText().equals("")) {
					Envelope ccEnvelope = new Envelope(ccMessage, server, password);
					SMTPConnection ccConnection = new SMTPConnection(ccEnvelope);
					// ccConnection.send(ccEnvelope);
					// ccConnection.close();
				}
			} catch (IOException error) {
				System.out.println("Sending failed: " + error);
				return;
			}
			System.out.println("Mail sent succesfully!");
		}
	}

	/* Clear the fields on the GUI. */
	class ClearListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Clearing fields");
			// fromField.setText("");
			toField.setText("");
			subjectField.setText("");
			messageText.setText("");
		}
	}

	/* Quit. */
	class QuitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
}