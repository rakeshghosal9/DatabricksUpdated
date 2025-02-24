Feature: Fetch Songs by Artist ID from Spotify

  @DATABRICKS_COLLIBRA_VALIDATION
  Scenario: Scenario to fetch song by artist id from Spotify

    Given the databricks configuration file is read successfully
    When the databricks query is read from file "SQL/playlist.sql"
    And the databricks query is executed and result is received
    And store the databricks query result from DB for comparison considering "id" column as primary key
    Given the access token is fetched to retrieve the data from Collibra
    Then fetch the data by making API call to Collibra and validate against Databricks DB for "asset" table