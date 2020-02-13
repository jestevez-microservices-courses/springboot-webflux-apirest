package com.joseluisestevez.msa.webflux.api.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.joseluisestevez.msa.webflux.api.models.documents.Category;

public interface CategoryDao extends ReactiveMongoRepository<Category, String> {

}
