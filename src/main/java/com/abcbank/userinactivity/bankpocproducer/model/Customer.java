package com.abcbank.userinactivity.bankpocproducer.model;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "customer")

public class Customer {
	@Id
	public Integer accno;

	public String description;

	public Timestamp timestamp;

	public String password;
	
	public Float transaction;

}