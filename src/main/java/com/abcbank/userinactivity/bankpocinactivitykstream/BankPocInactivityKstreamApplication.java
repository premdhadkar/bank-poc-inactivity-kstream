package com.abcbank.userinactivity.bankpocinactivitykstream;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.abcbank.userinactivity.bankpocinactivitykstream.model.Customer;
import com.abcbank.userinactivity.bankpocinactivitykstream.model.CustomerNMinusTwoTimeStampMap;
import com.abcbank.userinactivity.bankpocinactivitykstream.repository.CustomerNMinusTwoTimeStampMapRepository;
import com.abcbank.userinactivity.bankpocinactivitykstream.repository.CustomerRepository;

@EnableScheduling
@SpringBootApplication
public class BankPocInactivityKstreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankPocInactivityKstreamApplication.class, args);
	}

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	CustomerNMinusTwoTimeStampMapRepository tsMapRepository;

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

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Bean
	BiConsumer<KStream<Integer, com.abcbank.userinactivity.bankpocproducer.model.Customer>, KTable<Integer, Customer>> heartbeatBinder() {
		/*
		 * Problem with this approach will be, if a new user signs up in realtime, then
		 * it will not monitor its heartbeats.
		 */
		return (heartBeatStream, accountTable) -> {
			List<Customer> allCustomers = customerRepository.findAll();
			List<KStream<Integer, com.abcbank.userinactivity.bankpocproducer.model.Customer>> customerStream = new ArrayList<>();
			tsMapRepository.deleteAll();
			allCustomers.forEach(i -> {
				customerStream.add(heartBeatStream.branch((key, value) -> key == i.getAccno())[0]);
				tsMapRepository.save(CustomerNMinusTwoTimeStampMap.builder().accno(i.getAccno()).build());
			});

			for (KStream<Integer, com.abcbank.userinactivity.bankpocproducer.model.Customer> kstream : customerStream) {
				kstream.foreach((key, value) -> {
					Optional<CustomerNMinusTwoTimeStampMap> tsMapOptional = tsMapRepository.findById(key);
					tsMapOptional.ifPresent((tsMap) -> {
						tsMap.setNMinus1Timestamp(tsMap.getNtimestamp());
						tsMap.setNtimestamp(value.getTimestamp());
						tsMapRepository.save(tsMap);
					});
				});
			}
		};
	}

	@Scheduled(fixedRate = 30000) // 30 seconds in milliseconds
	public void myTask() {
		tsMapRepository.findAll().forEach(tsMap -> {
			Long nTS = tsMap.getNtimestamp() == null ? null : tsMap.getNtimestamp().getTime();
			Long currentTs = Timestamp.from(Instant.now()).getTime();
			if (nTS == null || currentTs - nTS > 21000) {
				System.out.println("User: " + tsMap.getAccno() + " No heartbeat since 30 seconds");
			}
		});
	}

}