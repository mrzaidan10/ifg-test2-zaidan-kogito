package com.example;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class CheckoutProcessTest {

    @Test
    public void testStartCheckoutProcess() {
        String payload = """
            {
              "request": {
                "cartId": "CART-001",
                "customerId": "USER-123",
                "amount": 150000,
                "cartValid": true,
                "stockAvailable": true,
                "paymentSuccessful": true
              }
            }
            """;

        given()
          .contentType(ContentType.JSON)
          .body(payload)
        .when()
          .post("/Process_Checkout/create")
        .then()
          .statusCode(201)
          .body("id", notNullValue());
    }
}