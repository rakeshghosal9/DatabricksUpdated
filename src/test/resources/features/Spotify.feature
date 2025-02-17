Feature: Fetch Songs by Artist ID from Spotify

  @SPOTIFY_TOKEN_RENEWAL
  Scenario: Scenario to fetch song by artist id from Spotify

    Given the configuration file is read successfully
    Given the sql query is read from file "SQL/playlist.sql"
    And query is executed and result is received
    And store the result from DB in memory
    Given the access token required for spotify is renewed
    And set up the basic configuration for the API call
    And validate the details from API is matching with previously stored data from DB