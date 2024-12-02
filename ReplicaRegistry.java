import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ReplicaRegistry extends UnicastRemoteObject implements ReplicaRegistryInterface{

    private List<Integer> replicaIDs;

    public ReplicaRegistry() throws RemoteException{
        super();
        replicaIDs = new ArrayList<>();
        try{
            Naming.rebind("rmi://localhost/ReplicaRegistry",this);
            System.out.println("Replica registry up");
        }catch(Exception e){
            System.err.println("Error creating Replica Registry");
        }   
    }

    public List<Integer> getReplicaIDList() throws RemoteException{
        return this.replicaIDs;
    }

    public void registerReplica(int id) throws RemoteException{
        if(replicaIDs.contains(id)){
            return;
        }
        replicaIDs.add(id);
    }

    public void recoverReplica(int id) throws RemoteException{
        try{
            Naming.rebind("rmi://localhost/Replica"+id, new Replica(id));
            System.out.println("Recovered replica "+id);
        }catch(Exception e){
            System.err.println("Error initiating recovery of replica: "+id);
        }
    }

}