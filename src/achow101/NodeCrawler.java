package achow101;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.AbstractPeerEventListener;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeCrawler {
	
	/*
	 * Node Crawler provides other programs with the addresses that it finds on the Bitcoin network.
	 * The program requires that the client have the connection and everything required for the Bitcoin
	 * network. This program simply returns addresses.
	 */
	
	// Global peerGroup
	private PeerGroup globalPeerGroup;
	
	public static int discoveredPeers;
	
	private final Logger log = LoggerFactory.getLogger(NodeCrawler.class);
	
	public NodeCrawler(PeerGroup peerGroup) {
		        
		// Set Peer Group
        globalPeerGroup = peerGroup;
        
		// Listen for new peer connections
		globalPeerGroup.addEventListener(new AbstractPeerEventListener() {
		    @Override
		    public void onPeerConnected(Peer peer, int peerCount) {
		    	// Get addresses from peer
				getMorePeerAddresses(peer);
				// Disconnect peer to save memory
				peer.close();
		    }
		});
		
	}
	
	// Get addresses from each peer
	private void getMorePeerAddresses(Peer peer)
	{
		List<PeerAddress> addresses;
		try {
			// Get address list from peer
			addresses = peer.getAddr().get().getAddresses();
			// Connect to each address from peer
			for(int i = 0; i < addresses.size(); i++)
			{
				// Address
				InetSocketAddress address = addresses.get(i).getSocketAddress();
				log.info("Found {} and attempting connection", address);
				globalPeerGroup.connectTo(address);
				// TODO: Add RPC Call to Bitcoin core to connect to this node.
				// TODO: Add Redis connection and add this address to database provided it isn't already there.
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

}
