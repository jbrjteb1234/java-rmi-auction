import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Frontend extends UnicastRemoteObject implements Auction {
    private Auction primaryReplica;
    private List<String> replicas;

    public Frontend() throws RemoteException {
        super();
        replicas = new ArrayList<>();
        // Add replicas by default (e.g., Replica1, Replica2, Replica3)
        replicas.add("Replica1");
        replicas.add("Replica2");
        replicas.add("Replica3");
        setPrimaryReplica();
    }

    // Select the first available replica as the primary
    private void setPrimaryReplica() {
        for (String replicaName : replicas) {
            try {
                primaryReplica = (Auction) Naming.lookup("rmi://localhost/" + replicaName);
                System.out.println("Connected to primary replica: " + replicaName);
                return;
            } catch (Exception e) {
                System.err.println("Failed to connect to replica: " + replicaName);
            }
        }
        System.err.println("No replica is available.");
        primaryReplica = null;
    }

    // Handle failover when the primary fails
    private void failover() {
        System.err.println("Failover triggered. Searching for a new primary...");
        setPrimaryReplica();
    }

    @Override
    public int register(String email) throws RemoteException {
        try {
            return primaryReplica.register(email);
        } catch (RemoteException e) {
            failover();
            return primaryReplica.register(email);
        }
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        try {
            return primaryReplica.getSpec(itemID);
        } catch (RemoteException e) {
            failover();
            return primaryReplica.getSpec(itemID);
        }
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        try {
            return primaryReplica.newAuction(userID, item);
        } catch (RemoteException e) {
            failover();
            return primaryReplica.newAuction(userID, item);
        }
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        try {
            return primaryReplica.listItems();
        } catch (RemoteException e) {
            failover();
            return primaryReplica.listItems();
        }
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        try {
            return primaryReplica.closeAuction(userID, itemID);
        } catch (RemoteException e) {
            failover();
            return primaryReplica.closeAuction(userID, itemID);
        }
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        try {
            return primaryReplica.bid(userID, itemID, price);
        } catch (RemoteException e) {
            failover();
            return primaryReplica.bid(userID, itemID, price);
        }
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        try {
            return primaryReplica.getPrimaryReplicaID();
        } catch (RemoteException e) {
            failover();
            return primaryReplica.getPrimaryReplicaID();
        }
    }

    public static void main(String[] args) {
        try {
            Frontend frontend = new Frontend();
            Naming.rebind("rmi://localhost/FrontEnd", frontend);
            System.out.println("Frontend is up and running.");
        } catch (Exception e) {
            System.err.println("Error starting frontend: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
