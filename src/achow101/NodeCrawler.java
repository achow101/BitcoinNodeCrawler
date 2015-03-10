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
	
	/*
	 * Node Crawler provides other programs with the addresses that it finds on the Bitcoin network.
	 * The program requires that the client have the connection and everything required for the Bitcoin
	 * network. This program simply returns addresses.
	 */
	
	// TODO: add options to pause/quit
	
	// Global peerGroup
	private PeerGroup globalPeerGroup;
	
	private final Logger log = LoggerFactory.getLogger(NodeCrawler.class);
	
	public NodeCrawler(PeerGroup peerGroup) {
		
		// This line makes the log output more compact and easily read, especially when using the JDK log adapter.
        BriefLogFormatter.init();
        
        globalPeerGroup = peerGroup;
        
		// Listen for new peer connections
		globalPeerGroup.addEventListener(new AbstractPeerEventListener() {
		    @Override
		    public void onPeerConnected(Peer peer, int peerCount) {
		    	// Write peer to file
		    	InetSocketAddress address = peer.getAddress().getSocketAddress();
				addressToFile(address);
				getMorePeerAddresses(peer);
		    }
		});
		
	}
	
	// Writes out each address to a text file
	private void addressToFile(InetSocketAddress address)
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
