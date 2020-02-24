package com.joseluisestevez.msa.webflux.api;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.joseluisestevez.msa.webflux.api.models.documents.Product;
import com.joseluisestevez.msa.webflux.api.service.ProductService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringbootWebfluxApirestApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductService productService;

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

    @Test
    void testView() {
        String productName = "TV Panasonic Pantalla LCD";
        Product product = productService.findByName(productName).block(); // block sincrono
        webTestClient.get().uri("/api/products/{id}", Collections.singletonMap("id", product.getId())).accept(MediaType.APPLICATION_JSON).exchange()
                .expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo(productName);
    }

}
