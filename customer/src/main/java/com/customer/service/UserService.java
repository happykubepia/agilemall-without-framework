package com.customer.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.customer.model.ResultVO;
import com.customer.model.User;
import com.customer.model.UserPointDTO;

public interface UserService {
	public ResponseEntity <List<User>> getUserList();

	public ResponseEntity <User> getUserById(String userId);
	
	public ResponseEntity <ResultVO<User>> get(String userId);

	public ResponseEntity <String > setUserUpdate(String userId, User user) throws Exception;

	public ResponseEntity <String > setUserInsert(User user) throws Exception;

	public ResponseEntity <String > setUserDelete(String userId) throws Exception;

	void updatePoint(UserPointDTO userPoint);
}
