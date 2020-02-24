package com.joseluisestevez.msa.webflux.api;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.joseluisestevez.msa.webflux.api.models.documents.Category;
import com.joseluisestevez.msa.webflux.api.models.documents.Product;
import com.joseluisestevez.msa.webflux.api.service.ProductService;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringbootWebfluxApirestApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootWebfluxApirestApplicationTests.class);

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
                .expectStatus().isOk().expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody(Product.class).consumeWith(response -> {
                    Product productResponse = response.getResponseBody();
                    Assertions.assertNotNull(productResponse.getName());
                    Assertions.assertTrue(productResponse.getName().length() > 0);
                    Assertions.assertEquals(productName, productResponse.getName());
                })
        // .expectBody().jsonPath("$.id").isNotEmpty()
        // .jsonPath("$.name").isEqualTo(productName)
        ;
    }

    @Test
    void testCreate() {
        String productName = "Alexa";
        String categoryName = "Electronics";
        Category category = productService.findCategoryByName(categoryName).block();

        LOGGER.info("category=[{}]", category);
        Product product = new Product(productName, 29.99, category);
        webTestClient.post().uri("/api/products").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(product), Product.class).exchange().expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.product.id").isNotEmpty().jsonPath("$.product.name").isEqualTo(productName)
                .jsonPath("$.product.category.name").isEqualTo(categoryName);
    }

}
