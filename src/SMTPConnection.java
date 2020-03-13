import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

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

	/**
	 * Changed port number for ssl port: 465
	 */
	/* Just to make it look nicer */
	private static final int SMTP_PORT = 465;
	private static final String CRLF = "\r\n";

	/* Are we connected? Used in close() to determine what to do. */
	private boolean isConnected = false;

	/**
	 * Setup a ssl connection using sslsocket. Using thread to handle the output stream.
	 * @param envelope
	 * @throws IOException
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
			this.send(envelope);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A method doing all the commands of sending in ssl
	 * @param envelope
	 * @throws InterruptedException
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
			sendCommand(envelope.Message.toString());
			sendCommand( CRLF + ".");
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