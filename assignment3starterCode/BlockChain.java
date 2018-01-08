// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {

    
    private TransactionPool txPool;
    private ArrayList<ArrayList<Object>> blockData;
    private int maxBlockIndex;
    public static final int CUT_OFF_AGE = 4;


    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        UTXOPool utxoPool = new UTXOPool();
        txPool = new TransactionPool();
        addCoinbaseToUTXOPool(genesisBlock, utxoPool);
        this.blockData = new ArrayList<>();
        addToChainList(genesisBlock,utxoPool);
        maxBlockIndex = 0;
    }

    /**
     * Get the maximum height block
     */
    public Block getMaxHeightBlock() {
    		
        return (Block)this.blockData.get(this.maxBlockIndex).get(0);
    }

    /**
     * Get the UTXOPool for mining a new block on top of max height block
     */
    public UTXOPool getMaxHeightUTXOPool() {
        return (UTXOPool)this.blockData.get(this.maxBlockIndex).get(1);
    }

    /**
     * Get the transaction pool to mine a new block
     */
    public TransactionPool getTransactionPool() {
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * <p>
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        byte[] prevBlockHash = block.getPrevBlockHash();
        if (prevBlockHash == null)
            return false;
        
        if(!previousBlockExist(prevBlockHash))
        		return false;
        
        setMaxHeightIndex(prevBlockHash);
        
        ArrayList<Object> maxBlock = this.blockData.get(this.maxBlockIndex);
        
        
        TxHandler transactionHandler = new TxHandler((UTXOPool)maxBlock.get(1));
        Transaction[] transactions = block.getTransactions().toArray(new Transaction[0]);
        Transaction[] validTransactions = transactionHandler.handleTxs(transactions);
        
        if (validTransactions.length != transactions.length) {
            return false;
        }
        
        int newHeight = (int)maxBlock.get(2) + 1;
        int parentHeight = newHeight - 1;
        
        int maxHeightBlock = (int)this.blockData.get(this.getMaxHeightIndexAllBlocks()).get(2);
        
        if(maxHeightBlock > CUT_OFF_AGE + 1) {
        		return false;
        }
        
        if(newHeight <= maxHeightBlock - CUT_OFF_AGE) {
        		return false;
        }
        
        if(parentHeight < maxHeightBlock - CUT_OFF_AGE) {
        		return false;
        }
        
        if(newHeight > CUT_OFF_AGE + 1) {
        		return false;
        }
        
        UTXOPool utxoPool = transactionHandler.getUTXOPool();
        addCoinbaseToUTXOPool(block, utxoPool);
        addToChainList(block,utxoPool);
        
        return true;
    }

    /**
     * Add a transaction to the transaction pool
     */
    public void addTransaction(Transaction tx) {
        txPool.addTransaction(tx);
    }

    private void addCoinbaseToUTXOPool(Block block, UTXOPool utxoPool) {
        Transaction coinbase = block.getCoinbase();
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            Transaction.Output out = coinbase.getOutput(i);
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, out);
        }
    }
    
    /*
     * For each block add the previous hash and current block hash. 
     */
    private void addToChainList( Block block, UTXOPool pool) {
    		ArrayList<Object> list = new ArrayList<>();
    		byte[] parent = block.getPrevBlockHash();
    		list.add(block);
    		list.add(pool);
    		
    		if(parent == null)
    			list.add(1);
    		else if(parent != null && this.blockData.size() == 1) {
    			list.add(2);
    		}
    		else {
    			for(ArrayList<Object> l: this.blockData) {
    				//Determine if previous hash is equal to
    				//the {@block parent hash}
    				Block previous = (Block)l.get(0);
    				if(previous.getHash() == parent)
    					list.add((int)l.get(2) + 1);
    			}
    		}
    		this.blockData.add(list);
    }
    
    /*
     * Get the maximum height for previous hash. 
     * If the blockData.get(0) is null then that is the parent block. return(1)
     */
    private void setMaxHeightIndex(byte[] hash) {
    		this.maxBlockIndex =  0;
    		
    		for(int i = 0; i < this.blockData.size(); i++) {
    			this.maxBlockIndex = i;
    			Block blockDataHash = (Block)this.blockData.get(i).get(0);
    			if(blockDataHash.getHash() == hash) {
    				break;
    			}
    		}
    }
    
    /*
     * Get the maximum height index for any blocks in the blockData array list. 
     */
    private int getMaxHeightIndexAllBlocks() {
    		int maxIndex = 0;
    		int height = (int)this.blockData.get(0).get(2);
    		
    		for(int i = 0; i < this.blockData.size(); i++) {
    			if((int)this.blockData.get(i).get(2) >= height) {
    				height = (int)this.blockData.get(i).get(2);
    				maxIndex = i;
    			}
    		}
    		return maxIndex;
    }
    
    private boolean previousBlockExist(byte[] previous) {
    		boolean exist = false;
    		for(ArrayList<Object> l: this.blockData) {
    			Block parent = (Block)l.get(0);
    			if(parent.getHash()== previous) {
    				exist = true;
    				break;
    			}
    		}
    		return exist;
    }
    
}