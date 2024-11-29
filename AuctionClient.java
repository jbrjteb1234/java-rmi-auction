import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Scanner;

public class AuctionClient {
    public static void main(String[] args) {
        try {
            // Connect to the RMI registry
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction auction = (Auction) registry.lookup("Auction");

            if (auction == null) {
                System.out.println("Could not connect to RMI registry");
                return;
            }

            Scanner scanner = new Scanner(System.in);
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            System.out.println("Registering user...");
            System.out.print("Enter your email: ");
            String email = scanner.nextLine();
            int userID = auction.register(email, publicKey);
            System.out.println("Registration successful. Your User ID is: " + userID);

            String token;

            while (true) {
                System.out.println("\nAuction Client Menu:");
                System.out.println("1. Create Auction");
                System.out.println("2. List Auctions");
                System.out.println("3. Get Auction Details");
                System.out.println("4. Place a Bid");
                System.out.println("5. Close Auction");
                System.out.println("6. Exit");
                System.out.print("Choose an option: ");

                int choice = Integer.parseInt(scanner.nextLine());
                token = authenticate(auction, userID, privateKey);
                switch (choice) {
                    case 1: 
                        System.out.println("Enter item name: ");
                        String itemName = scanner.nextLine();
                        System.out.println("Enter item description: ");
                        String itemDescription = scanner.nextLine();
                        System.out.println("Enter reserve price: ");
                        int reservePrice = Integer.parseInt(scanner.nextLine());

                        AuctionSaleItem item = new AuctionSaleItem();
                        item.name = itemName;
                        item.description = itemDescription;
                        item.reservePrice = reservePrice;

                        int itemID = auction.newAuction(userID, item, token);
                        if (itemID != -1) {
                            System.out.println("Auction created successfully. Item ID: " + itemID);
                        } else {
                            System.out.println("Failed to create auction.");
                        }
                        break;

                    case 2: 
                        AuctionItem[] items = auction.listItems(userID, token);
                        if (items != null) {
                            for (AuctionItem auctionItem : items) {
                                System.out.println("Item ID: " + auctionItem.itemID);
                                System.out.println("Name: " + auctionItem.name);
                                System.out.println("Description: " + auctionItem.description);
                                System.out.println("Highest Bid: " + auctionItem.highestBid);
                                System.out.println("=================");
                            }
                        } else {
                            System.out.println("No items available.");
                        }
                        break;

                    case 3: 
                        System.out.println("Enter Item ID: ");
                        int specItemID = Integer.parseInt(scanner.nextLine());
                        AuctionItem auctionItem = auction.getSpec(userID, specItemID, token);
                        if (auctionItem != null) {
                            System.out.println("Item Details:");
                            System.out.println("Name: " + auctionItem.name);
                            System.out.println("Description: " + auctionItem.description);
                            System.out.println("Highest Bid: " + auctionItem.highestBid);
                        } else {
                            System.out.println("Item not found or invalid token.");
                        }
                        break;

                    case 4: 
                        System.out.println("Enter Item ID: ");
                        int bidItemID = Integer.parseInt(scanner.nextLine());
                        System.out.println("Enter your bid amount: ");
                        int bidAmount = Integer.parseInt(scanner.nextLine());

                        boolean bidSuccess = auction.bid(userID, bidItemID, bidAmount, token);
                        if (bidSuccess) {
                            System.out.println("Bid placed successfully.");
                        } else {
                            System.out.println("Failed to place bid. Check the bid amount or token validity.");
                        }
                        break;

                    case 5: 
                        System.out.println("Enter Item ID to close: ");
                        int closeItemID = Integer.parseInt(scanner.nextLine());

                        AuctionResult result = auction.closeAuction(userID, closeItemID, token);
                        if (result != null) {
                            System.out.println("Auction closed successfully.");
                            System.out.println("Winning Email: " + result.winningEmail);
                            System.out.println("Winning Price: " + result.winningPrice);
                        } else {
                            System.out.println("Failed to close auction or invalid token.");
                        }
                        break;

                    case 6: 
                        System.out.println("Exiting client...");
                        scanner.close();
                        return;

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String authenticate(Auction auction, int userID, PrivateKey privateKey) throws Exception {
        String clientChallenge = "ClientChallenge";
        ChallengeInfo challengeInfo = auction.challenge(userID, clientChallenge);
    
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(challengeInfo.serverChallenge.getBytes());
        byte[] signedChallenge = signature.sign();
    
        TokenInfo tokenInfo = auction.authenticate(userID, signedChallenge);
        if (tokenInfo != null) {
            System.out.println("Authenticated successfully. Token received.");
            return tokenInfo.token;
        } else {
            throw new Exception("Authentication failed.");
        }
    }    
}
