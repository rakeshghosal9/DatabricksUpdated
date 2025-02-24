package org.example.definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.example.reusablemethods.ReusableCommonMethods;
import org.testng.Assert;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;

public class DatabricksStepDefinition {

    Properties prop;
    String sqlQuery;
    ResultSet resultSet;
    HashMap<String, HashMap<String, String>> dataFromDatabricks = new HashMap<>();
    String accessToken = null;

    @Given("the databricks configuration file is read successfully")
    public void the_databricks_configuration_file_is_read_successfully() {
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

    @When("the databricks query is read from file {string}")
    public void the_databricks_query_is_read_from_file(String filePath) {
        try {
            filePath = "src/test/resources/" + filePath;
            sqlQuery = new String(Files.readAllBytes(Paths.get(filePath)));
            if (sqlQuery.isEmpty()) {
                Assert.fail("The Databricks Query is empty having path [" + filePath + "], please check");
            }
        } catch (Exception e) {
            Assert.fail("Databricks Query is not read successfully, please check the file path");
        }
    }

    @When("the databricks query is executed and result is received")
    public void the_databricks_query_is_executed_and_result_is_received() {
        try {
            String jdbcURL = prop.getProperty("jdbcurl");
            Properties connectionProperties = new Properties();
            connectionProperties.put("user", "token");
            connectionProperties.put("password", prop.getProperty("password"));
            Connection connection = DriverManager.getConnection(jdbcURL, connectionProperties);
            if (connection != null) {
                Statement statement = connection.createStatement();
                resultSet = statement.executeQuery(sqlQuery);
            } else {
                Assert.fail("DB connection is not successful");
            }
        } catch (Exception e) {
            Assert.fail("Exception Occurred while establishing connection with database : " + e);
        }
    }

    @When("store the databricks query result from DB for comparison considering {string} column as primary key")
    public void store_the_databricks_query_result_from_db_for_comparison(String primaryKeyColumnName) {
        try {
            String primaryKeyValue = null;
            if (resultSet != null) {
                ResultSetMetaData md = resultSet.getMetaData();
                int totalColumn = md.getColumnCount();
                while (resultSet.next()) {
                    HashMap<String, String> temp = new HashMap<>();
                    for (int i = 0; i <= totalColumn; i++) {
                        String columnName = md.getColumnName(i).toUpperCase();
                        if (columnName.contains(".")) {
                            int index = columnName.indexOf(".");
                            columnName = columnName.substring((index + 1));
                        }
                        if (columnName.equalsIgnoreCase(primaryKeyColumnName)) {
                            primaryKeyValue = resultSet.getObject(i).toString();
                        }
                        if (resultSet.getObject(i) != null) {
                            temp.put(columnName.toUpperCase(), resultSet.getObject(i).toString());
                        } else {
                            temp.put(columnName.toUpperCase(), "NULL");
                        }
                    }
                    dataFromDatabricks.put(primaryKeyValue, temp);
                }

            } else {
                Assert.fail("No result received from the DataBricks, please check");
            }

        } catch (Exception e) {
            Assert.fail("Exception Occurred while fetching data from databricks : " + e);
        }
    }

    @Given("the access token is fetched to retrieve the data from Collibra")
    public void the_access_token_is_fetched_to_retrieve_the_data_from_collibra() {
        try {
            // Spotify API Token Renewal URL
            String tokenUrl = prop.getProperty("token_url");
            // Making the POST request
            Response response = given().relaxedHTTPSValidation().proxy(prop.getProperty("proxy"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Connection", "keep-alive")// Required header
                    .formParam("grant_type", prop.get("grant_type"))
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

        } catch (Exception e) {
            Assert.fail("Exception Occurred while fetching the access token : " + e);
        }
    }

    @Given("fetch the data by making API call to Collibra and validate against Databricks DB for {string} table")
    public void fetch_the_data_by_making_api_call_from_collibra(String tableName) {
        try {
            String endPointName = null;
            String mappingFileName = null;
            String primaryKeyInJSONResponse = null;
            //Define the Request Specification
            RequestSpecBuilder reqBuilder = new RequestSpecBuilder().
                    setBaseUri(prop.getProperty("base_uri")).
                    setBasePath(prop.getProperty("base_path")).
                    addHeader("Authorization", "Bearer " + accessToken).
                    addHeader("Accept", "application/json").
                    addHeader("Content-Type", "application/json").
                    setContentType(ContentType.JSON).
                    log(LogDetail.URI);
            RequestSpecification requestSpecification = reqBuilder.build();

            //Define the Response Specification
            ResponseSpecBuilder resBuilder = new ResponseSpecBuilder()
                    .log(LogDetail.STATUS);
            ResponseSpecification responseSpecification = resBuilder.build();

            switch (tableName) {
                case "asset":
                    endPointName = "asset_table_end_point";
                    mappingFileName = "asset_table_mapping.properties";
                    primaryKeyInJSONResponse = "id";
                    break;
                default:
                    Assert.fail("Given table [" + tableName + "] is not matching with any of the value in code");
            }

            String filePath = System.getProperty("user.dir") + "//src//test//resources//DB_JSON_MAPPING//" +
                    mappingFileName;
            Properties mappingFileProperty = ReusableCommonMethods.getPropertiesFileObject(filePath);
            if (mappingFileProperty == null) {
                Assert.fail("No Property file is available in the name of " +
                        "[" + mappingFileName + "] under folder DB_JSON_MAPPING");
            }

            int totalIteration = 1;
            int offset = 0;
            int limit = 1000;
            boolean overallValidationStatus = true;
            int totalRecordValidated = 0;
            int totalPass = 0;
            int totalFail = 0;
            for (int i = 0; i < totalIteration; i++) {
                Response response = given(requestSpecification).proxy(prop.getProperty("proxy")).
                        when().param("offset", offset).param("limit", limit).
                        get(prop.getProperty(endPointName)).
                        then().spec(responseSpecification).
                        assertThat().
                        statusCode(200).extract().response();
                totalIteration = (response.jsonPath().getInt("total") / limit + 1);
                for (int jsonIndex = 0; jsonIndex < limit; jsonIndex++) {
                    boolean eachJSONRecordValidationStatus = true;
                    totalRecordValidated++;
                    String primaryValue =
                            response.jsonPath().getString(("results[" + jsonIndex + "]." + primaryKeyInJSONResponse));
                    if (dataFromDatabricks.containsKey(primaryValue)) {
                        for (String jsonKey : mappingFileProperty.stringPropertyNames()) {
                            String valueFromJSON = response.jsonPath().getString("results[" + jsonIndex + "]." + jsonKey);
                            String valueFromDB = dataFromDatabricks.get(primaryValue).get(mappingFileProperty.getProperty(jsonKey).toUpperCase());
                            if (valueFromDB.compareTo(valueFromJSON) != 0) {
                                System.out.println("Value not matching for [" + jsonKey + "], " +
                                        "Expected value from JSON [" + valueFromJSON + "],Actual value from DB [" + valueFromDB + "]");
                                overallValidationStatus = false;
                                eachJSONRecordValidationStatus = false;
                            }
                        }

                    } else {
                        System.out.println("Primary Value  [" + primaryValue + "] is not present in Databricks Database");
                        overallValidationStatus = false;
                        eachJSONRecordValidationStatus = false;
                    }
                    if (eachJSONRecordValidationStatus) {
                        totalPass++;
                    } else {
                        totalFail++;
                    }

                }
                offset = offset + limit;
                System.out.println("Total Records Validate : [" + totalRecordValidated + "], Pass [" + totalPass + "], Fail [" + totalFail + "]");
            }
            if (!overallValidationStatus) {
                Assert.fail("Validation failed, please check log for more details");
            }

        } catch (Exception e) {
            Assert.fail("Exception Occurred while validating value between DB and JSON : " + e);

        }
    }
}
