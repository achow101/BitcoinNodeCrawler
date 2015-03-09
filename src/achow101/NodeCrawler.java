package achow101;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.bitcoinj.core.AbstractPeerEventListener;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeCrawler {

	// Global peerGroup
	private static PeerGroup peerGroup;
	
	private static final Logger log = LoggerFactory.getLogger(NodeCrawler.class);
	
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
		peerGroup = new PeerGroup(params);
				
		// Connect to network and start the PeerGroup thread
		peerGroup.addPeerDiscovery(new DnsDiscovery(params));
		peerGroup.startAsync();
		
		// Listen for new peer connections
		peerGroup.addEventListener(new AbstractPeerEventListener() {
		    @Override
		    public void onPeerConnected(Peer peer, int peerCount) {
		    	// Write peer to file
		    	InetSocketAddress address = peer.getAddress().getSocketAddress();
				addressToFile(address);
				getMorePeerAddresses(peer);
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
	
	// Writes out each address to a text file
	private static void addressToFile(InetSocketAddress address)
	{
		try
		{
			// Set file
	        File file = new File("Nodes.txt");
	        // Create file if it does not exist
	    	if(!file.exists())
	    	{
	    		file.createNewFile();
	    	}
	    	// File Writers
	    	FileWriter fw = new FileWriter(file,true);
	    	BufferedWriter bw = new BufferedWriter(fw);
	    	PrintWriter pw = new PrintWriter(bw);
	    	// Write to file
	    	pw.println(address.toString());
	    	pw.close();
	    	// Remove Duplicates
	    	stripDuplicatesFromFile("Nodes.txt");
	    	log.info("Recorded {}", address);
		}
		catch(IOException ioe){
	    	 log.error("IOException occured.");
	    	 ioe.printStackTrace();		
		}
	}
	
	// Get addresses from each peer
	private static void getMorePeerAddresses(Peer peer)
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
				peerGroup.connectTo(address);
				// Write address to file
				addressToFile(address);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	// Remove duplicate entries
	public static void stripDuplicatesFromFile(String filename) {
	    BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			Set<String> lines = new HashSet<String>(10000);
		    String line;
		    while ((line = reader.readLine()) != null) {
		        lines.add(line);
		    }
		    reader.close();
		    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		    for (String unique : lines) {
		        writer.write(unique);
		        writer.newLine();
		    }
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	}
}
