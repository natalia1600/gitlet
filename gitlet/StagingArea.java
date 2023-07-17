package gitlet;
import java.io.Serializable;
import java.util.TreeMap;

public class StagingArea implements Serializable  {
    Commit headCommit;
    Branch masterBranch;
    TreeMap<String, String> stagedToAdd;
    TreeMap<String, String> toRemove;

    public StagingArea(Commit currentHeadCommit, Branch master) {
        headCommit = currentHeadCommit;
        masterBranch = master;
        stagedToAdd = new TreeMap<>();
        toRemove = new TreeMap<>();
    }
}
