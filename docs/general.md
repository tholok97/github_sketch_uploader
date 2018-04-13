# Rambling markdown file around mobile project

## Questions

* Should user authenticate in the app, or be prompted for a personal access token?
    * Asking for token directly is the simplest approach, but could be cumbersome to the user
    * Not sure how to authenticate using username/password..
    * Possible to implement username/password auth later?
* What library to use for compression

## sources

* Helpful video explanation of below process for adding a file: <https://www.youtube.com/watch?v=3VCN18PMVnw>
* Contents api docs (simplest way of adding file): <https://developer.github.com/v3/repos/contents/>
* Helpful walkthrough of using egit to add a file: <https://gist.github.com/Detelca/2337731>
* Camera2 overview: <https://www.youtube.com/watch?v=KhqGphh6KPE>


## Planned external libraries

* (Something for talking with Github API: (one of the following))
    * org.eclipse.egit.github.core
    * JCabi (only jar, crashed on simple test) (might find grade dep.)
    * Volley (barebones)
* (Something to take images):
    * Camera1 (haven't looked at yet)
    * Camera2 (very advanced)
    * CameraKit (convenient, but only does preview ??)
    * Camera Intent (but cumbersome to get hold of taken images)
* Gson
* (Some image compression library (??))

## Quickest way to add a file:

"PUT https://api.github.com/repos/:user/:repo/contents/:path" with body: 

        {
            "message": "my commit message",
            "committer": {
                "name": "tholok97",
                "email": "anemail"
            },
            "content": "{{base64 content}}"
        }

## How to make commit

* Get personal access token
* Add personal access token to postman header "Authorization" with contents "token {{the-token}}"
* Make GET request to find sha of latest commit (HEAD): "GET https://api.github.com/repos/:user/:repo/git/refs/heads/master". Store "sha" property of "object" for later (sha-latest-commit)
* Make GET request to find tree of latest commit: "GET https://api.github.com/repos/:user/:repo/git/commits/{{latest-commit-sha}}". Take note of "sha" property of "tree" object (sha-base-tree)
* Create new tree with "POST https://api.github.com/repos/:user/:repo/git/trees" and body:

        {
            "base_tree": "{{sha-base-tree}}",
            "tree": [
                {
                    "path": "filename.txt",
                    "mode": "100644",
                    "type": "blob",
                    "contents": "Some content\n"
                }
            ]
        }

    Store "sha" property (sha-new-tree)
* Commit tree with "GET https://api.github.com/repos/:user/:repo/git/commits" and body:

        {
            "parents": ["{{sha-latest-commit}}"],
            "tree": "{{sha-new-tree}}",
            "message": "a commit message"
        }

    Store "sha" property (sha-new-commit).
* Set new head (?) "POST https://api.github.com/repos/:user/:repo/git/refs/heads/master" with body:

        {
            "sha": "{{sha-new-commit}}"
        }

* Done!
