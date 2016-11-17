package chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class Server {
	
	private static Lists lists = new Lists();
	private static SSLServerSocket serverSocket;
	

	public static void init() {

		CommandLineValues values = new CommandLineValues();
		CmdLineParser parser = new CmdLineParser(values);

		String SERVER_KEY_STORE_PASSWORD = "123456";
		String SERVER_TRUST_KEY_STORE_PASSWORD = "123456";
		

		InputStream key = Server.class.getResourceAsStream("/chatserver/key/kserver.keystore");
		InputStream tkey = Server.class.getResourceAsStream("/chatserver/key/tserver.keystore");

		try {
			SSLContext ctx = SSLContext.getInstance("SSL");

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

			KeyStore ks = KeyStore.getInstance("JKS");
			KeyStore tks = KeyStore.getInstance("JKS");

			ks.load(key, SERVER_KEY_STORE_PASSWORD.toCharArray());
			tks.load(tkey, SERVER_TRUST_KEY_STORE_PASSWORD.toCharArray());

			kmf.init(ks, SERVER_KEY_STORE_PASSWORD.toCharArray());
			tmf.init(tks);

			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			serverSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(values.getPort());
			serverSocket.setNeedClientAuth(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException, CmdLineException {
		// parser.parseArgument(args);
		init();
	     DataInputStream in;
		 DataOutputStream out;
		User admin = new User();
		Room mainhall = new Room();
		mainhall.setId("MainHall");
		mainhall.setOwner(admin);
		admin.setRoom(mainhall);
		lists.getAllUsers().add(admin);
		lists.getAllRooms().add(mainhall);

		try {

			System.out.println("Server is listening...");
			for (;;) {
				SSLSocket socket = (SSLSocket) serverSocket.accept();
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				String input = in.readUTF();
				boolean finish = false;
				boolean flag = false;

				if (input.equals("Y") || input.equals("y")) {
					User AC_user = null;
					int size = lists.getAuthenticatedID().size();

					input = in.readUTF();
					if (size != 0) {
						for (int i = 0; i < lists.getAuthenticatedID().size(); i++) {
							if (lists.getAuthenticatedID().get(i).getId().equals(input)) {
								AC_user = new User(lists.getAuthenticatedID().get(i));
								flag = true;
								out.writeUTF("Valid User.");
								out.flush();

							}
						}

						if (flag) {
							input = in.readUTF();

							if (AC_user.getPassword().equals(input)) {
								out.writeUTF("Valid Password.");
								out.flush();
								Thread client = new Thread(new ConnectThread(socket, lists, AC_user, 1));
								client.start();
							} else {
								out.writeUTF("Invalid Password.");
								out.flush();
								// in.close();
								// out.close();
								Thread client = new Thread(new ConnectThread(socket, lists));
								client.start();
							}
						} else {
							out.writeUTF("Invalid User.");
							out.flush();
							Thread client = new Thread(new ConnectThread(socket, lists));
							client.start();
						}
					} else {
						out.writeUTF("Invalid User.");
						out.flush();
						Thread client = new Thread(new ConnectThread(socket, lists));
						client.start();
					}
				} else {
					Thread client = new Thread(new ConnectThread(socket, lists));
					client.start();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (serverSocket != null) {
				serverSocket.close();
			}

		}

	}
}
