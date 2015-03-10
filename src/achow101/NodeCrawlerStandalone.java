package achow101;

import java.net.InetSocketAddress;

import org.bitcoinj.core.AbstractPeerEventListener;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeCrawlerStandalone {
	
	private final static Logger log = LoggerFactory.getLogger(NodeCrawlerStandalone.class);
	
	private static int connectedPeers;

	// TODO: Make nice looking console GUI
	// TODO: Add optional CLI options
	
	public static void main(String[] args) {
		
		// This line makes the log output more compact and easily read, especially when using the JDK log adapter.
        BriefLogFormatter.init();
        if (args.length < 1) {
            System.err.println("Usage: [Mainnet|Testnet|Regtest]");
            return;
        }
        
        // BitcoinJ Network parameters
		NetworkParameters params;
		
		// TODO: Add optional command line arguments.
		// Determine which network from first argument
		String mainTestRegNet = args[0];
		
		log.info("Network set to {}", mainTestRegNet);
		
		// Determine network for network parameters
		switch(mainTestRegNet)
		{
		// Testnet
		case "Testnet":
			params = TestNet3Params.get();
			break;
		// Regtest network
		case "Regtest":
			params = RegTestParams.get();
		    break;
		// Mainnet default
		default:
			params = MainNetParams.get();
		    break;		
		}
		
		// New Peer Group which manages connections to the Bitcoin Network
		PeerGroup peerGroup = new PeerGroup(params);
						
		// Connect to network and start the PeerGroup thread
		peerGroup.addPeerDiscovery(new DnsDiscovery(params));
		peerGroup.startAsync();
        
		NodeCrawler nodeCrawler = new NodeCrawler(peerGroup);
		
		// Listen for new peer connections
		peerGroup.addEventListener(new AbstractPeerEventListener() {
		    @Override
		    public void onPeerConnected(Peer peer, int peerCount) {
		    	connectedPeers = peerGroup.numConnectedPeers();
		    }
		    @Override
		    public void onPeerDisconnected(Peer peer, int peerCount)
		    {
		    	connectedPeers = peerGroup.numConnectedPeers();
		    }
		});

		// Keep this thread alive while Peer Group and listener threads work.
		while(true)
		{
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				log.error("Sleep Interrupted Exception");
				e.printStackTrace();
			}
		}

	}

}
