package com.joseluisestevez.msa.webflux.api.controllers;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.joseluisestevez.msa.webflux.api.models.documents.Product;
import com.joseluisestevez.msa.webflux.api.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Value("${config.uploads.path}")
    private String path;

    @PostMapping("/v2")
    public Mono<ResponseEntity<Product>> createV2(Product product, @RequestPart FilePart file) {

        if (product.getCreateAt() == null) {
            product.setCreateAt(new Date());
        }
        product.setPhoto(UUID.randomUUID().toString() + "-" + file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
        return file.transferTo(new File(path + product.getPhoto())).then(productService.save(product))
                .map(p -> ResponseEntity.created(URI.create("/api/products/".concat(p.getId()))).contentType(MediaType.APPLICATION_JSON).body(p));

    }

    @PostMapping("/uploads/{id}")
    public Mono<ResponseEntity<Product>> uploads(@PathVariable String id, @RequestPart FilePart file) {
        return productService.findById(id).flatMap(p -> {
            p.setPhoto(UUID.randomUUID().toString() + "-" + file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
            return file.transferTo(new File(path + p.getPhoto())).then(productService.save(p));
        }).map(p -> {
            return ResponseEntity.ok(p);
        }).defaultIfEmpty(ResponseEntity.notFound().build());
    }

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
    public Mono<ResponseEntity<Map<String, Object>>> create(@Valid @RequestBody Mono<Product> monoProduct) {
        LOGGER.info("Create monoProduct = [{}]", monoProduct);

        Map<String, Object> responseMap = new HashMap<>();
        return monoProduct.flatMap(product -> {
            LOGGER.info("Create product = [{}]", product);
            if (product.getCreateAt() == null) {
                product.setCreateAt(new Date());
            }
            return productService.save(product).map(p -> {
                responseMap.put("product", p);
                responseMap.put("message", "Product saved successfully");
                responseMap.put("timestamp", new Date());

                LOGGER.info("Create responseMap = [{}]", responseMap);
                return ResponseEntity.created(URI.create("/api/products/".concat(p.getId()))).contentType(MediaType.APPLICATION_JSON)
                        .body(responseMap);
            });
        }).onErrorResume(t -> {
            LOGGER.info("Error t = [{}]", t);
            return Mono.just(t).cast(WebExchangeBindException.class).flatMap(e -> Mono.just(e.getFieldErrors())).flatMapMany(Flux::fromIterable)
                    .map(fieldError -> "The field " + fieldError.getField() + " " + fieldError.getDefaultMessage()).collectList().flatMap(list -> {
                        responseMap.put("errors", list);
                        responseMap.put("timestamp", new Date());
                        responseMap.put("status", HttpStatus.BAD_REQUEST.value());

                        LOGGER.info("Error in create responseMap = [{}]", responseMap);
                        return Mono.just(ResponseEntity.badRequest().body(responseMap));
                    });
        });
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
