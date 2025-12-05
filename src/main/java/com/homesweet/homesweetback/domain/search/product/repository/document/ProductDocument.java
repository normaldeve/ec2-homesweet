package com.homesweet.homesweetback.domain.search.product.repository.document;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 Elastic 매핑 (개선 적용 버전)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setting(settingPath = "/elasticsearch/product-settings.json")
@Document(indexName = "products")
public class ProductDocument {

    @Id
    @Field(type = FieldType.Long, name = "product_id")
    private Long productId;

    /** -------------------------
     *  정규 검색 필드
     *  ------------------------- */
    @Field(type = FieldType.Text,
            analyzer = "product_search_analyzer",
            searchAnalyzer = "product_search_analyzer")
    private String name;

    /** -------------------------
     *  부분검색(Ngram) 필드
     *  검색 시에는 사용되지 않도록 searchAnalyzer=standard 설정
     *  ------------------------- */
    @Field(type = FieldType.Text,
            name = "name.ngram",
            analyzer = "product_ngram_analyzer",
            searchAnalyzer = "standard")
    private String nameNgram;

    /** -------------------------
     *  자동완성(prefix) 필드
     *  edge_ngram 기반의 autocomplete_analyzer 사용
     *  ------------------------- */
    @Field(type = FieldType.Text,
            name = "name.autocomplete",
            analyzer = "autocomplete_analyzer",
            searchAnalyzer = "standard")
    private String nameAutocomplete;

    /** -------------------------
     *  완전일치 검색용 Keyword 필드
     *  ------------------------- */
    @Field(type = FieldType.Keyword, name = "name.keyword")
    private String nameKeyword;

    @Field(type = FieldType.Keyword)
    private String brand;

    /** -------------------------
     *  description 검색 제외 (성능 핵심 개선)
     *  ------------------------- */
    @Field(type = FieldType.Text, index = false)
    private String description;

    @Field(type = FieldType.Integer, name = "base_price")
    private Integer basePrice;

    @Field(type = FieldType.Float, name = "discount_rate")
    private Float discountRate;

    @Field(type = FieldType.Integer, name = "sale_price")
    private Integer salePrice;

    @Field(type = FieldType.Integer, name = "shipping_price")
    private Integer shippingPrice;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword, name = "image_url")
    private String imageUrl;

    @Field(type = FieldType.Long, name = "category_id")
    private Long categoryId;

    @Field(type = FieldType.Keyword, name = "category_name")
    private String categoryName;

    /** 카테고리 텍스트 검색용 필드 */
    @Field(type = FieldType.Text,
            name = "category_name.text",
            analyzer = "product_search_analyzer")
    private String categoryNameText;

    @Field(type = FieldType.Float, name = "average_rating")
    private Double averageRating;

    @Field(type = FieldType.Long, name = "review_count")
    private Long reviewCount;

    @Field(type = FieldType.Date,
            name = "created_at",
            format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date,
            name = "updated_at",
            format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Nested, name = "option_groups")
    private List<OptionGroup> optionGroups;


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionGroup {

        @Field(type = FieldType.Keyword, name = "group_name")
        private String groupName;

        @Field(type = FieldType.Keyword, name = "values")
        private List<String> values;
    }
}