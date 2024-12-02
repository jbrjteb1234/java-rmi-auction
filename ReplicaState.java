import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface ReplicaState extends Remote{
    ConcurrentHashMap<Integer, AuctionItem> getItems() throws RemoteException;
    ArrayList<Integer> getUsers() throws RemoteException;
    ConcurrentHashMap<Integer, String> getEmails() throws RemoteException;
    ConcurrentHashMap<Integer, Integer> getCurrentLeaders() throws RemoteException;
    void set(ConcurrentHashMap<Integer, AuctionItem> items,
            ArrayList<Integer> users,
            ConcurrentHashMap<Integer, String> emails,
            ConcurrentHashMap<Integer, Integer> currentLeaders) throws RemoteException;
}
