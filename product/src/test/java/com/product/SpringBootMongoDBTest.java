package com.product;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.product.dao.ProductRepository;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class SpringBootMongoDBTest {
	@Autowired()
	private ProductRepository prodRepo;
	
	@Test
	public void printProducts() {
		System.out.println(prodRepo.findAll());
	}
}