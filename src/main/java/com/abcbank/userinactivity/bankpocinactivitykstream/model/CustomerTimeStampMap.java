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
@Entity(name = "customer_timestamp_map")
public class CustomerTimeStampMap {
	@Id
	public Integer accno;

	public Timestamp lastTimestamp;

}