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
	public int delay = 1000;

	public String user;
	public String password;

	/* Streams for reading and writing the socket */
	public BufferedReader fromServer;
	public DataOutputStream toServer;

	/* Just to make it look nicer */
	private static final int SMTP_PORT = 465;
	private static final String CRLF = "\r\n";

	/* Are we connected? Used in close() to determine what to do. */
	private boolean isConnected = false;

	/*
	 * Create an SMTPConnection object. Create the socket and the associated
	 * streams. Send HELO-command and check for errors.
	 */
	public SMTPConnection(Envelope envelope) throws IOException {
		user = Base64.getEncoder().encodeToString(envelope.Sender.getBytes());
		password = Base64.getEncoder().encodeToString(envelope.Password.getBytes());
		try {
		sock = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(envelope.DestAddr,
				SMTP_PORT);
		BufferedInputStream streamReader = new BufferedInputStream(sock.getInputStream());
		fromServer = new BufferedReader(new InputStreamReader(streamReader));

		toServer = new DataOutputStream(sock.getOutputStream());
		(new Thread(new Runnable() {
			public void run() {
				try {
					String line;
					while ((line = fromServer.readLine()) != null) {
						System.out.println(" SERVER: " + line);
					}
				} catch (Exception e) {
					System.out.println(" Client:Exit Reader...");
					try {
						fromServer.close();
						toServer.close();
						sock.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		})).start();

		/*
		 * connection = new Socket(envelope.DestAddr, SMTP_PORT); fromServer = new
		 * BufferedReader(new InputStreamReader( connection.getInputStream())); toServer
		 * = new DataOutputStream(connection.getOutputStream());
		 * 
		 * String reply = fromServer.readLine(); if (parseReply(reply) != 220) {
		 * System.out.println("Error in connect."); System.out.println(reply); return; }
		 * String localhost = (InetAddress.getLocalHost()).getHostName(); try {
		 * sendCommand("HELO " + localhost, 250); sendCommand("AUTH LOGIN",334);
		 * sendCommand(user,334); sendCommand(password,235); } catch (IOException e) {
		 * System.out.println("HELO failed. Aborting."); return; } isConnected = true;
		 * 
		 */
		
		this.send(envelope);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Send the message. Simply writes the correct SMTP-commands in the correct
	 * order. No checking for errors, just throw them to the caller.
	 */
	public void send(Envelope envelope) throws InterruptedException {
		try {
			sendCommand("EHLO smtp.gmail.com");
			sendCommand("AUTH LOGIN");
			sendCommand(user);
			sendCommand(password);
			sendCommand("MAIL FROM:<" + envelope.Sender + ">");
			sendCommand("RCPT TO:<" + envelope.Recipient + ">");
			sendCommand("DATA");
			sendCommand(envelope.Message.toString() + CRLF + ".");
			sendCommand("QUIT");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	/*
	 * Send an SMTP command to the server. Check for reply code. Does not check for
	 * multiple reply codes (required for RCPT TO).
	 */
	private void sendCommand(String command) throws InterruptedException {
		try {
			toServer.writeBytes(command + CRLF);
			toServer.flush();
			Thread.sleep(delay);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	

	
}