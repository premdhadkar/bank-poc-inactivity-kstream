package com.abcbank.userinactivity.bankpocinactivitykstream;

import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.SessionWindows;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.abcbank.userinactivity.bankpocinactivitykstream.repository.CustomerRepository;
import com.abcbank.userinactivity.bankpocproducer.model.Customer;

@SpringBootApplication
public class BankPocInactivityKstreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankPocInactivityKstreamApplication.class, args);
	}
	
	@Autowired
	CustomerRepository customerRepository;

	@SuppressWarnings("deprecation")
	@Bean
	BiConsumer<KStream<Integer, Customer>, KStream<Integer, Customer>> heartbeatBinder1() {
		return (heartBeatStream, accountTable) -> {

			heartBeatStream.groupByKey()
					.windowedBy(SessionWindows.ofInactivityGapAndGrace(Duration.ofSeconds(30), Duration.ZERO))
//			.windowedBy(TimeWindows.of(Duration.ofSeconds(60)))
					.count()
//			.filter((userId, count) -> count == 0)
					.toStream()
//			.foreach((k, v) -> System.out.println(k + " " + v));
					.foreach((k, v) -> System.out.println(k + " " + v));

		};
	}

	@Bean
	BiConsumer<KStream<Integer, Customer>, KTable<Integer, Customer>> heartbeatBinder() {
		return (heartBeatStream, accountTable) -> {
			List<Customer> allCustomers = customerRepository.findAll();
			
			allCustomers.forEach(customer->{
				new Thread(()->{
					//thread for every customer
				}).start();
			});
			
		};
	}
}
