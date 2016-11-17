package chatclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.json.simple.JSONObject;

import chatserver.Lists;
import chatserver.Room;
import chatserver.User;

public class SendMsg implements Runnable {
	private static SSLSocket socket;
	private User user = new User();
	
	
	//static ReadMes receiver = new ReadMes(socket);
	

	public SendMsg(SSLSocket socket) {
		this.socket = socket;
	}
	

	static DataOutputStream out;
	static Scanner keyboard = new Scanner(System.in);
	public static boolean stop ;

	
	public void run() {
		
     
		try {
			out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());

			String cmd = null;

			Thread.currentThread().sleep(300);

			
			while (true) {
				
				try {
					Thread.currentThread().sleep(200);
					if (ReadMes.getUserroom().equals(""))
						break;
					else {
						if (!stop) {
							System.out.printf("\n[" + ReadMes.getUserroom() + "] " + ReadMes.getUserID() + ">");
						}
					}
					if (!stop) {
						cmd = keyboard.nextLine();
					}
					if (!stop) {
						if (cmd.substring(0, 1).equals("#")) {
							String[] ss = cmd.split(" ");

							switch (ss[0]) {
							case "#identitychange":
								changeIdentity(ss);
								break;
							case "#join":
								joinRoom(ss);
								break;
							case "#who":
								roomContents(ss);
								break;
							case "#list":
								roomList(ss);
								break;
							case "#kick":
								kick(ss);
								break;
							case "#createroom":
								roomCreate(ss);
								break;
							case "#delete":
								roomDelete(ss);
								break;
							case "#quit":
								quit(ss);
								break;
							default:
								System.out.println("Invalid command!");
							}
						} else {
							message(cmd);
						}
					}
				} catch (SocketException e) {
					out.close();
					socket.close();
					System.out.println("Client close.");
					break;
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	
	
	/*------------------interrupr a thread to release the keyboard-------------------------------------*/
	/*----------------------------------------------------------------------------*/
	public static void inter() throws InterruptedException {
		// Thread.currentThread().sleep(3000);
		stop = true;
		// Thread.currentThread().sleep( 3000 );
	}

	public static void again() throws InterruptedException {
		stop = false;
	}

	public static boolean getStop() {
		return stop;
	}

	public static void setStop(boolean stop) {
		SendMsg.stop = stop;
	}

	public static SSLSocket getSocket() {
		return socket;
	}

	public static void setSocket(SSLSocket socket) {
		SendMsg.socket = socket;
	}

	public static void sendPsw(String a) throws IOException {

		JSONObject password = new JSONObject();

		password.put("type", "password");
		password.put("content", a);

		out.writeUTF(password.toString());
		out.flush();

	}

	
	
	 /*--------------------------Chnange Identity--------------------------------------*/
	   /*-------------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	private void changeIdentity(String[] cmd_array) throws IOException {
		JSONObject command = new JSONObject();
		if (cmd_array.length != 2)
			System.out.println("Invalid commmand!");
		else {
			command.put("type", "identitychange");
			command.put("identity", cmd_array[1]);
			
			String msg = command.toString();
			out.writeUTF(msg);
			out.flush();
		}
	}

	
	
	 /*--------------------------Join Room--------------------------------------*/
	 /*-------------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	private void joinRoom(String[] cmd_array) throws IOException {
		JSONObject command = new JSONObject();
		if (cmd_array.length != 2)
			System.out.println("Invalid commmand!");
		else {
			command.put("type", "join");
			command.put("roomid", cmd_array[1]);
			String msg = command.toString();

			out.writeUTF(msg);
			out.flush();
		}
	}

	
	
	
	 /*--------------------------RoomList-------------------------------------*/
	 /*-------------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	private void roomList(String[] cmd_array) throws IOException {
		JSONObject command = new JSONObject();
		if (cmd_array.length != 1)
			System.out.println("Invalid commmand!");
		else {
			command.put("type", "list");
			String msg = command.toString();

			out.writeUTF(msg);
			out.flush();
		}
	}

	@SuppressWarnings("unchecked")
	private void roomContents(String[] cmd_array) throws IOException {
		JSONObject command = new JSONObject();
		if (cmd_array.length != 2)
			System.out.println("Invalid commmand!");
		else {
			command.put("type", "who");
			command.put("roomid", cmd_array[1]);
			String msg = command.toString();

			out.writeUTF(msg);
			out.flush();
		}
	}

	
	
	 /*--------------------------Room Content--------------------------------------*/
	 /*-------------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	private void roomCreate(String[] cmd_array) throws IOException {
		JSONObject command = new JSONObject();
		if (cmd_array.length != 2)
			System.out.println("Invalid commmand!");
		else {
			command.put("type", "createroom");
			command.put("roomid", cmd_array[1]);
			String msg = command.toString();

			out.writeUTF(msg);
			out.flush();
		}
	}

	
	
	
	 /*--------------------------Delete Room --------------------------------------*/
	   /*-------------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	private void roomDelete(String[] cmd_array) throws IOException {
		JSONObject command = new JSONObject();
		if (cmd_array.length != 2)
			System.out.println("Invalid commmand!");
		else {
			command.put("type", "delete");
			command.put("roomid", cmd_array[1]);
			String msg = command.toString();

			out.writeUTF(msg);
			out.flush();
		}
	}

	
	
	
	 /*--------------------------Kick others--------------------------------------*/
	   /*-------------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	private void kick(String[] cmd_array) throws IOException {
		JSONObject command = new JSONObject();

		if (cmd_array.length != 4)
			System.out.println("Invalid reuqest!");

		else {
			command.put("type", "kick");
			command.put("identity", cmd_array[1]);
			command.put("roomid", cmd_array[2]);
			command.put("time", Integer.parseInt(cmd_array[3]));
			String msg = command.toString();

			out.writeUTF(msg);
			out.flush();
		}
	}

	
	
	
	 /*--------------------------Quit--------------------------------------*/
	   /*-------------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	private void quit(String[] cmd_array) throws IOException {
		JSONObject command = new JSONObject();
		if (cmd_array.length != 1)
			System.out.println("Invalid commmand!");
		else {
			command.put("type", "quit");

			String msg = command.toString();

			out.writeUTF(msg);
			out.flush();
		}
	}

	
	
	
	
	 /*--------------------------Chat Message--------------------------------------*/
	   /*-------------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	private void message(String command) throws IOException {
		JSONObject content = new JSONObject();
		content.put("type", "message");
		content.put("content", command);

		String msg = content.toString();

		out.writeUTF(msg);
		out.flush();
	}
}
