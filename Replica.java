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
import java.util.List;
import java.io.Serializable;

public class Replica extends UnicastRemoteObject implements Auction,ReplicaState {

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
        

        try{
            System.out.println("Replica " + id + " is up and running.");

            ReplicaRegistryInterface registry = (ReplicaRegistryInterface) Naming.lookup("rmi://localhost/ReplicaRegistry");
            registry.registerReplica(this.id);
        }catch(Exception e){
            System.err.println("Error initiating replica and registering: "+e);
        }

        syncWithPrimary();
    }

    public ConcurrentHashMap<Integer, AuctionItem> getItems() throws RemoteException {
        return this.items;
    }

    public ArrayList<Integer> getUsers() throws RemoteException {
        return this.users;
    }

    public ConcurrentHashMap<Integer, String> getEmails() throws RemoteException {
        return this.emails;
    }

    public ConcurrentHashMap<Integer, Integer> getCurrentLeaders() throws RemoteException {
        return this.currentLeaders;
    }

    public void set(ConcurrentHashMap<Integer, AuctionItem> items, ArrayList<Integer> users, ConcurrentHashMap<Integer, String> emails, ConcurrentHashMap<Integer, Integer> currentLeaders) throws RemoteException {
        this.items.clear();
        this.items.putAll(items);

        this.emails.clear();
        this.emails.putAll(emails);

        this.users.clear();
        this.users.addAll(users);

        this.currentLeaders.clear();
        this.currentLeaders.putAll(currentLeaders);
    }
    

    public int register(String email) throws RemoteException {
        int userID = email.hashCode();
        users.add(userID);
        emails.put(userID, email);
        replicateChanges();
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
        System.out.println("REPLICA: "+this.id+": New auction. Item id = "+itemID);
        replicateChanges();
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
        replicateChanges();
        return result;
    }

    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        AuctionItem item = items.get(itemID);
    
        if (item == null) {
            System.err.println("REPLICA: "+this.id+": Bid failed: Item with ID " + itemID + " does not exist.");
            return false;
        }
    
        if (price > item.highestBid) {
            item.highestBid = price;
            this.currentLeaders.put(itemID, userID);
            System.out.println("REPLICA: "+this.id+": Bid successful: User " + userID + " bid " + price + " on item " + itemID);
            replicateChanges();
            return true;
        } else {
            System.err.println("REPLICA: "+this.id+": Bid failed: Price " + price + " is not higher than the current highest bid of " + item.highestBid);
            return false;
        }
    }

    public int getPrimaryReplicaID(){
        return this.id;
    }

    private void replicateChanges(){
        try {
            
            // Look up the FrontEnd service
            Auction frontEnd = (Auction) Naming.lookup("rmi://localhost/FrontEnd");
            int primaryID = frontEnd.getPrimaryReplicaID(); // Get the primary replica ID
            
            if (primaryID != this.id || primaryID == -1) {
                System.out.println("Returning from replication");
                return;
            }

            System.out.println("Beginning replication\n\n");

            ReplicaRegistryInterface replicaRegistry = (ReplicaRegistryInterface) Naming.lookup("rmi://localhost/ReplicaRegistry");
            List<Integer> replicaIDs = replicaRegistry.getReplicaIDList();
            System.out.println(replicaIDs);
            for (int replicaID : replicaIDs) {
                System.out.println("Repping to: "+replicaID);
                if (replicaID == primaryID){ 
                    continue;
                }
                try {
                    ReplicaState replica = (ReplicaState) Naming.lookup("rmi://localhost/Replica" + replicaID);
                        
                    replica.set(this.items, this.users, this.emails, this.currentLeaders);

                    System.out.println("Primary replica "+this.id+" sent change to replica: " + replicaID);
                } catch (Exception e) {
                    System.err.println("Failed to connect to replica: " + replicaID);
                }
            }
        } catch (Exception e) {
            System.err.println("Error during primary replica selection: " + e.getMessage());
        }
    }

    private void syncWithPrimary() {
        try {
            // Look up the FrontEnd service
            Auction frontEnd = (Auction) Naming.lookup("rmi://localhost/FrontEnd");
            int primaryID = frontEnd.getPrimaryReplicaID(); // Get the primary replica ID
            
            if (primaryID == this.id || primaryID == -1) {
                return;
            }
    
            // Look up the primary replica by ID
            ReplicaState repState = (ReplicaState) Naming.lookup("rmi://localhost/Replica" + primaryID);
    
            System.out.println("Synchronizing with primary replica: " + primaryID);

            // Synchronize state from the primary replica
            this.items.clear();
            this.items.putAll(repState.getItems());

            this.emails.clear();
            this.emails.putAll(repState.getEmails());

            this.users.clear();
            this.users.addAll(repState.getUsers());

            this.currentLeaders.clear();
            this.currentLeaders.putAll(repState.getCurrentLeaders());

            System.out.println("Synchronization with primary replica completed successfully.");

            return;
        
        } catch (Exception e) {
            System.err.println("Error during synchronization with primary replica: " + e.getMessage());
        }
    }
    
    

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Replica <replicaID>");
            return;
        }

        try {
            int id = Integer.parseInt(args[0]);
            if(id<1){
                return;
            }
            Naming.rebind("rmi://localhost/Replica"+id, new Replica(id));
        } catch (Exception e) {
            System.err.println("Error starting replica: " + e.getMessage());
            e.printStackTrace();
        }
    }
}