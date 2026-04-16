package com.example.warehouse.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(type = FieldType.Integer)
    private Integer quantity;

    @Field(type = FieldType.Integer)
    private Integer reorderThreshold;

    @Field(type = FieldType.Integer)
    private Integer maxQuantity;

    @Field(type = FieldType.Keyword)
    private String shelfCode;

    @Field(type = FieldType.Text)
    private String aisleName;

    @Field(type = FieldType.Text)
    private String hallName;
}
