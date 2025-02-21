package com.amadeus.dutyofCare.diseases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ClientException;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.DiseaseReport;
import com.github.tomakehurst.wiremock.WireMockServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// https://developers.amadeus.com/self-service/category/covid-19-and-travel-safety/api-doc/travel-restrictions/api-reference
public class Covid19ReportIT {
  WireMockServer wireMockServer;

  private Amadeus amadeus;

  /**
   * In every test, we will authenticate.
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
  public void givenClientWhenCallTravelRestrictionsV2WithParamsThenOK()
      throws ResponseException {

    //Given
    String address = "/v2/duty-of-care/diseases/covid19-area-report"
        + "?countryCode=US&cityCode=NYC&language=EN";
    wireMockServer.stubFor(get(urlEqualTo(address))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
        .withStatus(200)
        .withBodyFile("travel_restrictions_v2_response_ok.json")));

    //When
    DiseaseReport result = amadeus.dutyOfCare.diseases.covid19Report.get(
        Params.with("countryCode", "US").and("cityCode", "NYC").and("language", "EN")
    );

    //Then
    assertNotNull(result);
  }

  //TODO Review with the team to upgrade the behaviour.
  @Test
  public void givenClientWhenCallTravelRestrictionsV2WithoutParamsThenOK() {

    //Given
    String address = "/v2/duty-of-care/diseases/covid19-area-report";
    wireMockServer.stubFor(get(urlEqualTo(address))
        .willReturn(aResponse().withHeader("Content-Type", "application/json")
        .withStatus(400)
        .withBody("")));

    //When
    //Then
    assertThatThrownBy(() -> {
      amadeus.dutyOfCare.diseases.covid19Report.get();
    }).isInstanceOf(ClientException.class);
  }
}

