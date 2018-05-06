# Report

In this report I'll discuss topics around my project. First I'd like explain the background behind the project, as I did in the presentation. Then I'll get into the design, development process, features and known bugs, before I end with my conclusion.

## Background

I like diagrams as a part of the development process, but I don't like big heavy diagramming programs. I'd rather sketch it out on paper, and put that under version control instead. This is something I've been doing to a degree for a while now, but currently it's a cumbersome process to get paper sketches under version control. This is where my project comes in. This app seeks to make it easy to push paper sketches to Github, while providing compression to make the process less painfull for git.

*I view this project as a prototype for the final app, and it serves this purpose well.*

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

1. The first is hand-crafing the communication by using something like "[Volley](https://github.com/google/volley)". I steered away from this approach as I'd like to be able to easily extend my app with more Github API related features in the future. A library would make my life much easier. 
2. The second is the "[github-api.kohsuke.org](http://github-api.kohsuke.org/)" library. I could not get this to run, so I quickly skipped over to the third option. 
3. The "[github.jcabi.com](http://github.jcabi.com/)" library seemed promising, boasting good object oriented design and an out-of-the-box mocking server. It crashed at even the most basic examples though, so I gave up on it.
4. I ended up going with the "[org.eclipse.egit.github.core](https://github.com/eclipse/egit-github/tree/master/org.eclipse.egit.github.core)" library. From what I understand it is used in an official git plugin for the Eclipse IDE, so it seemed credible. After some fiddling to add it as a dependency in Gradle it works smoothly. My only gripe with it is that it seems a tad outdated. It is missing the "Create a file" API call described [here](https://developer.github.com/v3/repos/contents/). I'll discuss this point below.

The most important interaction with the API in the app is adding an image to a Github repository. There are two main ways of doing this, where one is a shortcut of the other. The two processes are described in more detail [here](./general.md).

1. You can go through long back-and-fourth process of API calls that gives you finer control, but is hard to get your head around. A good explanation (that I found earlier) is [this one](http://www.levibotelho.com/development/commit-a-file-with-the-github-api/). I've done a write-up on this back-and-fourth in the [general](./general.md) page.
2. You can add the image using the "Create a file" contents API call described [here](https://developer.github.com/v3/repos/contents/). This let's you create one file in one commit using only one API call.

As I'm currently only committing one image at a time anyways, I initially wanted to use the second approach. The libray I chose lacked this functionality though, so I had to go with the first solution. In the long run I think that one is better anyways, as it allows multiple images in one commit down the line.

#### Compression

The idea of introducing compression was to make the thought of binaries in git less painful by making them as small as possible. Initially I was planning to introduce some kind of compression library to do the job, but I ended up sticking to the built-in `Bitmap.compress` functionality for this project. It takes a "quality" argument between 0 and 100, which I've hooked up to a seekbar. This works reasonably well. 

During my (admitedly limited) testing I've found that where my phone takes photos of around 1MB, I can reduce the size down to 50kB without noticing too much of a loss in clarity. I think this is awesome.

One cool feature that is missing is the choice to force black and white on paper sketches.

### User Interface

The user interface is very barebones. I focused on getting the functionality down, so the UI is full of placeholders and bad UX. 

It consists of these five activities:

* **AboutActivity**: Displays information around the app to the user, including intention of the app, libraries used and link to Github repo.
* **MainActivity**: Intended to be the center of the app, where the user can get to the functionality they want. Currently the activity only shows placeholders where I imageine a logo and miscellaneous stats would go.
* **PreferencesActivity**: Let's user set their token and default values to be used during upload. 
* **TakePictureAndSetMetadataActivity**: This is the meat of the app. Here the user can take a picture of their paper sketch, apply compression to it, determine metadata around the upload and launch the upload activity to do the actual upload.
* **UploadActivity**: This activity does the heavy lifting of uploading a given sketch to Github. It does this while displaying a soothing spinner to the user. I imagine it could display helpful stats about the upload as well, but currently this is only represented as a placeholder.

## Development process

I used the Github issue-tracker with feature-branches wherever appropriate.

## Features

### Done

### Planned

## Known bugs

## Conclusion ("What have I learned?")

---

*Task description*

readme file explaining what the project is about, who the authors are, and how the project code is organised, and if there is a need for logins/server-side, explain that.

project report: the short report explaining the development process, the design, what was the project about, what features are included and what is on ToDo list, what was easy, what was hard, and what have you learned. 
