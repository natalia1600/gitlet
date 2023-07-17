package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;


public class Commit implements Serializable {
    static String initCommitHash;
    /** The author of this commit */
    String author;
    /** The date of this Commit. */
    String dateCommit;
    /** The message of this Commit. */
    String message;
    /** The SHA1-hash of the parent of this commit. */
    String parentHash;
    /** blob map with file names and hashes captured in this commit */
    // TreeMap<FileNameOfBlob, HashOfBlob> blobMap;
    TreeMap<String, String> blobMap;
    /** Set containing all the files being tracked.
     *
     */
    /* Saves a snapshot of tracked files in the current commit and staging area so they
    can be restored at a later time, creating a new commit. The commit is said to be tracking
    the saved files. By default, each commit’s snapshot of files will be exactly the same as
    its parent commit’s snapshot of files; it will keep versions of files exactly as they are,
    and not update them. A commit will only update the contents of files it is tracking that
    have been staged for addition at the time of commit, in which case the commit will now include
    the version of the file that was staged instead of the version it got from its parent. A commit
    will save and start tracking any files that were staged for addition but weren’t tracked by
    its parent. Finally, files tracked in the current commit may be untracked in the new commit
    as a result being staged for removal by the rm command */
    /** The bottom line: By default a commit has the same file contents as its parent.
     *  Files staged for addition and removal are the updates to the commit.
     *  Of course, the date (and likely the mesage) will also different from the parent.
     */

    public Commit() {
        //Meta:
        author = System.getProperty("user.dir");
        Date date = new Date();
        date.setTime(0);
        dateCommit = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").format(date);
        message = "initial commit";
        parentHash = "initParentHash";
        blobMap = new TreeMap<String, String>();
    }

    public Commit(String givenMessage) {
        //Meta:
        author = System.getProperty("user.dir");
        Date date = new Date();
        dateCommit = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z").format(date);
        message = givenMessage;
        parentHash = Saving.commitHash(Saving.getStagingArea().headCommit);
        // blob stuff:
        blobMap = Saving.getStagingArea().headCommit.blobMap;
    }
}
