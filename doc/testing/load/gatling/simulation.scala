package com.example.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ApiLoadSimulation extends Simulation {

  // Base configuration
  val baseUrl = "http://localhost:8080"
  val apiPath = "/api"
  val contentType = "application/json"
  
  // Performance test configuration
  val userCount = Integer.getInteger("users", 100)
  val rampDuration = Integer.getInteger("ramp", 30)
  val testDuration = Integer.getInteger("duration", 300)

  // HTTP configuration
  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader(contentType)
    .userAgentHeader("Gatling/Performance-Test")

  // Scenario for getting all items
  val getAllItems = exec(http("Get All Items")
    .get(s"$apiPath/items")
    .check(status.is(200))
  )

  // Scenario for getting a single item
  val getItemById = exec(http("Get Item by ID")
    .get(s"$apiPath/items/1")
    .check(status.is(200))
  )

  // Scenario for creating a new item
  val createItem = exec(http("Create Item")
    .post(s"$apiPath/items")
    .body(StringBody("""{"name":"Test Item","description":"This is a test item created by Gatling","price":19.99}"""))
    .check(status.is(201))
  )

  // Scenario for updating an item
  val updateItem = exec(http("Update Item")
    .put(s"$apiPath/items/1")
    .body(StringBody("""{"name":"Updated Item","description":"This item was updated by Gatling","price":29.99}"""))
    .check(status.is(200))
  )

  // Combined scenario with different operations
  val scn = scenario("API Load Test")
    .exec(getAllItems)
    .pause(1)
    .exec(getItemById)
    .pause(1)
    .exec(createItem)
    .pause(1)
    .exec(updateItem)

  // Simulation setup
  setUp(
    scn.inject(
      rampUsers(userCount).during(rampDuration.seconds)
    ).protocols(httpProtocol)
  ).maxDuration(testDuration.seconds)
    .assertions(
      global.responseTime.max.lt(5000),    // Max response time less than 5 seconds
      global.successfulRequests.percent.gt(95)  // Success rate greater than 95%
    )
}