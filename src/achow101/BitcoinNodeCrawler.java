package achow101;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

public class BitcoinNodeCrawler implements Runnable {
	
	private static boolean quit = false;
	
	Scanner scan = new Scanner(System.in);
	
	public static void main(String[] args) {
		
		// BitcoinJ Network parameters
		NetworkParameters params;

		// Variables
		String mainTestRegNet = "mainnet";
		InetSocketAddress fullNode;
		int portNum = 8333;
		int rpcPortNum = 8332;
		String hostName = "localhost";
		
		// Set Logger Properties. Log outputs to CrawlerLog.txt
		System.setProperty(SimpleLogger.LOG_FILE_KEY, "CrawlerLog.txt");
		System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true");
		System.setProperty(SimpleLogger.SHOW_LOG_NAME_KEY, "false");
		System.setProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
		System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, "HH:mm:ss:SS MM:DD:YYYY");
        
		// Define CLI Options		
		Option testnet = new Option("testnet", "Use the Testnet3 network.");
		Option help = new Option("h", "help", false, "Print this help message");
		Option debug = new Option("debug", "Log Debug information");
		@SuppressWarnings("static-access")
		Option rpcport = OptionBuilder.withArgName( "rpcport" )
                						.hasArg()
                						.withDescription(  "Port for RPC connection to full node. Default 8332" )
                						.create( "rpcport" );
		@SuppressWarnings("static-access")
		Option port = OptionBuilder.withArgName( "port" )
                					.hasArg()
                					.withDescription(  "The port for regular full node connections. Default 8333" )
                					.create( "port" );
		@SuppressWarnings("static-access")
		Option hostname = OptionBuilder.withArgName( "hostname" )
                						.hasArg()
                						.withDescription(  "Hostname of the full node to connect to. Default localhost" )
                						.create( "hostname" );
		
		// Add CLI Options
		Options options = new Options();
		options.addOption(help);
		options.addOption(testnet);
		options.addOption(debug);
		options.addOption(port);
		options.addOption(hostname);
		options.addOption(rpcport);
		
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
	        
	        if(line.hasOption("help"))
	        {    	    
	       	 // automatically generate the help statement
	       	    HelpFormatter formatter = new HelpFormatter();
	       	    formatter.printHelp( "bitcoinnodecrawler", options );
	       	    return;
	        }
	        
	        if(line.hasOption("rpcport"))
	        {
	        	rpcPortNum = Integer.parseInt(line.getOptionValue("rpcport"));
	        }
	        if(line.hasOption("port"))
	        {
	        	portNum = Integer.parseInt(line.getOptionValue("port"));
	        }
	        if(line.hasOption("hostname"))
	        {
	        	hostName = line.getOptionValue("hostname");
	        }
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
	    
		final Logger log = LoggerFactory.getLogger(BitcoinNodeCrawler.class);
		
		log.info("Network set to {}", mainTestRegNet);
		log.info("Connecting to full node {}:{}", hostName, port);
		log.info("connecting to full node with RPCPort {}", rpcport);
		
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
		peerGroup.connectTo(new InetSocketAddress(hostName, portNum));		// Connects to Localhost so that peers node connects to branches off from localhost
		peerGroup.startAsync();
        
		NodeCrawler nodeCrawler = new NodeCrawler(peerGroup);
		
		System.out.println("Bitcoin Node Crawler");
		System.out.println("********************");
		System.out.println("Options [q]uit");
		
		(new Thread(new BitcoinNodeCrawler())).start();

		// Keep this thread alive while Peer Group and listener threads work.
		do
		{
			try {
				Thread.sleep(1000);
				//System.out.print("\r");
			} catch (InterruptedException e) {
				log.error("Sleep Interrupted Exception");
				e.printStackTrace();
			}
			
		} while(!quit);

	}

	@Override
	public void run() {
		
		if(scan.hasNext("q"))
		{
			quit = true;
			return;
		}
		
	}

}
