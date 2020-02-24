package com.joseluisestevez.msa.webflux.api.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.joseluisestevez.msa.webflux.api.models.documents.Category;

import reactor.core.publisher.Mono;

public interface CategoryDao extends ReactiveMongoRepository<Category, String> {
    Mono<Category> findByName(String name);
}
