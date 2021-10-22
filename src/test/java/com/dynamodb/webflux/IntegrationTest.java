package com.dynamodb.webflux;

import com.dynamodb.webflux.domain.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
public class IntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Value("${config.base.endpoint}")
    private String url;

    @Test
    public void getPerson() {
        webTestClient
                .get().uri(url + "1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(null);
    }

    @Test
    public void savePerson() throws InterruptedException {
        Person person = new Person("10", "Milton");
        webTestClient
                .post().uri(url)
                .body(Mono.just(person), Person.class)
                .exchange()
                .expectStatus().isOk();
        Thread.sleep(1500);
        webTestClient
                .get().uri(url + "10")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Person.class).isEqualTo(person);
    }
}