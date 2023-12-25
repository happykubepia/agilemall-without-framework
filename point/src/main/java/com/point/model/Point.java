package com.point.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Document(collection = "point")
public class Point {
  @Id
  private String id;
  private String userId;
  private int userPoint;
}
