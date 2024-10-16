import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;

public class AuctionServer extends UnicastRemoteObject implements Auction{

    private HashMap<Integer,AuctionItem> items;
    private SecretKey key;

    public AuctionServer() throws Exception {
        super(); //initates uro, binding to ports  etc. needed for RMI to function
        this.items = new HashMap<>();

        this.key = KeyManager.loadKey();


        //test items to laod into the server
        AuctionItem test1 = new AuctionItem(10, "car", "bmw");
        AuctionItem test2 = new AuctionItem(20, "watch", "rolex");
        AuctionItem test3 = new AuctionItem(30, "plane", "airbus a380");

        this.items.put(test1.getItemID(), test1);
        this.items.put(test2.getItemID(), test2);
        this.items.put(test3.getItemID(), test3);

        System.out.println("Auction server instantiated");
    }   

    public SealedObject getSpec(int itemID) throws RemoteException {
        try {
            AuctionItem item = items.get(itemID);
            if (item == null) {
                return null; // Item not found
            }
            //encrypt and return the auction item
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            SealedObject sealedItem = new SealedObject(item, cipher);
            return sealedItem;

        } catch (Exception e) {
            throw new RemoteException("Error retrieving item");
        }
    }

    public static void main(String[] args){
        try{
            AuctionServer server  = new AuctionServer();    //new server
            Naming.rebind("//localhost/Auction", server);               //binds server to Auction
        }catch (Exception e) {
            System.out.println("Error creating server\n");
        }
    }
}
