package com.joseluisestevez.msa.webflux.api;

import java.util.List;

import org.junit.jupiter.api.Assertions;
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
                .contentType(MediaType.APPLICATION_JSON).expectBodyList(Product.class).consumeWith(response -> {
                    List<Product> products = response.getResponseBody();
                    products.forEach(product -> {
                        System.out.println(product.getName());
                    });
                    // Assertions.assertEquals(9, products.size());
                    Assertions.assertEquals(false, products.isEmpty());
                });
        // .hasSize(9);
    }

}
