package com.abcbank.userinactivity.bankpocinactivitykstream;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

import com.abcbank.userinactivity.bankpocinactivitykstream.model.Customer;
import com.abcbank.userinactivity.bankpocinactivitykstream.model.CustomerTimeStampMap;
import com.abcbank.userinactivity.bankpocinactivitykstream.repository.CustomerNMinusTwoTimeStampMapRepository;
import com.abcbank.userinactivity.bankpocinactivitykstream.repository.CustomerRepository;

@EnableScheduling
@SpringBootApplication
@Transactional
public class BankPocInactivityKstreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankPocInactivityKstreamApplication.class, args);
	}

	private static Consumer<KStream<Integer, com.abcbank.userinactivity.bankpocproducer.model.Customer>> func;
	private static AtomicBoolean isTablesRefreshing = new AtomicBoolean(false);

	@Value("${user.tables.refresh.interval}")
	private long tables_refresh_interval;
	

	
    @Value("${user.heartbeat.monitoring.interval}")
	private long heartbeat_monitoring_interval;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	CustomerNMinusTwoTimeStampMapRepository tsMapRepository;

	@Bean
	Consumer<KStream<Integer, com.abcbank.userinactivity.bankpocproducer.model.Customer>> heartbeatBinder() {
		tsMapRepository.deleteAll();
		new Thread(() -> refreshMapsStreamsAndTables()).start();
		func = (heartBeatStream) -> {
			heartBeatStream.foreach((key, value)->{
				CustomerTimeStampMap tsMapUser = tsMapRepository.findById(key).orElseThrow();
				tsMapUser.setLastTimestamp(value.getTimestamp());
				tsMapRepository.save(tsMapUser);
			});
		};
		new Thread(() -> monitoring()).start();
		return func;
	}

	
	public void refreshMapsStreamsAndTables() {
		List<Customer> allCustomers;
		while (true) {
			isTablesRefreshing.set(true);
			System.out.print("refreshing...");
			allCustomers = customerRepository.findAll();
			allCustomers.forEach(i -> {
				if (tsMapRepository.existsById(i.getAccno())) {
					return;
				} else {
					tsMapRepository.save(CustomerTimeStampMap.builder().accno(i.getAccno()).build());
				}
			});
			System.out.println("Done");
			isTablesRefreshing.set(false);
			try {
				Thread.sleep(tables_refresh_interval == 0 ? 5000 : tables_refresh_interval);
			} catch (Exception e) {
				continue;
			}
		}
	}

	public void monitoring() {
		while (true) {
			while (isTablesRefreshing.get()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			tsMapRepository.findAll().forEach(tsMap -> {
				Long nTS = tsMap.getLastTimestamp() == null ? null : tsMap.getLastTimestamp().getTime();
				Long currentTs = Timestamp.from(Instant.now()).getTime();
				if (nTS == null || currentTs - nTS > 21000) {
					System.out.println("User: " + tsMap.getAccno() + " No heartbeat detected");
				}
			});
			try {
				Thread.sleep(heartbeat_monitoring_interval == 0 ? 5000 : heartbeat_monitoring_interval);
			} catch (Exception e) {
				continue;
			}
		}
	}
}