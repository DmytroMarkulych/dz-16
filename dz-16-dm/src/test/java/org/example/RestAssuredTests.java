package org.example;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class RestAssuredTests {

    private String token;
    private int bookingId;
    private SoftAssert softAssert = new SoftAssert();

    @BeforeMethod
    public void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";

        // Authentication request
        Response response = RestAssured.given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body("{\n" +
                        "    \"username\" : \"admin\",\n" +
                        "    \"password\" : \"password123\"\n" +
                        "}")
                .post("/auth");

        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        JsonPath jsonPath = response.jsonPath();
        token = jsonPath.getString("token");

        System.out.println("Token: " + token);
        softAssert.assertNotNull(token, "Token should not be null");
    }

    @Test
    public void createBooking() {
        String requestBody = "{\n" +
                "    \"firstname\": \"Jim\",\n" +
                "    \"lastname\": \"Brown\",\n" +
                "    \"totalprice\": 111,\n" +
                "    \"depositpaid\": true,\n" +
                "    \"bookingdates\": {\n" +
                "        \"checkin\": \"2018-01-01\",\n" +
                "        \"checkout\": \"2019-01-02\"\n" +
                "    },\n" +
                "    \"additionalneeds\": \"Breakfast\"\n" +
                "}";

        // Send the POST request to create a booking
        Response response = RestAssured
                .given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .post("/booking");

        response.then().statusCode(200);

        JsonPath jsonPath = response.jsonPath();
        bookingId = jsonPath.getInt("bookingid");
        softAssert.assertTrue(bookingId > 0, "Booking ID should be greater than 0");
        System.out.println("Booking ID: " + bookingId);
    }

    @Test(dependsOnMethods = "createBooking")
    public void getBooking() {
        if (bookingId <= 0) {
            throw new IllegalStateException("Booking ID not initialized. Ensure createBooking test runs successfully.");
        }

        Response response = RestAssured.given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .get("/booking/" + bookingId);

        response.then().statusCode(200);
        response.prettyPrint();
        softAssert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
    }

    @Test(dependsOnMethods = "createBooking")
    public void patchBooking() {
        if (bookingId <= 0) {
            throw new IllegalStateException("Booking ID not initialized. Ensure createBooking test runs successfully.");
        }
        String requestBody = "{\n" +
                "    \"totalprice\": 222\n" +
                "}";

        Response response = RestAssured
                .given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Cookie", "token=" + token)
                .body(requestBody)
                .patch("/booking/" + bookingId);

        response.then().statusCode(200);
        response.prettyPrint();
        softAssert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
    }

    @Test(dependsOnMethods = "patchBooking")
    public void putBooking() {
        if (bookingId <= 0) {
            throw new IllegalStateException("Booking ID not initialized. Ensure createBooking test runs successfully.");
        }
        String requestBody = "{\n" +
                "    \"firstname\": \"JimUpdated\",\n" +
                "    \"lastname\": \"BrownUpdated\",\n" +
                "    \"totalprice\": 3333,\n" +
                "    \"depositpaid\": false,\n" +
                "    \"bookingdates\": {\n" +
                "        \"checkin\": \"2019-01-01\",\n" +
                "        \"checkout\": \"2020-01-02\"\n" +
                "    },\n" +
                "    \"additionalneeds\": \"BreakfastUpdated\"\n" +
                "}";

        Response response = RestAssured
                .given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Cookie", "token=" + token)
                .body(requestBody)
                .put("/booking/" + bookingId);

        response.then().statusCode(200);
        response.prettyPrint();
        softAssert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
    }

    @Test(dependsOnMethods = "putBooking")
    public void getUpdatedBooking() {
        if (bookingId <= 0) {
            throw new IllegalStateException("Booking ID not initialized. Ensure putBooking test runs successfully.");
        }

        Response response = RestAssured.given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .get("/booking/" + bookingId);

        response.then().statusCode(200);
        response.prettyPrint();
        softAssert.assertEquals(response.getStatusCode(), 200, "Status code should be 200");
    }

    @Test(dependsOnMethods = "getUpdatedBooking")
    public void deleteBooking() {
        if (bookingId <= 0) {
            throw new IllegalStateException("Booking ID not initialized. Ensure getUpdatedBooking test runs successfully.");
        }

        Response response = RestAssured
                .given()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Cookie", "token=" + token)
                .delete("/booking/" + bookingId);

        response.then().statusCode(201);
        response.prettyPrint();
        softAssert.assertEquals(response.getStatusCode(), 201, "Status code should be 201");
    }

    @AfterMethod
    public void tearDown() {
        softAssert.assertAll();
    }
}
