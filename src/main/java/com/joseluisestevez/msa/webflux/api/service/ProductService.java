package com.joseluisestevez.msa.webflux.api.service;

import com.joseluisestevez.msa.webflux.api.models.documents.Category;
import com.joseluisestevez.msa.webflux.api.models.documents.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
    Flux<Product> findAll();

    Mono<Product> findById(String id);

    Mono<Product> save(Product product);

    Mono<Void> delete(Product product);

    Flux<Product> findAllWitNameUppercase();

    Flux<Product> findAllWitNameUppercaseRepeat();

    Flux<Category> findAllCategory();

    Mono<Category> findCategoryById(String id);

    Mono<Category> saveCategory(Category category);

    Mono<Product> findByName(String name);

    Mono<Category> findCategoryByName(String name);

}
