package com.abcbank.userinactivity.bankpocinactivitykstream;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.abcbank.userinactivity.bankpocinactivitykstream.model.CustomerTimeStampMap;
import com.abcbank.userinactivity.bankpocinactivitykstream.repository.CustomerTimeStampMapRepository;
import com.abcbank.userinactivity.bankpocproducer.model.Customer;

@EnableScheduling
@SpringBootApplication
@Transactional

public class BankPocInactivityKstreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankPocInactivityKstreamApplication.class, args);
	}

	private static Consumer<KStream<Integer, Customer>> func;

	private static AtomicBoolean isTablesRefreshing = new AtomicBoolean(false);

	@Value("${user.tables.refresh.interval}")
	private long tables_refresh_interval;

	@Value("${user.heartbeat.monitoring.interval}")
	private long heartbeat_monitoring_interval;

	@Value("${user.heartbeat.monitoring.timeDifference}")
	private long heartbeat_monitoring_time_difference;

	@Value("${customerMicroserviceUrl}")
	private String customerMicroserviceUrl;


	@Autowired
	CustomerTimeStampMapRepository tsMapRepository;
	

	
	Consumer<KStream<Integer, Customer>> heartbeatBinder() {
		tsMapRepository.deleteAll();
		new Thread(() -> refreshMapsStreamsAndTables()).start();
		func = (heartBeatStream) -> {
			heartBeatStream.foreach((key, value) -> {
				CustomerTimeStampMap tsMapUser = tsMapRepository.findById(key).orElseThrow();
				tsMapUser.setLastTimestamp(value.getTimestamp());
				tsMapRepository.save(tsMapUser);
			});
		};
		new Thread(() -> monitoring()).start();
		return func;
	}

	
	@SuppressWarnings("unchecked")
	public void refreshMapsStreamsAndTables() {
		RestTemplate restTemplate = new RestTemplate();
		List<Customer> allCustomers;
		while (true) {
			isTablesRefreshing.set(true);
			System.out.print("refreshing internal tables...");
			
			
			
			String url = customerMicroserviceUrl + "/getAllCustomers";

			ResponseEntity<List<Customer>> response = restTemplate.exchange(
				url, 
				HttpMethod.GET, 
				null,
				new ParameterizedTypeReference<List<Customer>>() {}
				);

			if (response.getStatusCode().is2xxSuccessful()) {
				allCustomers = response.getBody();
			} else {
				throw new RuntimeException("Failed to fetch customers. Status code: " + response.getStatusCodeValue());
			}
			
			
			
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
				if (nTS == null || currentTs - nTS > (heartbeat_monitoring_time_difference == 0 ? 5000 : heartbeat_monitoring_time_difference)) {
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