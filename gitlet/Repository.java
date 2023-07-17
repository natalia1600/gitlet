package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *  @author Natalia Ramirez
 */
public class Repository implements Serializable {
    /* DIRECTORIES: created during init. */
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * The branches directory.
     */
    public static final File BRANCH_DIR = join(GITLET_DIR, "branches");
    /**
     * The commits directory.
     */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    /**
     * The staging directory.
     */
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    public static final File BLOBS_ADDED = join(STAGING_DIR, "blobs_to_add");
    /**
     * File staged for removal
     */
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    static File stagingAreaFile = join(Repository.STAGING_DIR, "stagingarea.txt");


    public static void init() {
        if (join(CWD, ".gitlet").exists()) {
            System.out.println("A Gitlet version-control system already exists "
                    + "in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        BRANCH_DIR.mkdir();
        STAGING_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        BLOBS_ADDED.mkdir();
        try {
            stagingAreaFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Commit initCommit = new Commit();
        Branch master = new Branch("master", initCommit);
        StagingArea initStagingArea = new StagingArea(initCommit, master);
        Saving.saveCommit(initCommit);
        Saving.saveBranch(master);
        Saving.saveStagingArea(initStagingArea);

    }



    public static void add(String fileName) {
        // if the file does not exist in the current working directory, throw exception

        if (!join(CWD, fileName).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        StagingArea staging = Saving.getStagingArea();
        staging.toRemove.remove(fileName);
        // If the current working version of the file is identical to the version in
        // the current commit...
        String thisBlobToAddHash = Saving.getBlobHashFromFile(join(CWD, fileName));
        if (staging.headCommit.blobMap.containsKey(fileName)) {
            String headCommitBlobHash = staging.headCommit.blobMap.get(fileName);
            //PRINT STAGED TO REMOVE
            if (headCommitBlobHash.equals(thisBlobToAddHash)) {
                // ...then do not stage it to be added, and remove it from the
                // staging area if it is already there
                staging.stagedToAdd.remove(fileName);
                Saving.saveStagingArea(staging);
                return;
            }

        }
        File newFileForBlob = join(BLOBS_ADDED, thisBlobToAddHash);
        try {
            newFileForBlob.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        writeContents(newFileForBlob, readContents(join(CWD, fileName)));
        staging.stagedToAdd.put(fileName, thisBlobToAddHash);
        Saving.saveStagingArea(staging);
        //PRINT STAGED TO REMOVE
    }

    // Description: Unstage the file if it is currently staged for addition.
    // if the user has not already done so (do not remove it unless it is
    // tracked in the current commit).
    // Runtime: Should run in constant time relative to any
    // significant measure.
    // Failure cases: If the file is neither staged nor tracked by the
    // head commit, print the error
    // message No reason to remove the file.
    public static void rm(String fileName) {
        // If the file is neither staged nor tracked by the head commit, throw exception
        StagingArea staging = Saving.getStagingArea();
        // Unstage the file if it is currently staged for addition.
        if (!(staging.headCommit.blobMap.containsKey(fileName)
                || staging.stagedToAdd.containsKey(fileName))) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        staging.stagedToAdd.remove(fileName);
        // If the file is tracked in the current commit, stage it for removal and remove the
        // file from the working directory
        if (staging.headCommit.blobMap.containsKey(fileName)) {
            staging.toRemove.put(fileName, Saving.getBlobHashFromFile(join(CWD, fileName)));
            restrictedDelete(join(CWD, fileName));
        }
        Saving.saveStagingArea(staging);
    }
    //**COMMIT:
    // Saves a snapshot of tracked files in the current commit and staging
    // area so they
    // can be restored at a later time, creating a new commit. The commit is said to
    // be tracking the
    // saved files. By default, each commit’s snapshot of files will
    // be exactly the same as its parent
    // commit’s snapshot of files; it will keep versions of files exactly as
    // they are, and not update
    // them.
    // A commit will only update the contents of files it is tracking
    // that have been staged for
    // addition at the time of commit, in which case the commit will now
    // include the version of the file
    // that was staged instead of the version it got from its parent.
    // A commit will save and start tracking any files that were staged
    // for addition but weren’t
    // tracked by its parent.
    // Finally, files tracked in the current commit may be untracked
    // in the new commit as a result being
    // staged for removal by the rm command (below).

    public static void commit(String message) {
        StagingArea staging = Saving.getStagingArea();
        if (message == null || message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        // if filesToCommit and filesToRemove (staging area variables)
        // are both empty, throw exception
        if (staging.stagedToAdd.size() == 0 && staging.toRemove.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        // create new commit, the parent commit of our new commit, and the blobMap of our
        // new commit's parent
        Commit newCommit = new Commit(message);
        // remove all the files in our new commit's blobMap that are named fileName
        Commit newCommit2 = removeFilesToBeRemoved(newCommit);
        // add (replace) files from head commit's blobMap that are staged for addition
        // the staging area
        Commit newCommit3 = addFilesToBeAdded(newCommit2);

        Branch master = new Branch("master", newCommit3);
        Saving.saveBranch(master);
        StagingArea newStagingArea = new StagingArea(newCommit3, master);
        Saving.saveCommit(newCommit3);
        Saving.saveStagingArea(newStagingArea);
    }
    public static Commit removeFilesToBeRemoved(Commit newCommit) {
        // if stagingArea's filesToRemove hashset is not empty, then for
        // fileName in staging.filesToRemove,
        // remove all the files in our new commit's blobMap that
        // are named fileName
        StagingArea staging = Saving.getStagingArea();
        if (!staging.toRemove.isEmpty()) {
            for (String fileName : staging.toRemove.keySet()) {
                newCommit.blobMap.remove(fileName);
            }
        }
        return newCommit;
    }

    public static Commit addFilesToBeAdded(Commit newCommit) {
        // add (replace) files from head commit's blobMap that are
        // staged for addition the staging area
        StagingArea staging = Saving.getStagingArea();
        if (!staging.stagedToAdd.isEmpty()) {
            for (String fileName : staging.stagedToAdd.keySet()) {
                newCommit.blobMap.put(fileName, staging.stagedToAdd.get(fileName));
            }
            Saving.saveBlobs(newCommit);
        }
        return newCommit;
    }
    public static void branch(String branchName) {
        List<String> branchNames = Objects.requireNonNull(plainFilenamesIn(BRANCH_DIR));
        if (branchNames.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branch newBranch = new Branch(branchName, Saving.getStagingArea().headCommit);
        Saving.saveBranch(newBranch);
    }


    public static void log() {
        System.out.println(Saving.allParentCommits(Saving.getStagingArea().headCommit));
    }
    public static void globallog() {
        System.out.println(Saving.allCommits());
    }
    public static void find(String commitMessage) {
        System.out.println(Saving.commitsFromMessage(commitMessage));
    }

    public static void rmbranch(String branchName) {
        List<String> branchNames = Objects.requireNonNull(plainFilenamesIn(BRANCH_DIR));
        StagingArea staging = Saving.getStagingArea();
        if (!branchNames.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Branch branch = readObject(join(BRANCH_DIR, branchName), Branch.class);
        if (branch.commitHash.equals(Saving.commitHash(staging.headCommit))) {
            System.out.println("Cannot remove the current branch.");
        }
    }
    public static void status() {
        if (!join(CWD, ".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        System.out.println(Saving.printStatus());
    }
    public static void checkout1(String fileName) {
        // If the file does not exist in the previous commit, abort
        if (!Saving.getStagingArea().headCommit.blobMap.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        // get file from head commit by getting the hash from the blobMap and
        // then searching through the
        // blob directory for a file with the same hash. Then, write this file onto
        // a new file in the CWD
        // to replace the one we just deleted.
        String hashOfOldFile = Saving.getStagingArea().headCommit.blobMap.get(fileName);
        File currentFile = join(CWD, fileName);
        File oldFile = join(BLOB_DIR, hashOfOldFile);
        writeContents(currentFile, readContents(oldFile));
    }

    public static void checkout2(String commitID, String fileName) {
        if (!join(COMMIT_DIR, commitID + ".txt").exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commitGiven = readObject(join(COMMIT_DIR, commitID + ".txt"), Commit.class);
        if (!commitGiven.blobMap.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File currentWorkingFile = join(CWD, fileName);
        try {
            currentWorkingFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String blobHashGiven = commitGiven.blobMap.get(fileName);
        writeContents(currentWorkingFile, readContents(join(BLOB_DIR, blobHashGiven)));
    }

    public static void checkout3(String branchNameToCheckout) {
        if (!join(BRANCH_DIR, branchNameToCheckout).exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Branch branchGiven = readObject(join(BRANCH_DIR, branchNameToCheckout), Branch.class);
        if (branchGiven.commitHash.equals(Saving.getStagingArea().masterBranch.commitHash)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit commitToCheckoutTo = readObject(join(COMMIT_DIR, branchGiven.commitHash + ".txt"),
                Commit.class);
        for (String fileName: commitToCheckoutTo.blobMap.keySet()) {
            checkout2(branchGiven.commitHash, fileName);
        }
        StagingArea clearedStagingArea = new StagingArea(commitToCheckoutTo, branchGiven);
        Saving.saveStagingArea(clearedStagingArea);
    }

}

//    writeContents and readContents should only be used on actual files
//   readObject and writeObject should only be used on files that represent an object
