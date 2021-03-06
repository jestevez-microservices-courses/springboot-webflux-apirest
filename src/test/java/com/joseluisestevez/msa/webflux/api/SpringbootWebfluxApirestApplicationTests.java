package com.joseluisestevez.msa.webflux.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joseluisestevez.msa.webflux.api.models.documents.Category;
import com.joseluisestevez.msa.webflux.api.models.documents.Product;
import com.joseluisestevez.msa.webflux.api.service.ProductService;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) // WebEnvironment.MOCK - always fail first test?
class SpringbootWebfluxApirestApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootWebfluxApirestApplicationTests.class);

    @Value("${config.base.endpoint}")
    private String uri;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductService productService;

    @Test
    void testList() {
        webTestClient.get().uri(uri).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
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
        webTestClient.get().uri(uri + "/{id}", Collections.singletonMap("id", product.getId())).accept(MediaType.APPLICATION_JSON).exchange()
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
        webTestClient.post().uri(uri).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(product), Product.class).exchange().expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.product.id").isNotEmpty().jsonPath("$.product.name").isEqualTo(productName)
                .jsonPath("$.product.category.name").isEqualTo(categoryName);
    }

    @Test
    void testCreate2() {
        String productName = "Nintendo 64";
        String categoryName = "Electronics";
        Category category = productService.findCategoryByName(categoryName).block();
        ObjectMapper objectMapper = new ObjectMapper();
        LOGGER.info("category=[{}]", category);
        Product product = new Product(productName, 29.99, category);
        webTestClient.post().uri(uri).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(product), Product.class).exchange().expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
                }).consumeWith(response -> {
                    Object object = response.getResponseBody().get("product");

                    Product productResponse = objectMapper.convertValue(object, Product.class);
                    Assertions.assertNotNull(productResponse.getName());
                    Assertions.assertTrue(productResponse.getName().length() > 0);
                    Assertions.assertEquals(productName, productResponse.getName());
                    Assertions.assertNotNull(productResponse.getCategory());
                    Assertions.assertEquals(categoryName, productResponse.getCategory().getName());
                });
    }

    @Test
    void testEdit() {
        String productName = "TV Sony Bravia OLED 4K Ultra HD";
        Product product = productService.findByName(productName).block(); // block sincrono

        String categoryName = "Electronics";
        Category category = productService.findCategoryByName(categoryName).block();

        Product productEdited = new Product("Asus Notebook", 700.00, category);

        webTestClient.put().uri(uri + "/{id}", Collections.singletonMap("id", product.getId())).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).body(Mono.just(productEdited), Product.class).exchange().expectStatus().isCreated().expectHeader()
                .contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.id").isNotEmpty().jsonPath("$.name").isEqualTo("Asus Notebook")
                .jsonPath("$.category.name").isEqualTo(categoryName);
    }

    @Test
    void testDelete() {
        String productName = "Mica Cómoda 5 Cajones";
        Product product = productService.findByName(productName).block(); // block sincrono

        webTestClient.delete().uri(uri + "/{id}", Collections.singletonMap("id", product.getId())).exchange().expectStatus().isNoContent()
                .expectBody().isEmpty();

        webTestClient.get().uri(uri + "/{id}", Collections.singletonMap("id", product.getId())).exchange().expectStatus().isNotFound().expectBody()
                .isEmpty();

    }

}
