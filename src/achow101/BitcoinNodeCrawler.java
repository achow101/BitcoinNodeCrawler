package achow101;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bitcoinj.core.AbstractPeerEventListener;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

public class BitcoinNodeCrawler {
	
	private static int connectedPeers;

	// TODO: Make nice looking console GUI
	
	public static void main(String[] args) {
		
		// BitcoinJ Network parameters
		NetworkParameters params;

		// Variables
		String mainTestRegNet = "mainnet";
		String outfile = "Nodes.txt";
		
		// Set Logger Properties. Log outputs to CrawlerLog.txt
		// System.setProperty(SimpleLogger.LOG_FILE_KEY, "CrawlerLog.txt");
		System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true");
		System.setProperty(SimpleLogger.SHOW_LOG_NAME_KEY, "false");
		System.setProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
		System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, "HH:mm:ss:SS MM:DD:YYYY");
        
		// Define CLI Options
		@SuppressWarnings("static-access")
		Option nodefile = OptionBuilder.withArgName( "file" )
									.hasArg()
									.withDescription(  "use given file for recording nodes. Default is Nodes.txt" )
									.create( "nodefile" );
		
		Option testnet = new Option("testnet", "Use the Testnet3 network.");
		Option help = new Option("h", "help", false, "Print this help message");
		Option debug = new Option("debug", "Log Debug information");
		
		// Add CLI Options
		Options options = new Options();
		options.addOption(help);
		options.addOption(testnet);
		options.addOption(debug);
		options.addOption(nodefile);
		
		 // Command line Parser
	    CommandLineParser parser = new BasicParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        
	        // Option actions
	        if(line.hasOption("testnet"))
	        {
	        	mainTestRegNet = "testnet";
	        }
	        
	        if(line.hasOption("debug"))
	        {
	        	System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
	        }
	        
	        if(line.hasOption("nodefile"))
	        {
	        	outfile = line.getOptionValue("nodefile");
	        }
	        
	        if(line.hasOption("help"))
	        {    	    
	       	 // automatically generate the help statement
	       	    HelpFormatter formatter = new HelpFormatter();
	       	    formatter.printHelp( "bitcoinnodecrawler", options );
	       	    return;
	        }
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
	    
		final Logger log = LoggerFactory.getLogger(BitcoinNodeCrawler.class);
	    
		log.info("Network set to {}", mainTestRegNet);
		
		// Determine network for network parameters
		switch(mainTestRegNet)
		{
		// Testnet
		case "testnet":
			params = TestNet3Params.get();
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
        
		NodeCrawler nodeCrawler = new NodeCrawler(peerGroup, outfile);
		
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
