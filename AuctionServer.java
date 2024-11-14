import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;

public class AuctionServer extends UnicastRemoteObject implements Auction{

    private ConcurrentHashMap<Integer, AuctionItem> items;
    private ConcurrentHashMap<Integer, PublicKey> users;
    private ConcurrentHashMap<Integer, String> activeTokens;
    private KeyPair serverKeyPair;

    public AuctionServer() throws Exception {
        super(); //initates uro, binding to ports  etc. needed for RMI to function
        this.items = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.activeTokens = new ConcurrentHashMap<>();
    }   

    public int register(String email, PublicKey pkey) throws RemoteException{

    }
    
    public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException{

    }
    
    public TokenInfo authenticate(int userID, byte signature[]) throws RemoteException{

    }
    
    public AuctionItem getSpec(int userID, int itemID, String token) throws RemoteException{

    }
    
    public int newAuction(int userID, AuctionSaleItem item, String token) throws RemoteException{

    }
    
    public AuctionItem[] listItems(int userID, String token) throws RemoteException{

    }
    
    public AuctionResult closeAuction(int userID, int itemID, String token) throws RemoteException{

    }
    
    public boolean bid(int userID, int itemID, int price, String token) throws RemoteException{

    }

    public static void main(String[] args){
        try{
            AuctionServer server  = new AuctionServer();    //new server
            Naming.rebind("Auction", server);               //binds server to Auction
        }catch (Exception e) {
            System.out.println("Error creating server\n");
        }
    }
}
