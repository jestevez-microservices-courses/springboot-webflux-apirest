package com.joseluisestevez.msa.webflux.api;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.joseluisestevez.msa.webflux.api.models.documents.Category;
import com.joseluisestevez.msa.webflux.api.models.documents.Product;
import com.joseluisestevez.msa.webflux.api.service.ProductService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringbootWebfluxApirestApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootWebfluxApirestApplication.class);

    @Autowired
    private ProductService productService;
    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SpringbootWebfluxApirestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        reactiveMongoTemplate.dropCollection("products").subscribe();
        reactiveMongoTemplate.dropCollection("categories").subscribe();

        Category electronics = new Category("Electronics");
        Category computing = new Category("Computing");
        Category sport = new Category("Sport");
        Category furniture = new Category("Furniture");

        Flux.just(electronics, computing, sport, furniture).flatMap(productService::saveCategory)
                .doOnNext(c -> LOGGER.info("Category created [{}]", c))
                .thenMany(Flux.just(new Product("TV Panasonic Pantalla LCD", 456.89, electronics),
                        new Product("Sony Camara HD Digital", 177.89, electronics), new Product("Apple iPod", 46.89, electronics),
                        new Product("Sony Notebook", 846.89, computing), new Product("Hewlett Packard Multifuncional", 200.89, computing),
                        new Product("Bianchi Bicicleta", 70.89, sport), new Product("HP Notebook Omen 17", 2500.89, computing),
                        new Product("Mica Cómoda 5 Cajones", 150.89, furniture), new Product("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronics))
                        .flatMap(p -> {
                            p.setCreateAt(new Date());
                            return productService.save(p);
                        }))
                .subscribe(p -> LOGGER.info("Insert product id= [{}] name=[{}]", p.getId(), p.getName()));
    }

}
