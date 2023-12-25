package com.product.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.product.model.*;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
	public Product findByProductName(String productName);
}
