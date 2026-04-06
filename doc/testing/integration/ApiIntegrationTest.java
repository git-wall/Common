package com.example.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ApiIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @Test
    public void testGetAllItems() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/items")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    public void testGetItemById() {
        // First create an item
        String requestBody = "{\"name\":\"Test Item\",\"description\":\"Test Description\",\"price\":10.99}";
        
        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/items")
        .then()
            .statusCode(201)
            .extract().response();
        
        String itemId = createResponse.jsonPath().getString("id");
        
        // Then get the item by ID
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", itemId)
        .when()
            .get("/items/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(itemId))
            .body("name", equalTo("Test Item"))
            .body("description", equalTo("Test Description"))
            .body("price", equalTo(10.99f));
    }

    @Test
    public void testCreateItem() {
        String requestBody = "{\"name\":\"New Item\",\"description\":\"New Description\",\"price\":15.99}";
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/items")
        .then()
            .statusCode(201)
            .body("name", equalTo("New Item"))
            .body("description", equalTo("New Description"))
            .body("price", equalTo(15.99f));
    }

    @Test
    public void testUpdateItem() {
        // First create an item
        String createRequestBody = "{\"name\":\"Item to Update\",\"description\":\"Original Description\",\"price\":20.99}";
        
        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(createRequestBody)
        .when()
            .post("/items")
        .then()
            .statusCode(201)
            .extract().response();
        
        String itemId = createResponse.jsonPath().getString("id");
        
        // Then update the item
        String updateRequestBody = "{\"name\":\"Updated Item\",\"description\":\"Updated Description\",\"price\":25.99}";
        
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", itemId)
            .body(updateRequestBody)
        .when()
            .put("/items/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(itemId))
            .body("name", equalTo("Updated Item"))
            .body("description", equalTo("Updated Description"))
            .body("price", equalTo(25.99f));
    }

    @Test
    public void testDeleteItem() {
        // First create an item
        String requestBody = "{\"name\":\"Item to Delete\",\"description\":\"Delete Description\",\"price\":30.99}";
        
        Response createResponse = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/items")
        .then()
            .statusCode(201)
            .extract().response();
        
        String itemId = createResponse.jsonPath().getString("id");
        
        // Then delete the item
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", itemId)
        .when()
            .delete("/items/{id}")
        .then()
            .statusCode(204);
        
        // Verify the item is deleted
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", itemId)
        .when()
            .get("/items/{id}")
        .then()
            .statusCode(404);
    }
}