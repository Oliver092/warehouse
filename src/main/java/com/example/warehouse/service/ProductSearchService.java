package com.example.warehouse.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import com.example.warehouse.entity.Product;
import com.example.warehouse.entity.ProductDocument;
import com.example.warehouse.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    public void indexProduct(Product product) {
        ProductDocument doc = toDocument(product);
        productSearchRepository.save(doc);
    }

    public void deleteFromIndex(Long productId) {
        productSearchRepository.deleteById(String.valueOf(productId));
    }

    public List<ProductDocument> search(String query) {
        return productSearchRepository.findByNameContainingOrSkuContaining(query, query);
    }

    public void reindexAll(List<Product> allProducts) {
        productSearchRepository.deleteAll();
        List<ProductDocument> docs = allProducts.stream()
                .map(this::toDocument)
                .toList();
        productSearchRepository.saveAll(docs);
    }

    public List<ProductDocument> fuzzySearch(String query) {
        Query searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .fuzzy(f -> f
                                .field("name")
                                .value(query)
                                .fuzziness("AUTO")
                        )
                )
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations
                .search(searchQuery, ProductDocument.class);

        return hits.stream()
                .map(SearchHit::getContent)
                .toList();
    }

    public Map<String, Long> getStatisticsByHall() {
        Query query = NativeQuery.builder()
                .withAggregation("hall_statistics", Aggregation.of(a -> a
                        .terms(t -> t.field("hallName"))
                ))
                .withMaxResults(0)
                .build();

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(query, ProductDocument.class);

        ElasticsearchAggregations aggregations =
                (ElasticsearchAggregations) hits.getAggregations();

        assert aggregations != null;
        return aggregations
                .aggregationsAsMap()
                .get("hall_statistics")
                .aggregation()
                .getAggregate()
                .sterms()
                .buckets()
                .array()
                .stream()
                .collect(Collectors.toMap(
                        bucket -> bucket.key().stringValue(), // FieldValue → String
                        StringTermsBucket::docCount
                ));
    }

    private ProductDocument toDocument(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setId(String.valueOf(product.getId()));
        doc.setName(product.getName());
        doc.setSku(product.getSku());
        doc.setQuantity(product.getQuantity());
        doc.setReorderThreshold(product.getReorderThreshold());
        doc.setMaxQuantity(product.getMaxQuantity());

        if (product.getShelf() != null) {
            doc.setShelfCode(product.getShelf().getCode());
            if (product.getShelf().getAisle() != null) {
                doc.setAisleName(product.getShelf().getAisle().getName());
                if (product.getShelf().getAisle().getHall() != null) {
                    doc.setHallName(product.getShelf().getAisle().getHall().getName());
                }
            }
        }
        return doc;
    }
}
