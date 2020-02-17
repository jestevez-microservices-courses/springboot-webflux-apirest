package com.joseluisestevez.msa.webflux.api.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.net.URI;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.joseluisestevez.msa.webflux.api.models.documents.Product;
import com.joseluisestevez.msa.webflux.api.service.ProductService;

import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

    @Autowired
    private ProductService productService;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(productService.findAll(), Product.class);
    }

    public Mono<ServerResponse> view(ServerRequest request) {
        String id = request.pathVariable("id");
        return productService.findById(id).flatMap(product -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromValue(product)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<Product> product = request.bodyToMono(Product.class);

        return product.flatMap(p -> {
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return productService.save(p);
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId()))).contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(p)));

    }

    public Mono<ServerResponse> edit(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> product = request.bodyToMono(Product.class);
        Mono<Product> productDB = productService.findById(id);

        return productDB.zipWith(product, (db, req) -> {
            db.setName(req.getName());
            db.setPrice(req.getPrice());
            db.setCategory(req.getCategory());
            return db;
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId()))).contentType(MediaType.APPLICATION_JSON)
                .body(productService.save(p), Product.class)).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> productDB = productService.findById(id);
        return productDB.flatMap(p -> productService.delete(p).then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
