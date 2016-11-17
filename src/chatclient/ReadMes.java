package chatclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import chatclient.SendMsg;
import sun.misc.BASE64Encoder;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import chatclient.SendMsg;
import chatclient.Client;


public class ReadMes implements Runnable {
	private SSLSocket socket;
	private static String userID = new String();
	private static String userRoom = new String();
	JSONParser parser = new JSONParser();
	DataInputStream in;
	
	public static String getUserID() {
		return userID;
	}

	public static String getUserroom() {
		return userRoom;
	}

	public ReadMes(SSLSocket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {

			while (true) {
			
				in = new DataInputStream(socket.getInputStream());
				String response = in.readUTF();
				JSONParser parser = new JSONParser();
				JSONObject jsMSG = (JSONObject) parser.parse(response);
				String type = (String) jsMSG.get("type");
			 	
				try {
					switch (type) {
					case "newidentity":
						newIdentity(jsMSG);
						break;
					case "roomchange":
						roomChange(jsMSG);
						break;
					case "roomlist":
						roomList(jsMSG);
						break;
					case "roomcontents":
						roomContents(jsMSG);
						break;
					case "message":
						broadMessage(jsMSG);
						break;
					case "register":
						System.out.println("\nYou haven't been authenticated! ");
						System.out.println("Please register first.");								
						SendMsg.inter();
						register();						
						break;
					case "password":	
						if( ((String) jsMSG.get("content")).equals("success"))
						System.out.println("****** Congradulations! ******");	
						System.out.println("\n***** Welcome to join us! *****\n");
						SendMsg.again();
						break;
					default:
						System.out.println("Invalid input!");
						break;
					}
				} catch (SocketException | InterruptedException | NoSuchAlgorithmException e) {
					in.close();
					socket.close();
					System.out.println("Disconnected from server.");
					userRoom = "";
					break;
				}
			}
		} catch (IOException | ParseException e) {
			System.out.println("Disconnected from server.");
			userRoom = "";
		}

	}
	
	

   public static void register() throws IOException, InterruptedException, NoSuchAlgorithmException{

		Scanner keyboard = new Scanner(System.in);
		System.out.println("\nNow Create your password: ");
		String psw1 ;
		String psw2;	
		boolean flag=false;
		psw1 = keyboard.nextLine();
		
		System.out.println("Enter the same password again: ");		
		psw2 = keyboard.nextLine();	  
		while(!flag){
		if(psw1.equals(psw2)){
			String newstr=EncoderByMd5(psw2);			
	        SendMsg.again();
		    SendMsg.sendPsw(newstr);
		    flag = true;
		    //break;
	        //SendMsg.sendPsw(psw2);
		}	else{
			System.out.println("\nPlease enter the same password:");
			psw2= keyboard.nextLine();
		}
	}
   }	
	 /*--------------------------Encoding user paasowrd--------------------------------------*/
   /*------------------------------------------------------------------------------*/

   public static String EncoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException{
	   
	           MessageDigest md5=MessageDigest.getInstance("MD5");
	           BASE64Encoder base64en = new BASE64Encoder();
	   
	           String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
	   return newstr;
	       }

   
	 /*--------------------------New Identity--------------------------------------*/
    /*--------------------------------------------------------------------*/

	private void newIdentity(JSONObject decodeMSG) {
		String former = (String) decodeMSG.get("former");
		String identity = (String) decodeMSG.get("identity");
	
		
		
		if (former.equals("") || former.equals(null)) {
			userID = identity;
			System.out.println("Client Connected as " + identity + "...");
		} else if (!former.equals(identity)&&!former.equals("")&&former.equals(getUserID())) {
			System.out.println();
			System.out.println(former + " is now " + identity);
			userID = identity;
		} else if(!former.equals(identity)&&!former.equals("")&&!former.equals(getUserID())){
			System.out.println();
			System.out.println(former + " is now " + identity);		
		}	
		else {
			System.out.println();
			System.out.println("Invalid request or identity in used.");
		}
		}
	

	 /*--------------------------RoomChange--------------------------------------*/
   /*-------------------------------------------------------------------------*/
	private void roomChange(JSONObject decodeMSG) throws IOException {
		String identity = (String) decodeMSG.get("identity");
		String former = (String) decodeMSG.get("former");
		String roomid = (String) decodeMSG.get("roomid");
		if (identity.equals(userID)) {
			if (roomid.equals("") || roomid.equals(null)) {
				userRoom = "";
				this.in.close();
			} else if (former.equals("") || former.equals(null)) {
				userRoom = roomid;
				System.out.println();
				System.out.println(identity + " moved to MainHall.");
			} 
			else if (!former.equals(roomid)) {
				userRoom = roomid;
				System.out.println();
				System.out.println(identity + " moved form " + former + " to " + roomid);
			} else {
				System.out.println();
				System.out.println("Request invalid or non existing room.");
			}
		} else {
			if (!roomid.equals("")) {
				System.out.println();
				System.out.println(identity + " moved form " + former + " to " + roomid);
			}

		}
	}
	
	
	
	

	 /*--------------------------RoomList--------------------------------------*/
   /*--------------------------------------------------------------------*/
	private void roomList(JSONObject decodeMSG) {
		JSONArray rooms = (JSONArray) decodeMSG.get("rooms");
		JSONObject obj = new JSONObject();
		for (int i = 0; i < rooms.size(); i++) {
			obj = (JSONObject) rooms.get(i);
			System.out.println((String) obj.get("roomid") + ": " + (Long) obj.get("count") + " guests.");
		}
	}
	
	
	
	

	 /*--------------------------RoomContents--------------------------------------*/
   /*--------------------------------------------------------------------*/
	private void roomContents(JSONObject decodeMSG) {
		String roomid = (String) decodeMSG.get("roomid");
		String owner = (String) decodeMSG.get("owner");
		JSONArray members = (JSONArray) decodeMSG.get("identities");

		System.out.println(roomid + " contains ");

		for (int i = 0; i < members.size(); i++)
			if (members.get(i).equals(owner))
				System.out.print(members.get(i) + "* ");
			else
				System.out.printf(members.get(i) + " ");
		System.out.println();
	}

	

	 /*--------------------------Other's Message--------------------------------------*/
   /*--------------------------------------------------------------------*/
	private void broadMessage(JSONObject decodeMSG) {
		String identity = (String) decodeMSG.get("identity");
		String content = (String) decodeMSG.get("content");
		if (!identity.equals(userID)) {
			System.out.println();
			System.out.println(identity + ": " + content);
		} else {
			System.out.println(identity + ": " + content);

		}
	}
}
