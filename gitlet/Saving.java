package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static gitlet.Repository.BRANCH_DIR;
import static gitlet.Utils.sha1;
import static gitlet.Utils.readObject;
import static gitlet.Utils.readContents;
import static gitlet.Utils.join;
import static gitlet.Utils.plainFilenamesIn;
import static gitlet.Utils.writeObject;
import static gitlet.Utils.writeContents;
import static gitlet.Utils.serialize;

public class Saving {
    public static String getBlobHashFromFile(File blobFile) {
        return sha1(readContents(blobFile));
    }
    public static StagingArea getStagingArea() {
        File stagingAreaFileToReturn = join(Repository.STAGING_DIR, "stagingarea.txt");
        return readObject(stagingAreaFileToReturn, StagingArea.class);
    }

    public static void saveCommit(Commit commit) {
        String commitHash = commitHash(commit);
        if (commit.parentHash.equals("initial commit")) {
            Commit.initCommitHash = commitHash;
        }
        File commitFile = join(Repository.COMMIT_DIR, commitHash + ".txt");
        try {
            commitFile.createNewFile();
        } catch (IOException e) {
            System.out.println("File does not exist");
        }
        writeObject(commitFile, commit);
    }

    public static String commitHash(Commit commit) {
        return sha1(serialize(commit));
    }

    public static void saveStagingArea(StagingArea staging) {
        File stagingAreaFileToSave = join(Repository.STAGING_DIR, "stagingarea.txt");
        try {
            stagingAreaFileToSave.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        writeObject(stagingAreaFileToSave, staging);
    }
    public static void saveBranch(Branch branch) {
        File newBranchFile = join(BRANCH_DIR, branch.name);
        try {
            newBranchFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        writeObject(newBranchFile, branch);
    }

    public static void saveBlobs(Commit newCommit) {
        for (String fileName : newCommit.blobMap.keySet()) {
            String blobHash = newCommit.blobMap.get(fileName);
            File newBlobFile = join(Repository.BLOB_DIR, blobHash);
            try {
                newBlobFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            writeContents(newBlobFile, readContents(join(Repository.BLOBS_ADDED, blobHash)));
        }
    }

    public static String allParentCommits(Commit commit) {
        String log = "===\n";
        log = log.concat("commit " + getStagingArea().masterBranch.commitHash + "\n");
        log = log.concat("Date: " + commit.dateCommit + "\n");
        log = log.concat(commit.message + "\n" + "\n");

        while (!commit.parentHash.equals("initParentHash")) {
            File parentCommitFile = join(Repository.COMMIT_DIR, commit.parentHash + ".txt");
            commit = readObject(parentCommitFile, Commit.class);
            log = log.concat("===\n");
            log = log.concat("commit " + commitHash(commit) + "\n");
            log = log.concat("Date: " + commit.dateCommit + "\n");
            log = log.concat(commit.message + "\n" + "\n");
        }
        return log;
    }

    public static String allCommits() {
        String log = "";
        for (String fileName : Objects.requireNonNull(plainFilenamesIn(Repository.COMMIT_DIR))) {
            log = log.concat("===\n");
            Commit commit = readObject(join(Repository.COMMIT_DIR, fileName), Commit.class);
            log = log.concat("commit " + commitHash(commit) + "\n");
            log = log.concat("Date: " + commit.dateCommit + "\n");
            log = log.concat(commit.message + "\n" + "\n");
        }
        return log;
    }
    public static String commitsFromMessage(String commitMessage) {
        String log = "";
        for (String fileName :  Objects.requireNonNull(plainFilenamesIn(Repository.COMMIT_DIR))) {
            Commit commit = readObject(join(Repository.COMMIT_DIR, fileName), Commit.class);
            if (commit.message.equals(commitMessage)) {
                log = log.concat(fileName + "\n");
            }
        }
        if (log.equals("")) {
            return "Found no commit with that message.";
        }
        return log;
    }
    public static String printStatus() {
        StagingArea staging = getStagingArea();

        // Branches:
        String log = "=== Branches ===\n";
        List<String> branchNames = Objects.requireNonNull(plainFilenamesIn(BRANCH_DIR));
        Collections.sort(branchNames);
        for (String branchName : branchNames) {
            Branch branch = readObject(join(BRANCH_DIR, branchName), Branch.class);
            if (branch.commitHash.equals(commitHash(staging.headCommit))) {
                log = log.concat("*");
            }
            log = log.concat(branchName + "\n");
        }
        log = log.concat("\n");

        // Staged files:
        log = log.concat("=== Staged Files ===\n");
        for (String fileName : staging.stagedToAdd.navigableKeySet()) {
            log = log.concat(fileName + "\n");
        }
        log = log.concat("\n");
        // Removed files:
        log = log.concat("=== Removed Files ===\n");
        for (String fileName : staging.toRemove.navigableKeySet()) {
            log = log.concat(fileName + "\n");
        }
        log = log.concat("\n");
        log = log.concat("=== Modifications Not Staged For Commit ===\n");
        log = log.concat("\n");
        log = log.concat("=== Untracked Files ===\n");
        return log;
    }
}
