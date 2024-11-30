import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Replica extends UnicastRemoteObject implements Auction{

    private int id;

    public Replica(int id) throws RemoteException{
        super();
        this.id = id;
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: java Replica <replicaID>");
            return;
        }

        try {
            String id = args[0].trim();
            String replicaName = "Replica"+id;
            Replica replica = new Replica(Integer.parseInt(id)); 
            Naming.rebind(replicaName, replica);
            System.out.println(replicaName+" up\n");
        } catch (Exception e) {
            System.out.println("Error creating "+replicaName+"\n");
        }
    }
}