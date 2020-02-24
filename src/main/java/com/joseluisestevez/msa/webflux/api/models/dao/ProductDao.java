package com.joseluisestevez.msa.webflux.api.models.dao;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.joseluisestevez.msa.webflux.api.models.documents.Product;

import reactor.core.publisher.Mono;

public interface ProductDao extends ReactiveMongoRepository<Product, String> {

    Mono<Product> findByName(String name);

    @Query("{'name':?0}")
    Mono<Product> getProductWithQuery(String name);
}
