package chatserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;

import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLServerSocket;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ConnectThread implements Runnable {

	private SSLSocket socket;
	private Lists lists;
	private User user ;

	JSONParser parser = new JSONParser();
	InputStream inputstream;
	DataInputStream in;
	DataOutputStream out;
	Date nowTime;
	int ac;

	public ConnectThread(SSLSocket socket, Lists database, User user, int i) {
		this.socket = socket;
		this.lists = database;
		this.user = new User( user);
		ac = i;
	}

	public ConnectThread(SSLSocket socket, Lists database) {
		this.socket = socket;
		this.lists = database;
		user = new User();
		ac = 0;
	}

	@Override
	public void run() {
		try {

			in = new DataInputStream(socket.getInputStream());

			out = new DataOutputStream(socket.getOutputStream());

			initialUser();
			
			for(int m=0;m<lists.getAuthenticatedID().size();m++){
				System.out.println("ACLIST>>>>"+lists.getAuthenticatedID().get(m).getId());
			}

			while (true) {
				String msg = in.readUTF();
				JSONObject obj = (JSONObject) parser.parse(msg);
				String type = (String) obj.get("type");
				switch (type) {
				case "identitychange":
					changeIdentity(obj);
					break;
				case "join":
					joinRoom(obj);
					break;
				case "createroom":
					createRoom(obj);
					break;
				case "delete":
					deleteRoom(obj);
					break;
				case "who":
					roomContents(obj);
					break;
				case "list":
					roomList();
					break;
				case "kick":
					kickUser(obj);
					break;
				case "message":
					broadToOthers(obj);
					break;
				case "quit":
					quit();
					break;
				case "password":
					createPsw(obj);
					break;
				default:
					System.out.println("Invalid request.");
				}
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
			try {
				quit();
			} catch (IOException exit) {
				exit.printStackTrace();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void initialUser() throws IOException, InterruptedException {
		String id;
		if (ac == 0) {
			id = "guest" + this.lists.getCounter();
			this.user.setID(id);
		} else {
			id = this.user.getId();
		}

		this.user.setRoom((Room) this.lists.getAllRooms().get(0));

		this.user.setSocket(this.socket);
		this.lists.setCounter(this.lists.getCounter() + 1);
		for (Room each : this.lists.getAllRooms()) {
			if (each.getId().equals("MainHall")) {
				each.getUsers().add(user);
				break;
			}
		}

		this.lists.getAllUsers().add(this.user);

		JSONObject newIdentity = new JSONObject();
		newIdentity.put("type", "newidentity");
		newIdentity.put("former", "");
		newIdentity.put("identity", id);
		String msg = newIdentity.toString();

		out.writeUTF(msg);
		out.flush();

		roomList();

		JSONObject roomChange = new JSONObject();
		roomChange.put("type", "roomchange");
		roomChange.put("identity", this.user.getId());
		roomChange.put("former", "");
		roomChange.put("roomid", this.user.getRoom().getId());
		String roommsg = roomChange.toString();
		out.writeUTF(roommsg);
		out.flush();

		JSONObject roomContents = new JSONObject();
		roomContents.put("roomid", this.user.getRoom().getId());
		roomContents(roomContents);

	}

	/*--------------------Create password--------------------------------------*/
	/*-------------------------------------------------------------------------------*/
	public void createPsw(JSONObject object) throws IOException {
		String s = (String) object.get("content");
		user.setPassword(s);                        //digest password
		lists.getAuthenticatedID().add(this.user);

		JSONObject obj = new JSONObject();
		obj.put("type", "password");
		obj.put("content", "success");
		out.writeUTF(obj.toString());
		out.flush();
	}

	/*--------------------Check if UserID exits--------------------------------------*/
	/*-------------------------------------------------------------------------------*/
	public String IDExist(String id) {
		String already = null;
		for (int i = 0; i < lists.getAllUsers().size(); i++) {
			if (lists.getAllUsers().get(i).getId().equals(id)) {
				already = lists.getAllUsers().get(i).getId();
				break;
			}

		}
		return already;
	}

	/*--------------------------Check Invalid UserID------------------------------------*/
	/*--------------------------------------------------------------------*/
	public boolean checkID(String id) {
		boolean res = false;
		String format = "^[A-Za-z][A-Za-z0-9]{2,15}";
		Pattern pattern = Pattern.compile(format);
		Pattern pat = Pattern.compile(format, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(id);
		res = matcher.matches();

		return res;
	}

	/*--------------------------Identity Change--------------------------------------*/
	/*--------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	public synchronized void changeIdentity(JSONObject object) throws IOException {
		JSONObject obj = new JSONObject();
		String newid = (String) object.get("identity");
		// String name = (String) object.get("now");

		boolean find = false;
		for (int i = 0; i < lists.getAuthenticatedID().size(); i++) {
			if (lists.getAuthenticatedID().get(i).getId().equals(this.user.getId()))
				find = true;
			// break;
		}

		if (find) {
			boolean flag = false;
			for (int i = 1; i < lists.getAllUsers().size(); i++) {
				if (lists.getAllUsers().get(i).getId().equals(newid))
					flag = true;
				break;
			}

			if (checkID(newid) && flag == false) {

				obj.put("type", "newidentity");
				obj.put("former", user.getId());
				obj.put("identity", newid);
				String msg = obj.toString();

				for (int i = 1; i < lists.getAllUsers().size(); i++) {
					DataOutputStream temp = new DataOutputStream(
							lists.getAllUsers().get(i).getSocket().getOutputStream());
					temp.writeUTF(msg);
					temp.flush();
				}

				user.setID(newid);
				this.lists.quitCounter();

			} else {

				obj.put("type", "newidentity");
				obj.put("former", user.getId());
				obj.put("identity", user.getId());
				out.writeUTF(obj.toString());
				out.flush();
			}
		} else {

			obj.put("type", "register");
			out.writeUTF(obj.toString());
			out.flush();

		}
		System.out.println("objjjjjjjjjjjj" + obj);

	}

	/*------------------Generate key for the client when join a room--------------------------------------*/
	/*-------------------------------------------------------------- -------------*/
	public Key genPvKey() throws NoSuchAlgorithmException {
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		SecureRandom random = new SecureRandom();
		keygen.init(random);
		Key key = keygen.generateKey();

		for (User each : lists.getAllUsers()) {
			if (each.equals(user)) {
				user.joinKey(key);
				break;
			}
		}
		return key;
	}

	/*--------------------------Join--------------------------------------*/
	/*--------------------------------------------------------------------*/
	public synchronized void joinRoom(JSONObject object) throws IOException, NoSuchAlgorithmException {
		JSONObject obj = new JSONObject();
		String roomid = (String) object.get("roomid");
		String oldroom = user.getRoom().getId();
		Room new_room = null;
		boolean flag1 = false;
		boolean flag2 = false;

		for (int i = 0; i < lists.getAllRooms().size(); i++)
			if (lists.getAllRooms().get(i).getId().equals(roomid) && !(roomid.equals(oldroom))) {
				flag1 = true;
				new_room = lists.getAllRooms().get(i);
			}

		nowTime = new Date();
		long check = nowTime.getTime();

		if (user.getPermission().size() == 0)
			flag2 = true;
		else if (!user.getPermission().containsKey(new_room))
			flag2 = true;
		else if (check >= user.getPermission().get(new_room))
			flag2 = true;

		if (flag1 && flag2) {

			obj.put("type", "roomchange");
			obj.put("identity", user.getId());
			obj.put("former", user.getRoom().getId());
			obj.put("roomid", roomid);

			String msg = obj.toString();

			for (int i = 0; i < lists.getAllRooms().size(); i++) {

				if (lists.getAllRooms().get(i).getId().equals(oldroom))
					for (int j = 0; j < lists.getAllRooms().get(i).getUsers().size(); j++)
						if (lists.getAllRooms().get(i).getUsers().get(j).equals(user))
							lists.getAllRooms().get(i).getUsers().remove(j);
				lists.getAllRooms().get(i).getKeyList().add(genPvKey());

				if (lists.getAllRooms().get(i).getId().equals(roomid)) {
					user.setRoom(lists.getAllRooms().get(i));
					lists.getAllRooms().get(i).getUsers().add(user);
				}
			}

			for (int i = 1; i < this.lists.getAllUsers().size(); i++)
				if (lists.getAllUsers().get(i).getRoom().getId().equals(roomid)
						|| lists.getAllUsers().get(i).getRoom().getId().equals(oldroom)) {
					DataOutputStream temp = new DataOutputStream(
							lists.getAllUsers().get(i).getSocket().getOutputStream());
					temp.writeUTF(msg);
					temp.flush();
				}
			if (roomid.equals("MainHall")) {

				// JSONObject roomContents = new JSONObject();
				obj.put("roomid", "MainHall");
				roomContents(obj);
				roomList();
			}
		} else {
			// JSONObject roomChange = new JSONObject();
			obj.put("type", "roomchange");
			obj.put("identity", user.getId());
			obj.put("former", user.getRoom().getId());
			obj.put("roomid", user.getRoom().getId());
			String msg = obj.toString();
			out.writeUTF(msg);
			out.flush();
		}
	}

	/*--------------------------CheckRoomID--------------------------------------*/
	/*----------------------------------------------------------------------------*/
	public boolean checkRoomID(String id) {

		boolean res;
		String format = "^[A-Za-z][A-Za-z0-9]{2,31}";
		Pattern pattern = Pattern.compile(format);
		Pattern pat = Pattern.compile(format, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(id);
		res = matcher.matches();

		return res;

	}

	public boolean roomExist(String id) {
		boolean res = false;
		for (int i = 0; i < lists.getAllRooms().size(); i++) {

			if (lists.getAllRooms().get(i).getId().equals(id)) {
				res = true;
				break;
			}

		}
		return res;
	}

	/*--------------------------CreateRoom--------------------------------------*/
	/*---------------------------------------------------------------------------*/
	public synchronized void createRoom(JSONObject object) throws IOException {
		String roomid = (String) object.get("roomid");

		if (!roomExist(roomid) && checkRoomID(roomid)) {
			Room newroom = new Room();
			newroom.setId(roomid);
			newroom.setOwner(user);

			// 加密算法生成此房间的public key
			user.getOwned().add(newroom);
			lists.getAllRooms().add(newroom);
			roomList();
		} else {
			roomList();
		}
	}

	/*--------------------------ChangeRoom--------------------------------------*/
	/*--------------------------------------------------------------------*/
	public synchronized void changeRoom(JSONObject object) throws IOException {
		String roomid = (String) object.get("roomid");
		String former = (String) object.get("former");
		String identity = (String) object.get("identity");
		String msg = object.toString();
		Room temp1 = new Room();
		User temp2 = new User();
		Room temp3 = new Room();
		User temp4 = new User();
		for (int i = 0; i < lists.getAllRooms().size(); i++) {
			if (lists.getAllRooms().get(i).getId().equals(roomid)) {
				temp1 = lists.getAllRooms().get(i);
			}
		}
		for (int j = 1; j < lists.getAllUsers().size(); j++) {
			if (lists.getAllUsers().get(j).getId().equals(identity)) {

				temp2 = lists.getAllUsers().get(j);
				break;
			}
		}

		temp1.getUsers().add(temp2);
		temp2.setRoom(temp1);
		for (int i = 0; i < lists.getAllRooms().size(); i++) {
			if (lists.getAllRooms().get(i).getId().equals(former)) {
				temp3 = lists.getAllRooms().get(i);
			}

		}
		temp3.getUsers().remove(temp2);

		for (int i = 1; i < lists.getAllUsers().size(); i++)
			if ((lists.getAllUsers().get(i).getRoom().getId().equals(roomid)
					|| lists.getAllUsers().get(i).getRoom().getId().equals(former))
					&& !lists.getAllUsers().get(i).getSocket().isClosed())

			{
				DataOutputStream temp = new DataOutputStream(lists.getAllUsers().get(i).getSocket().getOutputStream());
				try {
					temp.writeUTF(msg);
					temp.flush();
				} catch (IOException e) {
					System.out.printf("Client \"%s\" is disconnected.\n", identity);
				}
			}
	}

	/*--------------------------DeleteRoom--------------------------------------*/
	/*--------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	public synchronized void deleteRoom(JSONObject object) throws IOException {
		String roomid = (String) object.get("roomid");
		boolean flag = false;
		// User temp = null;
		// boolean isACed = false;
		//
		// for (int i = 0; i < lists.getAllRooms().size(); i++) {
		//
		// if (lists.getAllRooms().get(i).getId().equals(roomid)) {
		// temp = lists.getAllRooms().get(i).getOwner();
		// }
		//
		// for (int j = 1; j < lists.getAuthenticatedID().size(); j++) {
		// if (lists.getAuthenticatedID().get(j).getId().equals(temp.getId())) {
		// isACed = true;
		// }
		// }
		// }

		// if (!isACed) {
		for (int i = 0; i < user.getOwned().size(); i++)
			if (user.getOwned().get(i).getId().equals(roomid)) {
				flag = true;

				while (user.getOwned().get(i).getUsers().size() != 0) {
					JSONObject obj = new JSONObject();
					obj.put("type", "roomchange");
					obj.put("identity", user.getOwned().get(i).getUsers().get(0).getId());
					obj.put("former", roomid);
					obj.put("roomid", "MainHall");
					changeRoom(obj);
				}
				user.getOwned().remove(i);
				for (int k = 0; k < lists.getAllRooms().size(); k++)
					if (lists.getAllRooms().get(k).getId().equals(roomid))
						lists.getAllRooms().remove(k);
				// roomList();
			}
		// if (!roomExist(roomid))
		// roomList();
	}
	// }

	/*--------------------------ShowMembers--------------------------------------*/
	/*--------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	public synchronized void roomContents(JSONObject object) throws IOException {
		String roomid = (String) object.get("roomid");
		String owner = null;
		JSONArray members = new JSONArray();

		for (int i = 0; i < lists.getAllRooms().size(); i++)
			if (lists.getAllRooms().get(i).getId().equals(roomid)) {
				owner = lists.getAllRooms().get(i).getOwner().getId();
				for (int j = 0; j < lists.getAllRooms().get(i).getUsers().size(); j++)
					members.add(lists.getAllRooms().get(i).getUsers().get(j).getId());
				JSONObject obj = new JSONObject();
				obj.put("type", "roomcontents");
				obj.put("roomid", roomid);
				obj.put("identities", members);
				obj.put("owner", owner);

				out.writeUTF(obj.toString());
				out.flush();
			}
	}

	/*--------------------------RoomList--------------------------------------*/
	/*--------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	public synchronized void roomList() throws IOException {

		JSONArray rooms = new JSONArray();

		for (int i = 0; i < lists.getAllRooms().size(); i++) {
			JSONObject obj = new JSONObject();
			obj.put("roomid", lists.getAllRooms().get(i).getId());
			obj.put("count", lists.getAllRooms().get(i).getUsers().size());
			rooms.add(obj);

		}
		JSONObject roomList = new JSONObject();
		roomList.put("type", "roomlist");
		roomList.put("rooms", rooms);
		try {
			out.writeUTF(roomList.toString());
			out.flush();
		} catch (IOException e) {
			System.out.println("Client " + user.getId() + " is disconnected.");
		}

	}

	/*--------------------------KickOther--------------------------------------*/
	/*--------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	public synchronized void kickUser(JSONObject object) throws IOException {
		String identity = (String) object.get("identity");
		String roomid = (String) object.get("roomid");
		long time = (Long) object.get("time");
		JSONObject obj = new JSONObject();
		boolean flag = false;
		boolean validroom = false;
		boolean flag1 = false;
		boolean flag2 = false;
		User kick_guest = new User();
		Room kick_room = new Room();
		
		
		for(int i=1;i<lists.getAllRooms().size();i++){
			if(lists.getAllRooms().get(i).getId().equals(roomid)){
				validroom=true;
				break;
			}
		}
		
		System.out.println("room is valid>>>"+ validroom);

		for (int j=0;j<lists.getAuthenticatedID().size();j++) {
			if (lists.getAuthenticatedID().get(j).getId().equals(user.getId()))
				flag = true;
			   break;
		}
		System.out.println("ACUSER>>>>"+ flag);
		
		if (flag&&validroom) {
			for (int i = 0; i < user.getOwned().size(); i++)
				if (user.getOwned().get(i).getId().equals(roomid)) {
					flag1 = true;
					kick_room = user.getOwned().get(i);
                    
					System.out.println("has this room>>>>"+ flag1);
					
					
					for (int j = 0; j < kick_room.getUsers().size(); j++)
						if (kick_room.getUsers().get(j).getId().equals(identity)) {
							flag2 = true;
							kick_guest = kick_room.getUsers().get(j);
							break;
						}
					
					System.out.println("invalid kicker>>>>"+ flag2);
				}

			if (flag1 && flag2) {
				nowTime = new Date();
				long end = nowTime.getTime() + time * 1000;
				kick_guest.getPermission().put(kick_room, end);

				obj.put("type", "roomchange");
				obj.put("identity", identity);
				obj.put("former", roomid);
				obj.put("roomid", "MainHall");
				changeRoom(obj);
			}
		}

	}

	/*--------------------------BroadMessage--------------------------------------*/
	/*-----------------------------------------------------------------------------*/
	@SuppressWarnings("unchecked")
	public synchronized void broadToOthers(JSONObject object) throws IOException {
		String content = (String) object.get("content");

		JSONObject obj = new JSONObject();
		obj.put("type", "message");
		obj.put("identity", user.getId());
		obj.put("content", content);

		for (int i = 0; i < user.getRoom().getUsers().size(); i++) {
			DataOutputStream broad = new DataOutputStream(
					user.getRoom().getUsers().get(i).getSocket().getOutputStream());

			broad.writeUTF(obj.toString());
			broad.flush();
		}
	}

	/*--------------------------Quit--------------------------------------*/
	/*--------------------------------------------------------------------*/
	public synchronized void quit() throws IOException {
		JSONObject obj = new JSONObject();
		// obj.put("type", "roomchange");
		// obj.put("identity", user.getId());
		// obj.put("former", user.getRoom().getId());
		// obj.put("roomid", "");
		// changeRoom(obj);
		boolean isACed = false;
		// User temp = null;

		if (user.getOwned().size() != 0) {

			JSONObject obj1 = new JSONObject();
			obj1.put("type", "delete");
			obj1.put("roomid", user.getOwned().get(0).getId());

			for (int j = 0; j < lists.getAuthenticatedID().size(); j++) {
				if (lists.getAuthenticatedID().get(j).getId().equals(user.getId())) {
					isACed = true;
				}
			}
			if (!isACed) {
				deleteRoom(obj1);
			}
		}

		for (Room each : lists.getAllRooms()) {
			if (each.getId().equals(user.getRoom().getId())) {
				each.getUsers().remove(user);
			}

		}

		for (int j = 1; j < lists.getAllUsers().size(); j++)
			if (lists.getAllUsers().get(j).equals(user)) {
				lists.getAllUsers().remove(user);
				lists.setCounter(j);
			}

		in.close();
		out.close();
		socket.close();
	}
}