package com.spotify.data.api

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class SpotifyApiCallerTest extends FlatSpec {
  "ApiCaller" should "get top tracks" in {
    SpotifyApiCaller.getTopTracks()
  }

}
