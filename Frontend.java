import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Frontend extends UnicastRemoteObject implements Auction {
    
    private int primaryReplicaID;

    public Frontend() throws RemoteException {
        super();

        try{
            new ReplicaRegistry();
            Naming.rebind("rmi://localhost/FrontEnd", this);
        } catch (Exception e) {
            System.err.println("Error starting frontend: " + e.getMessage());
        }

        primaryReplicaID = -1;
        System.out.println("Frontend is up and running.");
    }

    private void failover() {
        try {
            ReplicaRegistryInterface replicaRegistry = (ReplicaRegistryInterface) Naming.lookup("rmi://localhost/ReplicaRegistry");
            List<Integer> replicaIDs = replicaRegistry.getReplicaIDList();
            List<Integer> failedIDs = new ArrayList<>();
            boolean foundNewPrimary = false;
            failedIDs.add(this.primaryReplicaID);
            System.out.println(replicaIDs);
            for (int replicaID : replicaIDs) {
                try {
                    ((Auction)Naming.lookup("rmi://localhost/Replica" + replicaID)).listItems();
                    if(!foundNewPrimary){
                        this.primaryReplicaID = replicaID;
                        foundNewPrimary=true;
                    }
                    System.out.println("Connected to primary replica: " + replicaID);
                } catch (Exception e) {
                    failedIDs.add(replicaID);
                    System.err.println("Failed to connect to replica: " + replicaID);
                }
            }

            for(int failedID : failedIDs){
                if(failedID != -1){
                    replicaRegistry.recoverReplica(failedID);
                }
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
            new Frontend();
        } catch (Exception e) {
            System.err.println("Error starting frontend: " + e.getMessage());
        }
    }
}