package com.spotify.data.example;

// ABOUT AVRO MAGIC:
// Each field in an Avro model can be accessed with predefined getter.
// This is done in compile time, based on .avsc schema files.
// Example: If a model has a field called `name`, you can get name with `getName`.
object CodePubPlaylistJobSolution {

  def main(cmdlineArgs: Array[String]): Unit = {
    // Create a ScioContext (http://spotify.github.io/scio/api/com/spotify/scio/ScioContext.html)
    val sc = ScioContext()

    // Get the top tracks for your user
    val topTracksRequest = SpotifyApiCaller.getTopTracks()

    // Step1. Extract the IDs from your top tracks
    val ids = topTracksRequest.map(track => track.getId().toString)

    // Get audio features for your top tracks
    val topTrackAudioFeatures = SpotifyApiCaller.getTrackAudioFeatures(ids)

    sc
      // Convert your top track audio features to an SCollection
      .parallelize(topTrackAudioFeatures)
      // Step2. Use a filter to find your most danceable favorite songs
      .filter(track => track.getDanceability > 0.7)
      // Step3. Extract the track IDs
      .map(track => track.getId())
      // Save the results to a text file
      // DISCLAIMER: We need to save it as a text file in order for the playlist API call to
      // be able to read the output. Normally you would not do this part in a pipeline.
      .saveAsTextFile("playlist-tracks", numShards = 1)

    // Kick off the pipeline and wait for the results
    sc.close().waitUntilDone()

    // Read the file we created
    val playlistTracks = Source
      .fromFile("playlist-tracks/part-00000-of-00001.txt")
      .getLines()
      .toSeq

    // Fetch your user ID based on the token you provided
    val userId = SpotifyApiCaller.getUserID()

    // Step4. Define your playlist name. For example, "CodePub Playlist!"
    val playlistName = "CodePub Playlist!"

    // Create a new playlist for your user
    SpotifyApiCaller.createPlaylist(userId, playlistName)

    // Find the ID for your playlist based on its name
    val playlistId = SpotifyApiCaller.getPlaylistId(playlistName)

    // Add your dancing songs to your new playlist!
    SpotifyApiCaller.addPlaylistSong(userId, playlistId, playlistTracks)

  }
}
