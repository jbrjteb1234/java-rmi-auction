import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Frontend extends UnicastRemoteObject{

    public Frontend() throws RemoteException{
        super();
    }

    public static void main(String[] args) {
        try {
            Frontend frontend = new Frontend(); 
            Naming.rebind("Frontend", replica);
            System.out.println("Frontend up\n");
        } catch (Exception e) {
            System.out.println("Error creating frontend\n");
        }
    }
}