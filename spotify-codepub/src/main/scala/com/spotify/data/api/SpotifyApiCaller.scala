package com.spotify.data.api

import com.spotify.data.api.schemas.{CodePubTrack, CodePubTrackAudioFeatures}
import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.specification.{AudioFeatures, PlaylistSimplified, Track}

import scala.io.Source

/**
  * spotify-web-api-java library wrapper
  * https://github.com/thelinmichael/spotify-web-api-java
  */
object SpotifyApiCaller {

  def getTopTracks(time_range: String = "long_term" ): Seq[CodePubTrack] = {
    val api = getSpotifyApi

    api
      .getUsersTopTracks
      .limit(50)
      .offset(0)
      .time_range(time_range)
      .build
      .execute()
      .getItems
      .map(a => toCodePubTrack(a))
  }

  def getTrackAudioFeatures(ids: Seq[String]): Seq[CodePubTrackAudioFeatures] = {
    val api = getSpotifyApi
    getAudioFeaturesForSeveralTracksSync(ids, api)
  }
  
  private def getAudioFeaturesForSeveralTracksSync(ids: Seq[String], api: SpotifyApi)
  : Seq[CodePubTrackAudioFeatures]  = {
    val getAudioFeaturesForSeveralTracksRequest =
    api.getAudioFeaturesForSeveralTracks(ids : _*).build

    getAudioFeaturesForSeveralTracksRequest
      .execute
      .map(a => toCodePubTrackAudioFeatures(a))
  }

  // Converts the API response to Avro
  private def toCodePubTrack(track: Track): CodePubTrack = {
    CodePubTrack.newBuilder()
      .setId(track.getId)
      .setUri(track.getUri)
      .setName(track.getName)
      .setExplicit(track.getIsExplicit)
      .setDurationMs(track.getDurationMs)
      .setPopularity(track.getPopularity)
      .build()
  }
  
  // Converts the API response to Avro
  private def toCodePubTrackAudioFeatures(audioFeatures: AudioFeatures)
  : CodePubTrackAudioFeatures = {
    CodePubTrackAudioFeatures.newBuilder()
      .setId(audioFeatures.getId)
      .setUri(audioFeatures.getUri)
      .setDanceability(audioFeatures.getDanceability)
      .setEnergy(audioFeatures.getEnergy)
      .setLiveness(audioFeatures.getLiveness)
      .setTempo(audioFeatures.getTempo)
      .setInstrumentalness(audioFeatures.getInstrumentalness)
      .setLoudness(audioFeatures.getLoudness)
      .setValence(audioFeatures.getValence)
      .setSpeechiness(audioFeatures.getSpeechiness)
      .setMode(audioFeatures.getMode.getType)
      .setAcousticness(audioFeatures.getAcousticness)
      .build()
  }
  
  def getUserID(): String = {
    getSpotifyApi
      .getCurrentUsersProfile
      .build
      .execute
      .getId
  }

  def createPlaylist(userId: String, name: String): Unit = {
      getSpotifyApi
        .createPlaylist(userId, name)
        .collaborative(false)
        .public_(false)
        .description("Code Pub Playlist!")
        .build
        .execute
  }

  def getPlaylistId(name: String): String = {
    getPlaylistId(name, 0, 20).getId
  }

  private def getPlaylistId(name: String, offset: Int, limit: Int): PlaylistSimplified = {
    val playlists = getSpotifyApi
      .getListOfCurrentUsersPlaylists
      .limit(limit)
      .offset(offset)
      .build
      .execute
      .getItems

    val maybePlaylist = playlists.find(_.getName.equals(name))
    maybePlaylist.getOrElse(getPlaylistId(name, offset + limit, limit))
  }

  def addPlaylistSong(userId: String, playlistId: String, trackIds: Seq[String]): Unit = {
    val uris = trackIds.map(tid => s"spotify:track:$tid").toArray

    getSpotifyApi
      .addTracksToPlaylist(userId, playlistId, uris)
      .position(0)
      .build
      .execute
  }

  // Creates a Spotify API object using the spotifyToken provided in resources
  // To generate a token, go to: https://developer.spotify.com/console/post-playlists/
  // and press 'Get Token'. You need to add the following scopes:
  // playlist-modify-public, playlist-modify-private, user-top-read, playlist-read-private
  private def getSpotifyApi: SpotifyApi = {
    val accessToken = Source.fromURL(getClass.getResource("/spotifyToken"))
                            .getLines().mkString("").trim

    new SpotifyApi.Builder()
      .setAccessToken(accessToken)
      .build
  }
}
