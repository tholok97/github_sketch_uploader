package com.example.tholok.github_sketch_uploader;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class UploadActivity extends AppCompatActivity {

    public static String BITMAP_EXTRA = "bitmap_extra";

    private static String LOG_TAG = "UploadActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {

            // get encoded picture
            String base64PictureString = b.getString(UploadActivity.BITMAP_EXTRA);

            new PushBase64PictureToRepositoryTask().execute(
                    base64PictureString,
                    "tholok97",
                    "test",
                    "THETOKENGOESHERE", // removed for adding into git
                    "data/testtest.jpg",
                    "BRANCH",
                    "commit message",
                    "thomahl@stud.ntnu.no",
                    "tholok97"
            );
        } else {
            // should only be called with bundle..
        }
    }

    /**
     * Task to upload picture to Github.
     *
     * Paramters are as follows (all strings):
     *      base64PictureString
     *      user
     *      repositoryName
     *      token
     *      targetPath
     *      targetBranch
     *      commitMessage
     *      committerEmail
     *      committerUsername
     */
    private class PushBase64PictureToRepositoryTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {

            // put passed parameters into variables
            String base64PictureString  = strings[0];
            String user                 = strings[1];
            String repositoryName       = strings[2];
            String token                = strings[3];
            String targetPath           = strings[4];
            String targetBranch         = strings[5];
            String commitMessage        = strings[6];
            String committerEmail       = strings[7];
            String committerUsername    = strings[8];

            try {

                GitHubClient client = new GitHubClient();
                client.setOAuth2Token(token);

                // based on http://swanson.github.com/blog/2011/07/23/digging-around-the-github-api-take-2.html
                // initialize github client

                Log.d(LOG_TAG, "Starting to do commit thing");

                // create needed services
                RepositoryService repositoryService = new RepositoryService(client);
                CommitService commitService = new CommitService(client);
                DataService dataService = new DataService(client);

                // get some sha's from current state in git
                Repository repository =  repositoryService.getRepository(user, repositoryName);
                String baseCommitSha = repositoryService.getBranches(repository).get(0).getCommit().getSha();
                RepositoryCommit baseCommit = commitService.getCommit(repository, baseCommitSha);
                String treeSha = baseCommit.getSha();

                // create new blob with data
                Blob blob = new Blob();
                blob.setContent(base64PictureString);
                blob.setEncoding("base64");
                String blob_sha = dataService.createBlob(repository, blob);
                Tree baseTree = dataService.getTree(repository, treeSha);

                // create new tree entry
                TreeEntry treeEntry = new TreeEntry();
                treeEntry.setPath(targetPath);
                treeEntry.setMode(TreeEntry.MODE_BLOB);
                treeEntry.setType(TreeEntry.TYPE_BLOB);
                treeEntry.setSha(blob_sha);
                treeEntry.setSize(blob.getContent().length());
                Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
                entries.add(treeEntry);
                Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

                // create commit
                Commit commit = new Commit();
                commit.setMessage(commitMessage);
                commit.setTree(newTree);
                commit.setAuthor(new CommitUser().setDate(new Date()).setEmail(committerEmail).setName(committerUsername));
                commit.setCommitter(new CommitUser().setDate(new Date()).setEmail(committerEmail).setName(committerUsername));
                List<Commit> listOfCommits = new ArrayList<Commit>();
                listOfCommits.add(new Commit().setSha(baseCommitSha));
                // listOfCommits.containsAll(base_commit.getParents());
                commit.setParents(listOfCommits);
                // commit.setSha(base_commit.getSha());
                Commit newCommit = dataService.createCommit(repository, commit);

                // create resource
                TypedResource commitResource = new TypedResource();
                commitResource.setSha(newCommit.getSha());
                commitResource.setType(TypedResource.TYPE_COMMIT);
                commitResource.setUrl(newCommit.getUrl());

                // get master reference and update it
                Reference reference = dataService.getReference(repository, "heads/master");     //TODO
                reference.setObject(commitResource);
                dataService.editReference(repository, reference, true);

                // success
                Log.d(LOG_TAG, "updating github");
            } catch (Exception e) {
                // error
                e.printStackTrace();
                Log.d(LOG_TAG, "error: " + e.getMessage());
            }




            return null;
        }
    }

}
