package org.example.definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.example.reusablemethods.ReusableCommonMethods;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

import static io.restassured.RestAssured.given;

public class SpotifyStepDefinition {

    String accessToken = null;
    RequestSpecification requestSpecification;
    ResponseSpecification responseSpecification;
    Response response = null;
    String sqlQuery;
    ResultSet resultSet;
    HashMap<String, String> dataFromAPI = new HashMap<>();
    HashMap<String, String> dataFromDB = new HashMap<>();
    Properties prop;


    @Given("set up the basic configuration for the API call")
    public void setUpBasicConfig() {

        RequestSpecBuilder reqBuilder = new RequestSpecBuilder().
                setBaseUri("https://api.spotify.com").
                setBasePath("/v1").
                addHeader("Authorization", "Bearer " + accessToken).
                setContentType(ContentType.JSON).
                log(LogDetail.URI);
        requestSpecification = reqBuilder.build();
        ResponseSpecBuilder resBuilder = new ResponseSpecBuilder()
                .log(LogDetail.STATUS);
        responseSpecification = resBuilder.build();

    }

    @Given("the access token required for spotify is renewed")
    public void renewSpotifyAccessToken() {

        // Spotify API Token Renewal URL
        String tokenUrl = "https://accounts.spotify.com/api/token";
        // Making the POST request
        Response response = given()
                .header("Content-Type", "application/x-www-form-urlencoded") // Required header
                .formParam("grant_type", prop.get("grant_type"))
                .formParam("refresh_token", prop.get("refresh_token"))
                .formParam("client_id", prop.get("client_id"))
                .formParam("client_secret", prop.get("client_secret"))
                .when()
                .post(tokenUrl)
                .then()
                .statusCode(200)  // Validate success response
                .extract()
                .response();

        // Extract the new access token
        accessToken = response.jsonPath().getString("access_token");
        // Print the new token
        System.out.println("New Access Token: " + accessToken);

    }

    @Given("validate the details from API is matching with previously stored data from DB")
    public void retrievePlaylistOfAnArtist() {
        int totalIteration = 1;
        int offset = 0;
        int limit = 50;
        for (int i = 0; i < totalIteration; i++) {
            response = given(requestSpecification).
                    when().param("offset", offset).param("limit", limit).
                    get("/artists/12bITtONPx1jkPjsQVMHP3/albums").
                    then().spec(responseSpecification).
                    assertThat().
                    statusCode(200).extract().response();
            totalIteration = (response.jsonPath().getInt("total") / limit + 1);
            for (int j = 0; j < 50; j++) {
                //System.out.println(response.jsonPath().getString("items[" + j + "].id") + "|" + response.jsonPath().getString("items[" + j + "].name"));
                if (dataFromDB.containsKey(response.jsonPath().getString("items[" + j + "].id"))
                        && dataFromDB.get(response.jsonPath().getString("items[" + j + "].id")).equalsIgnoreCase(response.jsonPath().getString("items[" + j + "].name"))) {
                    System.out.println("Validation Passed : " + response.jsonPath().getString("items[" + j + "].id"));
                } else {
                    System.out.println("Validation Failed : " + response.jsonPath().getString("items[" + j + "].id"));
                }
            }
            offset = offset + 50;
        }
    }

    @Given("the sql query is read from file {string}")
    public void the_sql_query_is_read_from_file(String filePath) throws IOException {
        filePath = "src/test/resources/" + filePath;
        sqlQuery = new String(Files.readAllBytes(Paths.get(filePath)));
    }

    @Given("query is executed and result is received")
    public void query_is_executed_and_result_is_received() throws SQLException {
        // Establish DB connection
        // Database connection parameters
        String jdbcURL = prop.getProperty("jdbcurl");
        String username = prop.getProperty("username");
        String password = prop.getProperty("password");
        Connection connection = DriverManager.getConnection(jdbcURL, username, password);
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(sqlQuery);

    }

    @Then("store the result from DB in memory")
    public void storeTheResultFromDB() throws SQLException {
        if (resultSet != null) {
            while (resultSet.next()) {
                dataFromDB.put(resultSet.getString(1), resultSet.getString(2));
            }
            System.out.println("Data from DB : " + dataFromDB);
        }
    }

    @Then("validate the details from api and db are matching")
    public void validate_the_details_from_api_and_db_are_matching() throws SQLException {
        if (resultSet != null) {
            while (resultSet.next()) {
                dataFromDB.put(resultSet.getString(1), resultSet.getString(2));
            }
            System.out.println("Data from DB : " + dataFromDB);
        }
    }

    @Then("the configuration file is read successfully")
    public void readConfigurationFile() throws SQLException {
        //Validate if environment parameter is not passed from maven command line
        if (System.getProperty("env") != null && !System.getProperty("env").trim().isEmpty()) {
            //Take the environment passed from the maven command line
            String filePath = System.getProperty("user.dir") + "//src//test//resources//environments//" +
                    System.getProperty("env").trim() + ".properties";
            prop = ReusableCommonMethods.getPropertiesFileObject(filePath);
        } else {
            Assert.fail("Please provide the environment to read the configuration file");
        }
    }
}
