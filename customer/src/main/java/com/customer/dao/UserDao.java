package com.customer.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.customer.model.User;
import com.customer.model.UserPointDTO;

@Mapper
@Repository
public interface UserDao {

	List<User> selectUserAll() throws Exception ;

	User selectUser(String userId) throws Exception;

	int insertUser(User user) throws Exception;

	int updateUser(User user) throws Exception;

	int deleteUser(String userId) throws Exception;

	void updatePoint(UserPointDTO userPoint);
}