# Report

In this report I'll discuss topics around my project. First I'd like explain the background behind the project, as I did in the presentation. Then I'll get into the design, development process, features and known bugs, before I end with my conclusion.

## Background

I like diagrams as a part of the development process, but I don't like big heavy diagramming programs. I'd rather sketch it out on paper, and put that under version control instead. This is something I've been doing to a degree for a while now, but currently it's a cumbersome process to get paper sketches under version control. This is where my project comes in. This app seeks to make it easy to push paper sketches to Github, while providing compression to make the process less painfull for git.

*I view this project as a prototype for the final app, and it serves this purpose well.*

## Design

The app has only one job; to upload images to Github, so I kept the design basic both functionality-wise and UI-wise.

### Functionality


### User Interface

The user interface is very barebones. I focused on getting the functionality down, so the UI is full of placeholders and bad UX. 

It consists of these five activities:

* **AboutActivity**: Displays information around the app to the user, including intention of the app, libraries used and link to Github repo.
* **MainActivity**: Intended to be the center of the app, where the user can get to the functionality they want. Currently the activity only shows placeholders where I imageine a logo and miscellaneous stats would go.
* **PreferencesActivity**: Let's user set their token and default values to be used during upload. 
* **TakePictureAndSetMetadataActivity**: This is the meat of the app. Here the user can take a picture of their paper sketch, apply compression to it, determine metadata around the upload and launch the upload activity to do the actual upload.
* **UploadActivity**: This activity does the heavy lifting of uploading a given sketch to Github. It does this while displaying a soothing spinner to the user. I imagine it could display helpful stats about the upload as well, but currently this is only represented as a placeholder.

## Development process

## Features

### Done

### Planned

## Known bugs

## Conclusion ("What have I learned?")

---

*Task description*

readme file explaining what the project is about, who the authors are, and how the project code is organised, and if there is a need for logins/server-side, explain that.

project report: the short report explaining the development process, the design, what was the project about, what features are included and what is on ToDo list, what was easy, what was hard, and what have you learned. 
