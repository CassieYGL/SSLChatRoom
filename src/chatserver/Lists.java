package chatserver;
import java.util.ArrayList;

public class Lists {
	private ArrayList<User> allUsers = new ArrayList<User>();
	private static ArrayList<Room> allRooms=new ArrayList<Room>();	
	private int counter=1;
	private ArrayList<User> authenticatedID = new ArrayList<User>();
	
	public ArrayList<User> getAllUsers() {
		return allUsers;
	}

	public ArrayList<User> getAuthenticatedID() {
		return authenticatedID;
	}

	public void setAuthenticatedID(ArrayList<User> authenticatedID) {
		this.authenticatedID = authenticatedID;
	}

	public static ArrayList<Room> getAllRooms() {
		return allRooms;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int i) {
		this.counter =i;
	}
	
	public void quitCounter(){
		this.counter-=1;
	}
	
}

