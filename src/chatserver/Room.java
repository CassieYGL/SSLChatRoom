package chatserver;

import java.security.Key;
import java.util.ArrayList;

public class Room {
	
	private String id;
	private User owner;
	private ArrayList<User> users = new ArrayList<User>();
	private String roomPublicKey;
	private ArrayList<Key> keyList = new ArrayList<Key>();
	
	public ArrayList<Key> getKeyList() {
		return keyList;
	}
	public void setKeyList(ArrayList<Key> keyList) {
		this.keyList = keyList;
	}

	public String getRoomPublicKey() {
		return roomPublicKey;
	}
	public void setRoomPublicKey(String roomPublicKey) {
		this.roomPublicKey = roomPublicKey;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	public ArrayList<User> getUsers() {
		return users;
	}
	public void setUsers(ArrayList<User> users) {
		this.users = users;
	}
	
	

}

