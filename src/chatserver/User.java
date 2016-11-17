package chatserver;

import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.net.ssl.SSLSocket;

public class User {
	
	private String id;
	private Room room;
	private ArrayList<Room> hasOwned = new ArrayList<Room>();
	private Hashtable<Room,Long> permission = new Hashtable<Room,Long>();		
	private SSLSocket socket;
	String uniqueID;
	private String password;
	
	public User(){
		
	}
	
	
	public User(User user){
		this.id = user.getId();
		this.room = user.getRoom();
		this.hasOwned= user.getOwned();
		this.permission = user.getPermission();
		this.password = user.getPassword();
	}
	
	

	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}
	private Key joinKey;
	///owner同意加入房间成功之后 被分配一个private key。private key同事添加到对应房间的key list里面
	
	public void joinKey(Key key){
		this.joinKey=key;
	}

	public String getId() {
		return id;
	}
	public void setID(String id) {
		this.id = id;
	}
	public Room getRoom() {
		return room;
	}
	public void setRoom(Room room) {
		this.room = room;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(SSLSocket socket) {
		this.socket = socket;
	}
	public ArrayList<Room> getOwned() {
		return hasOwned;
	}
	public void setOwned(ArrayList<Room> owned) {
		this.hasOwned = owned;
	}
	public Hashtable<Room,Long> getPermission() {
		return permission;
	}
	public void setPermission(Hashtable<Room,Long> permission) {
		this.permission = permission;
	}
		
}

