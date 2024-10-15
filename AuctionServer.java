import java.net.MalformedURLException;
import java.rmi.*;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;

public class AuctionServer implements Auction{ 

    public SealedObject getSpec(int itemID) throws RemoteException {
        //TODO: IMPLEMENT
        return null;
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
