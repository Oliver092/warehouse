package com.example.warehouse.repository;

import com.example.warehouse.entity.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, String> {

    List<ProductDocument> findByNameContainingOrSkuContaining(String name, String sku);

    List<ProductDocument> findByHallName(String hallName);
}
