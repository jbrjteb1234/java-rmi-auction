import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class AuctionClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction auction = (Auction) registry.lookup("Auction");

            if(auction == null){
                System.out.println("Could not connect to RMI registry");
                return;
            }

            SealedObject sealedItem = auction.getSpec(30);
            SecretKey secretKey = KeyManager.loadKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            AuctionItem item = (AuctionItem) sealedItem.getObject(cipher);
            System.out.println(item.getName());


        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
