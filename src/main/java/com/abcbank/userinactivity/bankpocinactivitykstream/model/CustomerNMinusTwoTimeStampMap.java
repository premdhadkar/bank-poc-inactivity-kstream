package com.abcbank.userinactivity.bankpocinactivitykstream.model;

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
@Entity(name = "customer_two_timestamp_map")
public class CustomerNMinusTwoTimeStampMap {
	@Id
	public Integer accno;

	public Timestamp Ntimestamp;

	public Timestamp NMinus1Timestamp;

}