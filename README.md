# Spotify Code Pub Hack
_Stockholm 2018-10-03_

Curate your Playlist by Data Engineering.


## Prerequisites:

### Set up your environment
You will need [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), [Scala](https://www.scala-lang.org/) and [SBT](https://www.scala-sbt.org/).
For more details, [please take a look at this in-depth guide](https://docs.google.com/document/d/1aHxZxZXrJEYnBWEwwPGwEtAGC5375oNJ3guqJegXhO4/edit?ts=5bb48102).

### Generate a Spotify Access Token
This hack will be using your personal listening history from your Spotify account through our [Web API](https://developer.spotify.com/documentation/web-api/). To be able to use this data, you need to create a token that gives you permission to read it.

To generate a token, go to [the Spotify developer portal](https://developer.spotify.com/console/post-playlists/) and press `Get Token`.
Make sure you select the following scopes for this exercise:
- playlist-modify-public
- playlist-modify-private
- user-top-read
- playlist-read-private

You can read more about what the different scopes provide in [this guide](https://developer.spotify.com/documentation/general/guides/scopes/).

Copy and paste this token into `spotify-codepub/src/main/resources/spotifyToken` and it will applied to any API calls you make.

### Compile the project
When you have downloaded the project you will need to compile it. This process will do things like the Avro magic we mentioned.
To do this, navigate to the project folder in your terminal and run:

```
sbt compile
```

## The exercise

Open `spotify-codepub/src/main/scala/com.spotify.data/example/CodePubPlaylistJob`.
We have created some boilerplate code for you, with comments to explain what is going on and some steps for you to complete in order to get this pipeline working.
Don't hesitate to ask your peers or the organizers if you get stuck.

### Running your pipeline
If you are using an IDE like IntelliJ, you can simply run the main method of the pipeline class to see it in action.

If you are unable to do this, you can run your pipeline using `sbt`. In this case, navigate to the project folder in your terminal and simply run:
 ```
 sbt
 ```
Now you are in a Scala environment. Since the codebase consists of one project encompassing two smaller sub-projects, we want to navigate to the one with the pipeline logic. We do this by running:
```
project spotify-codepub
```

From here, we can run the main method of our class with this command:
```
runMain com.spotify.data.example.CodePubPlaylistJob
```

# Good luck, and have fun! 🎶