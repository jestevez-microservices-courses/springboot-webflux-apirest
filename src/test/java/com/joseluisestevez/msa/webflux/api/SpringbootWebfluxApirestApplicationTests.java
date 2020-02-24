package com.joseluisestevez.msa.webflux.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.joseluisestevez.msa.webflux.api.models.documents.Product;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringbootWebfluxApirestApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testList() {
        webTestClient.get().uri("/api/products").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
                .contentType(MediaType.APPLICATION_JSON).expectBodyList(Product.class).hasSize(9);
    }

}
