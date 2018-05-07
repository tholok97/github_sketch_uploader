*The following is a report of the project as it stood at commit COMMIT*

---

# Report

In this report I'll discuss topics around my project. First I'd like explain the background behind the project, as I did in the presentation. Then I'll get into the design, development process, features and known bugs, before I end with my conclusion.

## Background

I like diagrams as a part of the development process, but I don't like big heavy diagramming programs. I'd rather sketch it out on paper, and put that under version control instead. This is something I've been doing to a degree for a while now, but currently it's a cumbersome process to get paper sketches under version control. This is where my project comes in. This app seeks to make it easy to push paper sketches to Github, while providing compression to make the process less painfull for git.

## Design

The app has only one job; to upload images to Github, so I kept the design basic both technology-wise and UI-wise.

### Technology

The app has three main technology aspects to discuss: image capture, interfacing with the Github API and compression. I spent some time researching these topics before starting this codebase. To check out my thoughts during research check out [this page](./general.md).

#### Image capture

The Android camera APIs seemed cumbersome, so I checked out the "[CameraKit](https://github.com/CameraKit/camerakit-android)" library first. It seemed like a reasonably active library, but the documentation was very much lacking. The nail in the coffin was that I couldn't figure out how to properly get full sized bitmaps of the taken images.

I also checked out the "Android Camera2" API, but it seemed like overkill for the task I wanted to achieve.

In the end I landed on the basic "camera intent" solution, where you ask the phone nicely to use an already installed camera-app to do the heavy lifting. The interface is not obvious, and examples provided by Android helped a lot.

#### Interfacing with the Github API

From my research I found four solutions:

The first is hand-crafing the communication by using something like "[Volley](https://github.com/google/volley)". I steered away from this approach as I'd like to be able to easily extend my app with more Github API related features in the future. A library would make my life much easier. 

The second is the "[github-api.kohsuke.org](http://github-api.kohsuke.org/)" library. I could not get this to run, so I quickly skipped over to the third option. 

The "[github.jcabi.com](http://github.jcabi.com/)" library seemed promising, boasting good object oriented design and an out-of-the-box mocking server. It crashed at even the most basic examples though, so I gave up on it.

I ended up going with the "[org.eclipse.egit.github.core](https://github.com/eclipse/egit-github/tree/master/org.eclipse.egit.github.core)" library. From what I understand it is used in an official git plugin for the Eclipse IDE, so it seemed credible. After some fiddling to add it as a dependency in Gradle it works smoothly. My only gripe with it is that it seems a tad outdated. It is missing the "Create a file" API call described [here](https://developer.github.com/v3/repos/contents/). I'll discuss this point below.

The most important interaction with the API in the app is adding an image to a Github repository. There are two main ways of doing this, where one is a shortcut of the other. The two processes are described in more detail [here](./general.md). **Here is a summary**:

1. You can add the image using the "Create a file" contents API call described [here](https://developer.github.com/v3/repos/contents/). This let's you create one file in one commit using only one API call.
2. You can go through long back-and-fourth process of API calls that gives you finer control, but is hard to get your head around. A good explanation (that I wish found earlier) is [this one](http://www.levibotelho.com/development/commit-a-file-with-the-github-api/). I've done a write-up on this back-and-fourth in the [general](./general.md) page.

As I'm currently only committing one image at a time anyways, I initially wanted to use the first approach. The libray I chose lacked this functionality though, so I had to go with the second solution. In the long run I think that one is better anyways, as it allows multiple images in one commit down the line.

To communicate with the Github API the app needs to authenticate. It could either do this through a username/password combination, or through a "personal access token". As the app is targeted at developers the token solution seemed most appropriate. Github allows users to generate tokens with access to specific functionality, so security-wise it seems appropriate as well.


#### Compression

The idea of introducing compression was to make the thought of binaries in git less painful by making them as small as possible. Initially I was planning to introduce some kind of compression library to do the job, but I ended up sticking to the built-in `Bitmap.compress` functionality for this project. It takes a "quality" argument between 0 and 100, which I've hooked up to a seekbar. This works reasonably well. 

During my (admitedly limited) testing I've found that where my phone takes photos of around 1MB, I can reduce the size down to 50kB without noticing too much of a loss in clarity. I think this is awesome.

One cool feature that is missing is the choice to force black and white on paper sketches.

### User Interface

The user interface is very barebones. I focused on getting the functionality down, so the UI is full of placeholders and bad UX. 

It consists of these five activities:

* **AboutActivity**: Displays information around the app to the user, including intention of the app, libraries used and link to Github repo.
* **MainActivity**: Intended to be the center of the app, where the user can get to the functionality they want. Currently the activity only shows placeholders where I imagine a logo and miscellaneous stats would go.
* **PreferencesActivity**: Let's user set their token and default values to be used during upload. 
* **TakePictureAndSetMetadataActivity**: This is the meat of the app. Here the user can take a picture of their paper sketch, apply compression to it, determine metadata around the upload and launch the upload activity to do the actual upload.
* **UploadActivity**: This activity does the heavy lifting of uploading a given sketch to Github. It does this while displaying a soothing spinner to the user. I imagine it could display helpful stats about the upload as well, but currently this is only represented as a placeholder.

## Development process

As mentioned I spent some time researching before starting this codebase. While coding I used the Github issue-tracker, with feature-branches where appropriate. I did most of my testing in the emulator, as my phone refuses to log with `Log.d`.

## Features

### Done

* The image preview is updated in realtime as you adjust compression level.
* The estimated file size is updated in realtime as you adjust compression level.
* Can set default values to speed up uploading process.
* Suggests uniquely generated filename to the user during upload process. 
* All displayed strings are separated out into `strings.xml` to prepare for localization.
* Can upload taken image with given compression level to given Github repository. 

### TODO

* Interactive tutorial upon first startup that introduces user to the app and helps them set their token.
* Replace placeholders in MainActivity and UploadActivity.
* Support chosing what branch to target. Currently forces master.
* Add support for Norwegian
* Improve UX by fully utilizing the Github API:
    * Test token on input to ensure that it works.
    * Suggest repositories and branches while uploading. 
* Compression option that forces black and white. Targeted at pen and paper sketches.
* Support mass-upload of pictures.
* Support taking pictures from gallery of phone. (Pictures taken outside of app, screengrabs?)

## Known bugs

*These are documented in the issue-tracker*

* **App sometimes crashes after taking picture while running on phyical device**. This is an annoying bug that makes it hard to test on a phyical device. I do not think it has to do with file size. I didn't notice this bug until right around my presentation, so haven't had much time to work on it. You should be able to avoid it by retrying until successful.
* **App sometimes crashes after clicked upload button in TakePictureAndSetMetadataActivity while running on phyical device**. This is a rare bug. It might be related to the above issue.
* **The compression level seekbar is horribly laggy**. This could possibly be fixed by doing the on-change work in an AsyncTask, but I have not prioritized a fix.

## Conclusion

I did not get to spend as much time with this project as I had hoped to due to the workload from other courses and my jobs. I do, however, think the project as it stands serves as a good prototype for the app I have in mind. The primary functionality I wanted is implemented and working, and the only thing stopping me personally from using the app fully are the bugs that arise when running on phyical devices. I am hoping to release this app at some point though, and it needs a fair bit of work in UI/UX before that point, as well as bug crushing.

Overall I'm happy about the project. I am left with experience browsing Java libraries, more practice presenting and a working implementation of an app-idea I've had ever since first opening Microsoft Visio.
