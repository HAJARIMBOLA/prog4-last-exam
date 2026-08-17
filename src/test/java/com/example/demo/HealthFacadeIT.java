package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

class HealthFacadeIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;

  @LocalServerPort private int port;

  @Test
  void pingRespondsOkAgainstTheRealEmbeddedServerAndRealPostgres() {
    var response = restTemplate.getForEntity("http://localhost:" + port + "/ping", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
