# Github sketch-uploader

* **Code:** github sketch uploader
* **Category**: Github integration, devtool

An app that encourages sketching (diagrams, program flow, design, whatever) on pen and paper during development by making it easier / more acceptable to upload images to git. The app would be an alternative to wasting time in cumbersome drawing/diagramming/UML programs.

## Goals

* Let the user take an image and choose the repository and branch to upload to. Filename should be auto-generated if left unspecified.
* The app should compress the images down as much as possible, as it is going into git version control. Many of the sketches will be simple pen-and-paper ones, and these can probably be reduced a great deal in size (black and white, reducing color depth, ++) before upload, as their image quality is not the main concern. The app should let the user specify the level of compression. 
* The app should upload the image using the Github APIs.

## Topics to explore

* Find a good way of compressing down the images. External library?
* Figure out a good way to have the images added to git. (Commit as self or bot?)
* Integrate with Github using their APIs. This link might be of use: https://developer.github.com/apps/getting-started-with-building-apps/
* How to deal with authentication. Security concerns of using app to interact with repository.
* Allow for multiple images to be uploaded at once? 
* Allow for upload of stored images?
* Allow for update of existing images? Example use-case: the database design has changed, and the conceptual diagram needs an update.
* Allow for upload into user-defined folder structure?
