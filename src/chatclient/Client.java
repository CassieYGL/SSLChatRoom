package chatclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import chatserver.Server;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class Client {

	private static SSLSocket sslSocket;

	public static void init() {
		CommandLineValues values = new CommandLineValues();
		CmdLineParser parser = new CmdLineParser(values);

		InputStream key = Server.class.getResourceAsStream("/chatclient/key/kclient.keystore");
		InputStream tkey = Server.class.getResourceAsStream("/chatclient/key/tclient.keystore");

		String CLIENT_KEY_STORE_PASSWORD = "123456";
		String CLIENT_TRUST_KEY_STORE_PASSWORD = "123456";

		try {
			SSLContext ctx = SSLContext.getInstance("SSL");

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

			KeyStore ks = KeyStore.getInstance("JKS");
			KeyStore tks = KeyStore.getInstance("JKS");

			ks.getClass().getResourceAsStream(CLIENT_KEY_STORE_PASSWORD);

			ks.load(key, CLIENT_KEY_STORE_PASSWORD.toCharArray());
			tks.load(tkey, CLIENT_TRUST_KEY_STORE_PASSWORD.toCharArray());

			kmf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());
			tmf.init(tks);

			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			sslSocket = (SSLSocket) ctx.getSocketFactory().createSocket(values.getHost(), values.getPort());

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) throws ParseException, InterruptedException, CmdLineException,
			UnknownHostException, IOException, NoSuchAlgorithmException {

		DataInputStream in;
		DataOutputStream out;
		Thread command;
		Thread system;

		init();
		in = new DataInputStream(sslSocket.getInputStream());
		out = new DataOutputStream(sslSocket.getOutputStream());

		System.out.println("Login as a user?");
		System.out.println("Please enter y/Y or n/N: ");

		int input_num = 3;
		boolean flag = false;
		Scanner keyboard = new Scanner(System.in);
		String res = keyboard.nextLine();

        while(!flag){
			if (res.equals("Y") || res.equals("y")) {
				flag=true;
				out.writeUTF(res);
				out.flush();
				System.out.println("Enter your username: ");
				res = keyboard.nextLine();
				out.writeUTF(res);
				out.flush();
				String input = in.readUTF();

				if (input.equals("Valid User.")) {
					System.out.println("Enter your password: ");
					res = keyboard.nextLine();
					String encode_psw = ReadMes.EncoderByMd5(res);
					out.writeUTF(encode_psw);
					out.flush();

					input = in.readUTF();

					if (input.equals("Valid Password.")) {
						System.out.println("Login successfully!");
					}
				}
				System.out.println("\nYou are not an existing user. Please jion as a guest.\n");
				system = new Thread(new ReadMes(sslSocket));
				system.start();
				command = new Thread(new SendMsg(sslSocket));
				command.start();

			} else if (res.equals("N") || res.equals("n")) {
				flag = true;
				out.writeUTF(res);
				out.flush();
				system = new Thread(new ReadMes(sslSocket));
				system.start();
				command = new Thread(new SendMsg(sslSocket));
				command.start();
			} else {
				System.out.println("\nInvalid input, please choose again. ");
				res = keyboard.nextLine();
				
			}

		}
	}

}
