package com.joseluisestevez.msa.webflux.api.controllers;

import java.net.URI;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.joseluisestevez.msa.webflux.api.models.documents.Product;
import com.joseluisestevez.msa.webflux.api.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Product>>> list() {
        return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(productService.findAll()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> view(@PathVariable String id) {
        return productService.findById(id).map(p -> {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(p);
        }).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Product>> create(@RequestBody Product product) {

        if (product.getCreateAt() == null) {
            product.setCreateAt(new Date());
        }

        return productService.save(product)
                .map(p -> ResponseEntity.created(URI.create("/api/products/".concat(p.getId()))).contentType(MediaType.APPLICATION_JSON).body(p));

    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> edit(@PathVariable String id, @RequestBody Product product) {
        return productService.findById(id).flatMap(p -> {
            p.setName(product.getName());
            p.setPrice(product.getPrice());
            p.setCategory(product.getCategory());
            return productService.save(p);
        }).map(p -> ResponseEntity.created(URI.create("/api/products/".concat(p.getId()))).contentType(MediaType.APPLICATION_JSON).body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return productService.findById(id).flatMap(p -> {
            return productService.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

}
