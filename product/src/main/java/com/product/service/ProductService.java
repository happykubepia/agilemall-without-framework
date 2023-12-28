package com.product.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.product.config.IChannel;
import com.product.dao.ProductRepository;
import com.product.model.ChannelRequest;
import com.product.model.ChannelResponse;
import com.product.model.Product;
import com.product.model.ProductInventoryDTO;
import com.product.model.RequestDeliveryDetailDTO;
import com.product.model.ResultVO;

@Service
public class ProductService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private RabbitMessagingTemplate queueTemplate;
	
	public ResponseEntity<ResultVO<Product>> register(Product product) {
		productRepository.insert(product);
		
		return new ResponseEntity<ResultVO<Product>>(
				ResultVO.<Product>builder()
				.returnCode(true)
				.result(productRepository.findByProductName(product.getProductName()))
				.build(), HttpStatus.OK);
	}
	
	public ResponseEntity<ResultVO<Product>> search(String prodName) {
		log.info("search product => "+prodName);
		
		return new ResponseEntity<ResultVO<Product>>(
				ResultVO.<Product>builder()
				.returnCode(true)
				.result(productRepository.findByProductName(prodName))
				.build(), HttpStatus.OK);

	}

	public ResponseEntity<ResultVO<Product>> out(String prodName, int qty) {
		log.debug("product OUT => "+prodName+":"+qty);
		
		ResultVO<Product> result = null;
		try {
			/*
			 * MongoDB 갱신
			 */
			changeInventory(prodName, -qty);

			result = ResultVO.<Product>builder()
					.returnCode(true)
					.result(productRepository.findByProductName(prodName))
					.build();
		} catch (Exception e) {
			result = ResultVO.<Product>builder()
					.returnCode(false)
					.returnMessage(e.getMessage())
					.build();
		}

		return new ResponseEntity<ResultVO<Product>>(result, HttpStatus.OK);
	}
	

	public ResponseEntity<ResultVO<Product>> in(String prodName, int qty) {
		log.debug("product IN => "+prodName+":"+qty);
		
		ResultVO<Product> result = null;
		try {
			/*
			 * MongoDB 갱신
			 */
			changeInventory(prodName, qty);

			result = ResultVO.<Product>builder()
					.returnCode(true)
					.result(productRepository.findByProductName(prodName))
					.build();
		} catch (Exception e) {
			result = ResultVO.<Product>builder()
					.returnCode(false)
					.returnMessage(e.getMessage())
					.build();
		}

		return new ResponseEntity<ResultVO<Product>>(result, HttpStatus.OK);
	}		

	private Product changeInventory(String prodName, int qty) {
		Product prod = productRepository.findByProductName(prodName);

		if ( prod != null ) {
			prod.setInventoryQty(prod.getInventoryQty() + qty);
			productRepository.save(prod);
		}
		return prod;
	}
	
	private boolean checkInventoryQty(Gson gs, ChannelRequest<ProductInventoryDTO> req) {		
		ProductInventoryDTO payload = req.getPayload();
		Product prod = null;
		String prodName = "";
		int qty = 0;		//차감할 재고량 
		
		for(RequestDeliveryDetailDTO item:payload.getProducts()) {
			prodName = item.getProductName();
			qty = item.getQty();
			
			prod = productRepository.findByProductName(prodName);
			if(prod == null) {
				ChannelResponse<ProductInventoryDTO> res = new ChannelResponse<>();
				res.setTrxId(req.getTrxId());
				res.setMessageType("PRODUCT");
				res.setReturnCode(false);
				res.setErrorString("제품명 <"+prodName+">에 대한 정보를 찾을 수 없음");
				queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
				return false;
			}
			//주문량이 재고량보다 많으면 에러를 리턴함 
			if(prod.getInventoryQty() < qty) {
				ChannelResponse<ProductInventoryDTO> res = new ChannelResponse<>();
				res.setTrxId(req.getTrxId());
				res.setMessageType("PRODUCT");
				res.setReturnCode(false);
				res.setErrorString("제품명 <"+prodName+"의 현재 재고량이 부족하여 주문 불가 합니다. ");
				queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
				return false;
			}
		}
		return true;
	}

	/**
	 * 재고량 처리를 수행한다.
	*/
	
	public void startTransaction(Gson gs, ChannelRequest<ProductInventoryDTO> req)
	{
		/*
		 * 재고량 감소 처리 및 결과 반환
		 */

		try {
			ProductInventoryDTO payload = req.getPayload();
			Product prod = null;
			String prodName = "";
			int qty = 0;		//차감할 재고량 
			List<RequestDeliveryDetailDTO> inventoryList = new ArrayList<RequestDeliveryDetailDTO>();
			RequestDeliveryDetailDTO inventory = null;

			//주문량이 재고량보다 많으면 에러를 리턴함 
			if(!checkInventoryQty(gs, req)) {
				return;
			}
			
			for(RequestDeliveryDetailDTO item:payload.getProducts()) {
				prodName = item.getProductName();
				qty = item.getQty();
				
				//주문량 감소 처리 
				prod = changeInventory(prodName, -1 * qty);
				
				inventory = new RequestDeliveryDetailDTO();
				inventory.setProductName(prodName);
				inventory.setQty(prod.getInventoryQty());
				inventoryList.add(inventory);
			}
			
			/*
			 * 처리 결과를 메시지 브로커로 전송한다. 
			 */
			ProductInventoryDTO resPayload = new ProductInventoryDTO();
			resPayload.setOrderId(payload.getOrderId());
			resPayload.setProducts(inventoryList);

			ChannelResponse<ProductInventoryDTO> res = new ChannelResponse<>();
			res.setTrxId(req.getTrxId());
			res.setMessageType("PRODUCT");
			res.setReturnCode(true);
			res.setPayload(resPayload);

			queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
		}catch(Exception e) {
			ChannelResponse<ProductInventoryDTO> res = new ChannelResponse<>();
			res.setTrxId(req.getTrxId());
			res.setMessageType("PRODUCT");
			res.setReturnCode(false);
			res.setErrorString(e.getMessage());
			queueTemplate.convertAndSend(IChannel.CH_ORDER_RESPONSE, gs.toJson(res));
		}
	}

	/**
	 * 주문 ID를 이용하여 포인트 차감을 취소한다.
	 * 
	 * @param req
	 */
	public void rollback(ChannelRequest<ProductInventoryDTO> req)
	{
		ProductInventoryDTO payload = req.getPayload();
		
		try {
			String prodName = "";
			int qty = 0;		//차감 취소할 재고량 
			for(RequestDeliveryDetailDTO item:payload.getProducts()) {
				prodName = item.getProductName();
				qty = item.getQty();
				changeInventory(prodName, qty);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}

