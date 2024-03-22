package com.abcbank.userinactivity.bankpocinactivitykstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abcbank.userinactivity.bankpocinactivitykstream.model.Customer;
import com.abcbank.userinactivity.bankpocinactivitykstream.model.CustomerNMinusTwoTimeStampMap;

public interface CustomerNMinusTwoTimeStampMapRepository extends JpaRepository<CustomerNMinusTwoTimeStampMap, Integer> {

}
