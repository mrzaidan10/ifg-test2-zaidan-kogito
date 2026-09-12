package com.example;

import com.example.checkout.audit.CheckoutAuditStore;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CheckoutProcessTest {

    @Inject
    CheckoutAuditStore auditStore;

    @BeforeEach
    void clearAuditStore() {
        auditStore.clear();
    }

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
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("id", notNullValue())
          .body("orderId", startsWith("ORD-"))
          .body("request.cartId", equalTo("CART-001"))
          .body("request.customerId", equalTo("USER-123"))
          .body("cartValid", equalTo(true))
          .body("stockAvailable", equalTo(true))
          .body("paymentSuccessful", equalTo(true));
    }

    @Test
    public void testCheckoutRejectedWhenCartInvalid() {
        String payload = """
            {
              "request": {
                "cartId": "CART-001",
                "customerId": "USER-1",
                "amount": 100,
                "cartValid": false,
                "stockAvailable": true,
                "paymentSuccessful": true
              }
            }
            """;

        given()
          .contentType(ContentType.JSON)
          .body(payload)
        .when()
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("id", notNullValue())
          .body("orderId", nullValue())
          .body("cartValid", equalTo(false))
          .body("request.cartId", equalTo("CART-001"));
    }

    @Test
    public void testCheckoutRejectedWhenOutOfStock() {
        String payload = """
            {
              "request": {
                "cartId": "CART-005",
                "customerId": "USER-2",
                "amount": 100,
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
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("id", notNullValue())
          .body("orderId", nullValue())
          .body("cartValid", equalTo(true))
          .body("stockAvailable", equalTo(false));
    }

    @Test
    public void testCheckoutRejectedWhenPaymentFails() {
        String payload = """
            {
              "request": {
                "cartId": "CART-001",
                "customerId": "USER-3",
                "amount": 99999,
                "cartValid": true,
                "stockAvailable": true,
                "paymentSuccessful": false
              }
            }
            """;

        given()
          .contentType(ContentType.JSON)
          .body(payload)
        .when()
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("id", notNullValue())
          .body("orderId", nullValue())
          .body("cartValid", equalTo(true))
          .body("stockAvailable", equalTo(true))
          .body("paymentSuccessful", equalTo(false));
    }

    @Test
    public void testCheckoutRejectedWhenAllFlagsFalse() {
        String payload = """
            {
              "request": {
                "cartId": "CART-005",
                "customerId": "USER-4",
                "amount": 50,
                "cartValid": false,
                "stockAvailable": false,
                "paymentSuccessful": false
              }
            }
            """;

        given()
          .contentType(ContentType.JSON)
          .body(payload)
        .when()
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("id", notNullValue())
          .body("orderId", nullValue())
          .body("cartValid", equalTo(false));
    }

    @Test
    public void testCheckoutRejectedCartValidButOutOfStock() {
        String payload = """
            {
              "request": {
                "cartId": "CART-005",
                "customerId": "USER-5",
                "amount": 75,
                "cartValid": true,
                "stockAvailable": false,
                "paymentSuccessful": false
              }
            }
            """;

        given()
          .contentType(ContentType.JSON)
          .body(payload)
        .when()
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("id", notNullValue())
          .body("orderId", nullValue())
          .body("cartValid", equalTo(true))
          .body("stockAvailable", equalTo(false));
    }

    @Test
    public void testInventoryReducesAfterSuccessfulCheckout() {
        given()
        .when()
          .get("/inventory/CART-004")
        .then()
          .statusCode(200)
          .body("CART-004", equalTo(1));

        String payload = """
            {
              "request": {
                "cartId": "CART-004",
                "customerId": "USER-INV",
                "amount": 500,
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
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("orderId", notNullValue());

        given()
        .when()
          .get("/inventory/CART-004")
        .then()
          .statusCode(200)
          .body("CART-004", equalTo(0));
    }

    @Test
    public void testZeroStockRejectsCheckout() {
        given()
        .when()
          .get("/inventory/CART-005")
        .then()
          .statusCode(200)
          .body("CART-005", equalTo(0));

        String payload = """
            {
              "request": {
                "cartId": "CART-005",
                "customerId": "USER-NO-STOCK",
                "amount": 500,
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
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("orderId", nullValue())
          .body("stockAvailable", equalTo(false));
    }

    @Test
    public void testAuditRecordsCreatedForSuccessfulCheckout() {
        String payload = """
            {
              "request": {
                "cartId": "CART-001",
                "customerId": "USER-A",
                "amount": 200,
                "cartValid": true,
                "stockAvailable": true,
                "paymentSuccessful": true
              }
            }
            """;

        String instanceId = given()
          .contentType(ContentType.JSON)
          .body(payload)
        .when()
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("id", notNullValue())
          .extract().path("id");

        given()
        .when()
          .get("/checkout-instances")
        .then()
          .statusCode(200)
          .body("findAll { it.instanceId == '" + instanceId + "' }.event", hasItems("started", "completed"));
    }

    @Test
    public void testAuditRecordsCreatedForRejectedCheckout() {
        String payload = """
            {
              "request": {
                "cartId": "CART-005",
                "customerId": "USER-B",
                "amount": 200,
                "cartValid": false,
                "stockAvailable": true,
                "paymentSuccessful": true
              }
            }
            """;

        String instanceId = given()
          .contentType(ContentType.JSON)
          .body(payload)
        .when()
          .post("/Process_Checkout")
        .then()
          .statusCode(201)
          .body("orderId", nullValue())
          .extract().path("id");

        given()
        .when()
          .get("/checkout-instances")
        .then()
          .statusCode(200)
          .body("findAll { it.instanceId == '" + instanceId + "' }.event", hasItems("started", "completed"));
    }

    @Test
    public void testAuditCountEndpoint() {
        given()
        .when()
          .get("/checkout-instances/count")
        .then()
          .statusCode(200)
          .body("count", equalTo(0));

        String payload = """
            {
              "request": {
                "cartId": "CART-001",
                "customerId": "USER-C",
                "amount": 300,
                "cartValid": true,
                "stockAvailable": true,
                "paymentSuccessful": true
              }
            }
            """;
        given().contentType(ContentType.JSON).body(payload)
          .when().post("/Process_Checkout").then().statusCode(201);

        given()
        .when()
          .get("/checkout-instances/count")
        .then()
          .statusCode(200)
          .body("count", equalTo(2));
    }

    @Test
    public void testAuditFilterByEvent() {
        String ok = """
            { "request": { "cartId": "CART-001", "customerId": "U", "amount": 1, "cartValid": true, "stockAvailable": true, "paymentSuccessful": true } }
            """;
        String bad = """
            { "request": { "cartId": "CART-005", "customerId": "U", "amount": 1, "cartValid": false, "stockAvailable": true, "paymentSuccessful": true } }
            """;
        given().contentType(ContentType.JSON).body(ok).when().post("/Process_Checkout").then().statusCode(201);
        given().contentType(ContentType.JSON).body(bad).when().post("/Process_Checkout").then().statusCode(201);

        given()
        .when()
          .get("/checkout-instances?event=completed")
        .then()
          .statusCode(200)
          .body("event", everyItem(equalTo("completed")));

        given()
        .when()
          .get("/checkout-instances?event=started")
        .then()
          .statusCode(200)
          .body("event", everyItem(equalTo("started")));
    }
}