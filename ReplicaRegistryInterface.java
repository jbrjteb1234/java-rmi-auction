import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ReplicaRegistryInterface extends Remote {
    void registerReplica(int id) throws RemoteException;
    List<Integer> getReplicaIDList() throws RemoteException;
    void recoverReplica(int id) throws RemoteException;
}
