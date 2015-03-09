# BitcoinNodeCrawler
Bitcoin Network Crawler that attempts to find all available Bitcoin nodes. The crawler uses BitcoinJ to connect to the network. It searches for peers and lists all the peers. After finding the peers, it will get the addresses used by the peers and connect to those. The program writes out the addresses of all nodes that it has found to a text file.

## Compiling
To compile, compile the program as a standard java program.

## Usage
Run the program from the command line as a standard java .class program. The program requires that a network be specified. The network can be Regtest, Testnet, or Mainnet.
