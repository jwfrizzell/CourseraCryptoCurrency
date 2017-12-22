package coursera_crypto;
import java.util.ArrayList;
import java.util.List;
import java.security.PublicKey;

public class TxHandler {

    private UTXOPool currentPool = null;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
    		currentPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    		UTXOPool utxoPool = new UTXOPool();
    		int counter = 0;
    		double sumInputValues = 0;
    		double sumOutputValues = 0;
    		
    		for(Transaction.Input input: tx.getInputs()){
    			byte[] hash = input.prevTxHash;
    			int index = input.outputIndex;
    			UTXO utxo = new UTXO(hash,index);
    			Transaction.Output utxoOutput = currentPool.getTxOutput(utxo);
 
    			if(!currentPool.contains(utxo)) {
    				System.out.println("Transaction outputs are not in the current UTXO pool.");
    				return false;
    			}
    			else if(!Crypto.verifySignature(utxoOutput.address, tx.getRawDataToSign(counter), input.signature)) {
    				System.out.println(String.format("Signature %s is an invalid signature.", input.signature));
    				return false;
    			}
    			else if (utxoPool.contains(utxo)) {
    				System.out.println("Transaction has been entered multiple times and is invalid.");
    				return false;
    			}
    			else if(utxoOutput.value < 0) {
    				System.out.println("Value must be greater than or equal to zero.");
    				return false;
    			}
    			
			utxoPool.addUTXO(utxo, utxoOutput);
			
    			sumInputValues += utxoOutput.value;
    			counter++;
    		}
    		
    		for(Transaction.Output output: tx.getOutputs()) {
    			if(output.value <0) {
    				System.out.println("Output value must be greater than or equal to zero.");
    				return false;
    			}
    			sumOutputValues += output.value;
    		}
    		
    		if(sumInputValues < sumOutputValues) {
    			System.out.println("Output was greater than input. Transaction has failed.");
    			return false;
    		}
    		
    		System.out.println("Transaction was successful.");
    		return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    		ArrayList<Transaction> validTransactions = new ArrayList<Transaction>();
    		
    		for(Transaction transaction: possibleTxs) {
    			if(isValidTx(transaction)) {
    				for(Transaction.Input input: transaction.getInputs()) {
    					currentPool.removeUTXO(new UTXO(input.prevTxHash,input.outputIndex));
    				}
    				
    				int counter = 0;
    				for(Transaction.Output output: transaction.getOutputs()) {
    					currentPool.addUTXO(new UTXO(transaction.getHash(), counter),output);
    					counter++;
    				}
    				
    				validTransactions.add(transaction);
    			}
    		}
    		
    		int size = validTransactions.size();
        return validTransactions.toArray(new Transaction[size]);
    }

}

