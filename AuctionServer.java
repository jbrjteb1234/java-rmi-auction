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
import java.io.Serializable;

public class AuctionServer extends UnicastRemoteObject implements Auction {

    private ConcurrentHashMap<Integer, AuctionItem> items;
    private ConcurrentHashMap<Integer, PublicKey> users;
    private ConcurrentHashMap<Integer, TokenInfo> activeTokens;
    private ConcurrentHashMap<Integer, String> serverChallenges; 
    private ConcurrentHashMap<Integer, String> emails;
    private ConcurrentHashMap<Integer, Integer> currentLeaders;
    private KeyPair serverKeyPair;

    public AuctionServer() throws Exception {
        super(); // Initializes RMI server instance
        this.items = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.activeTokens = new ConcurrentHashMap<>();
        this.serverChallenges = new ConcurrentHashMap<>();
        this.currentLeaders = new ConcurrentHashMap<>();
        this.emails = new ConcurrentHashMap<>();
        this.serverKeyPair = KeyManager.loadServerKeyPair(); // Load server key pair
    }

    public int register(String email, PublicKey pkey) throws RemoteException {
        int userID = email.hashCode();
        users.put(userID, pkey);
        emails.put(userID, email);
        return userID;
    }

    public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(serverKeyPair.getPrivate());
            signature.update(clientChallenge.getBytes());

            byte[] response = signature.sign();
            String serverChallenge = Long.toHexString(Double.doubleToLongBits(Math.random()));

            serverChallenges.put(userID, serverChallenge); 

            ChallengeInfo newChalInfo = new ChallengeInfo();
            newChalInfo.response = response;
            newChalInfo.serverChallenge = serverChallenge;

            return newChalInfo;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Challenge creation failed");
        }
    }

    public TokenInfo authenticate(int userID, byte signature[]) throws RemoteException {
        try {
            PublicKey clientPublicKey = users.get(userID);
            if (clientPublicKey == null) {
                throw new RemoteException("User not found");
            }

            String serverChallenge = serverChallenges.get(userID);
            if (serverChallenge == null) {
                throw new RemoteException("Server challenge not found");
            }

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(clientPublicKey);
            sig.update(serverChallenge.getBytes()); 

            if (sig.verify(signature)) {
                String token = Long.toHexString(Double.doubleToLongBits(Math.random()));
                long expiryTime = System.currentTimeMillis() + 10000; // 10 seconds validity

                TokenInfo newTokenInfo = new TokenInfo();
                newTokenInfo.token = token;
                newTokenInfo.expiryTime = expiryTime;

                activeTokens.put(userID, newTokenInfo);
                serverChallenges.remove(userID); 

                return newTokenInfo;
            } else {
                throw new RemoteException("Authentication failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Authentication error");
        }
    }

    private boolean validateToken(int userID, String token) {
        TokenInfo tokenInfo = activeTokens.get(userID);
        if (tokenInfo == null || !tokenInfo.token.equals(token)) {
            return false;
        }
        if (System.currentTimeMillis() > tokenInfo.expiryTime) {
            activeTokens.remove(userID); // Token expired
            return false;
        }
        activeTokens.remove(userID); 
        return true;
    }

    public AuctionItem getSpec(int userID, int itemID, String token) throws RemoteException {
        if (!validateToken(userID, token)) {
            return null;
        }
        return items.get(itemID);
    }

    public int newAuction(int userID, AuctionSaleItem item, String token) throws RemoteException {
        if (!validateToken(userID, token)) {
            return -1;
        }
        int itemID = item.hashCode(); 
        AuctionItem auctionItem = new AuctionItem();
        auctionItem.itemID = itemID;
        auctionItem.name = item.name;
        auctionItem.description = item.description;
        auctionItem.highestBid = item.reservePrice;
        items.put(itemID, auctionItem);
        return itemID;
    }

    public AuctionItem[] listItems(int userID, String token) throws RemoteException {
        if (!validateToken(userID, token)) {
            return null;
        }
        return items.values().toArray(new AuctionItem[0]);
    }

    public AuctionResult closeAuction(int userID, int itemID, String token) throws RemoteException {
        if (!validateToken(userID, token)) {
            return null;
        }
        AuctionItem item = items.remove(itemID);
        if (item == null) {
            return null;
        }
        AuctionResult result = new AuctionResult();

        int winningUserID = this.currentLeaders.get(itemID);
        String email = this.emails.get(winningUserID);

        result.winningEmail = email; 
        result.winningPrice = item.highestBid;
        return result;
    }

    public boolean bid(int userID, int itemID, int price, String token) throws RemoteException {
        if (!validateToken(userID, token)) {
            return false;
        }
        AuctionItem item = items.get(itemID);
        if (item == null || price <= item.highestBid) {
            return false;
        }
        item.highestBid = price;

        this.currentLeaders.put(itemID, userID);

        return true;
    }

    public static void main(String[] args) {
        try {
            AuctionServer server = new AuctionServer(); // New server
            Naming.rebind("Auction", server); // Bind server to Auction
            System.out.println("Server up!\n");
        } catch (Exception e) {
            System.out.println("Error creating server\n");
        }
    }
}
