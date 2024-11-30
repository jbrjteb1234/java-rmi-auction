import java.rmi.Naming;
import java.rmi.RemoteException;

public class AuctionClient implements Auction {
    private Auction primary;

    public AuctionClient() throws Exception {
        primary = (Auction) Naming.lookup("rmi://localhost/FrontEnd");
    }
}
