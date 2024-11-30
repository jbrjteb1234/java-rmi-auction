import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.io.Serializable;

public class Replica extends UnicastRemoteObject implements Auction {

    private int id;

    private ConcurrentHashMap<Integer, AuctionItem> items;
    private ArrayList<Integer> users;
    private ConcurrentHashMap<Integer, String> emails;
    private ConcurrentHashMap<Integer, Integer> currentLeaders;

    public Replica(int id) throws Exception {
        super(); // Initializes RMI server instance
        this.id = id;
        this.items = new ConcurrentHashMap<>();
        this.users = new ArrayList<>();
        this.currentLeaders = new ConcurrentHashMap<>();
        this.emails = new ConcurrentHashMap<>();
    }

    public int register(String email) throws RemoteException {
        int userID = email.hashCode();
        users.add(userID);
        emails.put(userID, email);
        return userID;
    }

    public AuctionItem getSpec(int itemID) throws RemoteException {
        return items.get(itemID);
    }

    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        int itemID = item.hashCode(); 
        AuctionItem auctionItem = new AuctionItem();
        auctionItem.itemID = itemID;
        auctionItem.name = item.name;
        auctionItem.description = item.description;
        auctionItem.highestBid = item.reservePrice;
        items.put(itemID, auctionItem);
        return itemID;
    }

    public AuctionItem[] listItems() throws RemoteException {
        return items.values().toArray(new AuctionItem[0]);
    }

    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        AuctionItem item = items.remove(itemID);
        if (item == null) {
            return null;
        }
        AuctionResult result = new AuctionResult();

        int winningUserID = this.currentLeaders.get(itemID);
        String email = this.emails.get(winningUserID);

        result.winningEmail = email; 
        result.winningPrice = item.highestBid;
        return result;
    }

    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        AuctionItem item = items.get(itemID);
    
        if (item == null) {
            System.err.println("Bid failed: Item with ID " + itemID + " does not exist.");
            return false;
        }
    
        if (price > item.highestBid) {
            item.highestBid = price;
            this.currentLeaders.put(itemID, userID);
            System.out.println("Bid successful: User " + userID + " bid " + price + " on item " + itemID);
            return true;
        } else {
            System.err.println("Bid failed: Price " + price + " is not higher than the current highest bid of " + item.highestBid);
            return false;
        }
    }
    

    public int getPrimaryReplicaID(){
        return this.id;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Replica <replicaID>");
            return;
        }

        try {
            int id = Integer.parseInt(args[0]);
            Replica replica = new Replica(id);
            Naming.rebind("rmi://localhost/Replica" + id, replica);
            System.out.println("Replica " + id + " is up and running.");
        } catch (Exception e) {
            System.err.println("Error starting replica: " + e.getMessage());
            e.printStackTrace();
        }
    }
}