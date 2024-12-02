import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Frontend extends UnicastRemoteObject implements Auction {
    
    private int primaryReplicaID;

    public Frontend() throws RemoteException {
        super();
        primaryReplicaID = -1;
        new ReplicaRegistry();
        System.out.println("Frontend is up and running.");
    }

    // Select the first available replica as the primary
    private void failover() {
        try {
            ReplicaRegistryInterface replicaRegistry = (ReplicaRegistryInterface) Naming.lookup("rmi://localhost/ReplicaRegistry");
            List<Integer> replicaIDs = replicaRegistry.getReplicaIDList();
            int failedPrimaryID = this.primaryReplicaID;
            System.out.println(replicaIDs);
            for (int replicaID : replicaIDs) {
                try {
                    ((Auction)Naming.lookup("rmi://localhost/Replica" + replicaID)).listItems();
                    this.primaryReplicaID = replicaID;
                    System.out.println("Connected to primary replica: " + replicaID);
                    break;
                } catch (Exception e) {
                    System.err.println("Failed to connect to replica: " + replicaID);
                }
            }
            if(failedPrimaryID != -1){
                replicaRegistry.recoverReplica(failedPrimaryID);
            }

        } catch (Exception e) {
            System.err.println("Error during primary replica selection: " + e.getMessage());
        }
    }

    public int register(String email) throws RemoteException {
        try {
            return ((Auction)Naming.lookup("rmi://localhost/Replica" + primaryReplicaID)).register(email);
        } catch (Exception e) {
            failover();
            return this.register(email);
        }
    }

    public AuctionItem getSpec(int itemID) throws RemoteException {
        try {
            return ((Auction)Naming.lookup("rmi://localhost/Replica" + primaryReplicaID)).getSpec(itemID);
        } catch (Exception e) {
            failover();
            return this.getSpec(itemID);
        }
    }

    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        try {
            return ((Auction)Naming.lookup("rmi://localhost/Replica" + primaryReplicaID)).newAuction(userID, item);
        } catch (Exception e) {
            failover();
            return this.newAuction(userID, item);
        }
    }

    public AuctionItem[] listItems() throws RemoteException {
        try {
            return ((Auction)Naming.lookup("rmi://localhost/Replica" + primaryReplicaID)).listItems();
        } catch (Exception e) {
            failover();
            return this.listItems();
        }
    }

    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        try {
            return ((Auction)Naming.lookup("rmi://localhost/Replica" + primaryReplicaID)).closeAuction(userID, itemID);
        } catch (Exception e) {
            failover();
            return this.closeAuction(userID, itemID);
        }
    }

    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        try {
            return ((Auction)Naming.lookup("rmi://localhost/Replica" + primaryReplicaID)).bid(userID, itemID, price);
        } catch (Exception e) {
            failover();
            return this.bid(userID, itemID, price);
        }
    }

    public int getPrimaryReplicaID() throws RemoteException {
        if(this.primaryReplicaID == -1){
            failover();
        }
        return this.primaryReplicaID;
    }

    public static void main(String[] args) {
        try {
            Naming.rebind("rmi://localhost/FrontEnd", new Frontend());
        } catch (Exception e) {
            System.err.println("Error starting frontend: " + e.getMessage());
            e.printStackTrace();
        }
    }
}