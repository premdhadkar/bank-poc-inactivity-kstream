package com.abcbank.userinactivity.bankpocinactivitykstream.model;

import com.abcbank.userinactivity.bankpocproducer.model.Customer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Aggregate {
	Customer heartbeat;
	Customer signUp;
}
