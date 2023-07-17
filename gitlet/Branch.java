package gitlet;
// added this comment to see if github is working
import java.io.Serializable;

public class Branch implements Serializable {
    String name;
    String commitHash;
    public Branch(String branchName, Commit commit) {
        name = branchName;
        commitHash = Saving.commitHash(commit);
    }
}
