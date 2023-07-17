package gitlet;



/** Driver class for Gitlet, a subset of the Git version-control system.
 *  # A simple test of adding, committing, modifying, and checking out.
 * > init
 * <<<
 * + wug.txt wug.txt
 * > add wug.txt
 * <<<
 * > commit "added wug"
 * <<<
 * + wug.txt notwug.txt
 * # Must change
 * > checkout -- wug.txt
 * <<<
 * = wug.txt wug.txt
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        //Repository.init();
        //Repository.add("randomtext.txt");
        //Repository.commit("committing");
        //System.out.println(Saving.getStagingArea().getCurrentlyTrackedFiles());
        //Repository.rm("randomtext.txt");
        String firstArg = args[0];
        switch (firstArg) {
            case "":
                System.out.println("Please enter a command.");
                break;
            case "init":
                Repository.init();
                break;
            case "add":
                String secondArgAdd = args[1];
                Repository.add(secondArgAdd);
                break;
            case "commit":
                String commitMessage = args[1];
                Repository.commit(commitMessage);
                break;
            case "rm":
                String secondArgRm = args[1];
                Repository.rm(secondArgRm);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globallog();
                break;
            case "find":
                String message = args[1];
                Repository.find(message);
                break;
            case "status":
                Repository.status();
                break;
            case "branch":
                String branchName = args[1];
                Repository.branch(branchName);
                break;
            case "rm-branch":
                String branchName2 = args[1];
                Repository.rmbranch(branchName2);
                break;
            case "checkout":
                // java gitlet.Main checkout -- [file name]
                if (args[1].equals("--")) {
                    String fileName = args[2];
                    Repository.checkout1(fileName);
                } else if (args.length > 3 && args[2].equals("--")) {
                    // java gitlet.Main checkout [commit id] -- [file name]
                    String commitID = args[1];
                    String fileName = args[3];
                    Repository.checkout2(commitID, fileName);
                } else {
                // java gitlet.Main checkout [branch name]
                    String branchNameToCheckout = args[1];
                    Repository.checkout3(branchNameToCheckout);
                }
                break;
//            case "printstagedtoremove":
//                System.out.println(Saving.getStagingArea().toRemove);
//            case "printstagedtoadd":
//                HashSet<String> stagedToAdd = Saving.getStagingArea().stagedToRemove;
//                System.out.println(Saving.getStagingArea().stagedToAdd);
//            case "printcurrentlytracked":
//                TreeMap<String, String> blobMap = Saving.getStagingArea().headCommit.blobMap;
//                System.out.println(Saving.getStagingArea().headCommit.blobMap);
            default:
                break;
        }
    }
}
