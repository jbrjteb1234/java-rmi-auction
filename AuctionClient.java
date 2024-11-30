import java.rmi.Naming;

public class AuctionClient {
    public static void main(String[] args) {
        try {
            Auction frontEnd = (Auction) Naming.lookup("rmi://localhost/FrontEnd");

            // Example operations
            int userID = frontEnd.register("test@example.com");
            System.out.println("Registered user with ID: " + userID);

            AuctionSaleItem testItem = new AuctionSaleItem();
            testItem.reservePrice = 1000;
            testItem.name = "bmw";
            testItem.description = "blue";

            AuctionItem[] items = frontEnd.listItems();
            for (AuctionItem item : items) {
                System.out.println("Item: " + item.name);
            }

            int itemID = frontEnd.newAuction(userID, testItem);
            System.out.println(frontEnd.bid(userID, itemID, 1200));

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
