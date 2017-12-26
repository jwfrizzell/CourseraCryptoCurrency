import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private ArrayList<Integer> lstOfFollowees = new ArrayList<Integer>();
    private Set<Transaction> transactionsPending;
    
    private double graph;
    private double malicious;
    private double txDistribution;
    private int rounds;
    private boolean[] followees;
	
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
    		this.graph = p_graph;
    		this.malicious = p_malicious;
    		this.txDistribution = p_txDistribution;
    		this.rounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
    		this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
    		this.transactionsPending = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
    		Set<Transaction> followerTransactions = new HashSet<Transaction>(this.transactionsPending);
    		this.transactionsPending.clear();
    		return followerTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
    		for(Candidate candidate: candidates)
    			this.transactionsPending.add(candidate.tx);
    }
}
