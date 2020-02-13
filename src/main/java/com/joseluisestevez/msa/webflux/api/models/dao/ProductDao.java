package com.joseluisestevez.msa.webflux.api.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.joseluisestevez.msa.webflux.api.models.documents.Product;

public interface ProductDao extends ReactiveMongoRepository<Product, String> {

}
