package com.abcbank.userinactivity.bankpocinactivitykstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abcbank.userinactivity.bankpocinactivitykstream.model.CustomerTimeStampMap;

public interface CustomerTimeStampMapRepository extends JpaRepository<CustomerTimeStampMap, Integer> {

}
