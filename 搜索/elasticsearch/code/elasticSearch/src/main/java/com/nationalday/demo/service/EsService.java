package com.nationalday.demo.service;

import com.nationalday.demo.Dao.repository.EsProductRepository;
import com.nationalday.demo.es.EsProduct;
import lombok.Setter;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/11/8
 */

@Service
public class EsService {
    @Autowired
    private EsProductRepository esProductRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;




    public  void  testInsert(){
        EsProduct esProduct = new EsProduct();
        esProduct.setBrandId(1L);
        esProduct.setId(1L);
        esProduct.setBrandName("hello world ,little breast");
        esProduct.setProductCategoryName("yellow month month bird 川普和拜登谁将赢得大选");
        esProductRepository.save(esProduct);
    }


    public  void  testSearch(String keyword){
        Pageable pageable = PageRequest.of(0, 10);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        nativeSearchQueryBuilder.withQuery()
        .build();
        Page<EsProduct> esProductPage  =  esProductRepository.findByBrandName(keyword,pageable);
        elasticsearchRestTemplate.bulkIndex();
       /* QueryBuilder queryBuilder = new Q
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();\
        nativeSearchQueryBuilder.w*/

    }
}
