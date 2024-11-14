import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class AuctionServer extends UnicastRemoteObject implements Auction{

    private ConcurrentHashMap<Integer, AuctionItem> items;
    private ConcurrentHashMap<Integer, PublicKey> users;
    private ConcurrentHashMap<Integer, String> activeTokens;
    private KeyPair serverKeyPair;

    public AuctionServer() throws Exception {
        super(); //initates uro, binding to ports  etc. needed for RMI to function
        this.items = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.activeTokens = new ConcurrentHashMap<>();
    }   

    public int register(String email, PublicKey pkey) throws RemoteException{
        int userID = email.hashCode(); 
        users.put(userID, pkey);      
        return userID;                
    }
    
    public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException{
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(serverKeyPair.getPrivate());
            signature.update(clientChallenge.getBytes());

            byte[] response = signature.sign();
            String serverChallenge = Long.toHexString(Double.doubleToLongBits(Math.random()));

            // Return both the signed client challenge and the server challenge

            ChallengeInfo newChalInfo = new ChallengeInfo();
            newChalInfo.response = response;
            newChalInfo.serverChallenge = serverChallenge;

            return newChalInfo;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Challenge creation failed");
        }
    }
    
    public TokenInfo authenticate(int userID, byte signature[]) throws RemoteException{
        try {
            PublicKey clientPublicKey = users.get(userID);
            if (clientPublicKey == null) {
                throw new RemoteException("User not found");
            }

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(clientPublicKey);
            sig.update(activeTokens.get(userID).getBytes()); // verify against the server's challenge

            if (sig.verify(signature)) {
                // Generate and store a token for the client
                String token = Long.toHexString(Double.doubleToLongBits(Math.random()));
                long expiryTime = System.currentTimeMillis() + 10000; // 10 seconds validity
                activeTokens.put(userID, token);

                TokenInfo newTokenInfo = new TokenInfo();
                newTokenInfo.token = token;
                newTokenInfo.expiryTime = expiryTime;

                return newTokenInfo;
            } else {
                throw new RemoteException("Authentication failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Authentication error");
        }
    }
    
    public AuctionItem getSpec(int userID, int itemID, String token) throws RemoteException{

    }
    
    public int newAuction(int userID, AuctionSaleItem item, String token) throws RemoteException{

    }
    
    public AuctionItem[] listItems(int userID, String token) throws RemoteException{

    }
    
    public AuctionResult closeAuction(int userID, int itemID, String token) throws RemoteException{

    }
    
    public boolean bid(int userID, int itemID, int price, String token) throws RemoteException{

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
