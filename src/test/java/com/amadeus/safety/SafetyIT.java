package com.amadeus.safety;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.SafePlace;
import com.github.tomakehurst.wiremock.WireMockServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//https://developers.amadeus.com/blog/announcing-safe-place-api-geosure
public class SafetyIT {

  WireMockServer wireMockServer;

  private Amadeus amadeus;

  /**
   * In every tests, we will authenticate.
   */
  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(8080);
    wireMockServer.start();

    //https://developers.amadeus.com/self-service/apis-docs/guides/authorization-262
    String address = "/v1/security/oauth2/token"
        + "?grant_type=client_credentials&client_secret=DEMO&client_id=DEMO";
    wireMockServer.stubFor(post(urlEqualTo(address))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
        .withStatus(200)
        .withBodyFile("auth_ok.json")));

    amadeus = Amadeus
      .builder("DEMO", "DEMO")
      .setHost("localhost")
      .setPort(8080)
      .setSsl(false)
      .setLogLevel("debug")
      .build();
  }

  @AfterEach
  public void teardown() {
    wireMockServer.stop();
  }

  @Test
  public void givenClientWhenCallSafetyRateLocationBySquareWithParamsThenOK()
      throws ResponseException {

    //Given
    String address = "/v1/safety/safety-rated-locations/by-square"
        + "?east=2.177181&south=41.394582&north=41.397158&west=2.160873";
    wireMockServer.stubFor(get(urlEqualTo(address))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
        .withStatus(200)
        .withBodyFile("safety_rate_location_by_square_response_ok.json")));

    Params params = Params
        .with("north", "41.397158")
        .and("west", "2.160873")
        .and("south", "41.394582")
        .and("east", "2.177181");

    //When
    SafePlace[] result = amadeus.safety.safetyRatedLocations.bySquare.get(params);

    //Then
    assertNotEquals(0, result.length);
  }

  @Test
  public void givenClientWhenCallSafetyRateLocationBySquareWithoutParamsThenOK()
      throws ResponseException {

    //Given
    wireMockServer.stubFor(get(urlEqualTo("/v1/safety/safety-rated-locations/by-square"))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
        .withStatus(200)
        .withBodyFile("safety_rate_location_by_square_response_ok.json")));

    //When
    SafePlace[] result = amadeus.safety.safetyRatedLocations.bySquare.get();

    //Then
    assertNotEquals(0, result.length);
  }

  @Test
  public void givenClientWhenCallSafetyRateLocationThenOK() throws ResponseException {

    //Given
    wireMockServer.stubFor(get(urlEqualTo("/v1/safety/safety-rated-locations"
        + "?latitude=41.39715&longitude=2.160873"))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
        .withStatus(200)
        .withBodyFile("safety_rate_location_by_square_response_ok.json")));

    Params params = Params
        .with("latitude", "41.39715")
        .and("longitude", "2.160873");

    //When
    SafePlace[] result = amadeus.safety.safetyRatedLocations.get(params);

    //Then
    assertNotEquals(0, result.length);
  }

  @Test
  public void givenClientWhenCallSafetyRateLocationByIdThenOK() throws ResponseException {

    //Given
    wireMockServer.stubFor(get(urlEqualTo("/v1/safety/safety-rated-locations/Q930402719"))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
        .withStatus(200)
        .withBodyFile("safety_rated_location_id_response_ok.json")));

    String id = "Q930402719";

    //When
    SafePlace result = amadeus.safety.safetyRatedLocation(id).get();

    //Then
    assertNotNull(result);
  }

}
