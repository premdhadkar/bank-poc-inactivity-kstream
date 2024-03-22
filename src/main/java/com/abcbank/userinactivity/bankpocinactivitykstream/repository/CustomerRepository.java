package com.abcbank.userinactivity.bankpocinactivitykstream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abcbank.userinactivity.bankpocinactivitykstream.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}
