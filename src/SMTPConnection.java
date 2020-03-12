import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

import javax.net.ssl.*;

/* $Id: SMTPConnection.java,v 1.1.1.1 2003/09/30 14:36:01 kangasha Exp $ */

/**
 * Open an SMTP connection to a remote machine and send one mail.
 * 
 * @author Jussi Kangasharju
 */
public class SMTPConnection {
	/* The socket to the server */
	public Socket connection;
	public SSLSocket sock;

	public String user;
	public String password;

	/* Streams for reading and writing the socket */
	public BufferedReader fromServer;
	public DataOutputStream toServer;

	/* Just to make it look nicer */
	private static final int SMTP_PORT = 587;
	private static final String CRLF = "\r\n";

	/* Are we connected? Used in close() to determine what to do. */
	private boolean isConnected = false;

	/*
	 * Create an SMTPConnection object. Create the socket and the associated
	 * streams. Send HELO-command and check for errors.
	 */
	public SMTPConnection(Envelope envelope) throws IOException {
		user =Base64.getEncoder().encodeToString(envelope.Sender.getBytes());
		password = Base64.getEncoder().encodeToString(envelope.Password.getBytes());
		sock = (SSLSocket)((SSLSocketFactory)SSLSocketFactory.getDefault()).createSocket(envelope.DestAddr, SMTP_PORT);
		fromServer = new BufferedReader(new InputStreamReader(
				sock.getInputStream()));
		toServer = new DataOutputStream(sock.getOutputStream());
		/*
		connection = new Socket(envelope.DestAddr, SMTP_PORT);
		fromServer = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		toServer = new DataOutputStream(connection.getOutputStream());
		 */
		String reply = fromServer.readLine();
		if (parseReply(reply) != 220) {
			System.out.println("Error in connect.");
			System.out.println(reply);
			return;
		}
		String localhost = (InetAddress.getLocalHost()).getHostName();
		try {
			sendCommand("HELO " + localhost, 250);			
			sendCommand("AUTH LOGIN"+CRLF,334);
			sendCommand(user+CRLF,334);
			sendCommand(password+CRLF,235);
		} catch (IOException e) {
			System.out.println("HELO failed. Aborting.");
			return;
		}
		isConnected = true;
	}

	/*
	 * Send the message. Simply writes the correct SMTP-commands in the correct
	 * order. No checking for errors, just throw them to the caller.
	 */
	public void send(Envelope envelope) throws IOException {
		sendCommand("MAIL FROM:<" + envelope.Sender + ">", 250);
		sendCommand("RCPT TO:<" + envelope.Recipient + ">", 250);
		sendCommand("DATA", 354);
		sendCommand(envelope.Message.toString() + CRLF + ".", 250);
	}

	/*
	 * Close the connection. Try to send QUIT-commmand and then close the socket.
	 */
	public void close() {
		isConnected = false;
		try {
			sendCommand("QUIT", 221);
			connection.close();
		} catch (IOException e) {
			System.out.println("Unable to close connection: " + e);
			isConnected = true;
		}
	}

	/*
	 * Send an SMTP command to the server. Check for reply code. Does not check for
	 * multiple reply codes (required for RCPT TO).
	 */
	private void sendCommand(String command, int rc) throws IOException {
		String reply = null;

		toServer.writeBytes(command + CRLF);
		reply = fromServer.readLine();
		if (parseReply(reply) != rc) {
			System.out.println("Error in command: " + command);
			System.out.println(reply);
			throw new IOException();
		}

	}

	/* Parse the reply line from the server. Returns the reply code. */
	private int parseReply(String reply) {
		StringTokenizer parser = new StringTokenizer(reply);
		String replycode = parser.nextToken();
		return (new Integer(replycode)).intValue();
	}

	/* Destructor. Closes the connection if something bad happens. */
	protected void finalize() throws Throwable {
		if (isConnected) {
			close();
		}
		super.finalize();
	}
}