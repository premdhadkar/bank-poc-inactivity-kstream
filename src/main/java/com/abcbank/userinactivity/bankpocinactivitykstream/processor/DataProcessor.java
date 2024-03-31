package com.abcbank.userinactivity.bankpocinactivitykstream.processor;

import java.time.Duration;
import java.util.function.Function;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.SessionWindows;
import org.apache.kafka.streams.kstream.SlidingWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.abcbank.userinactivity.bankpocproducer.model.Customer;

@Configuration
public class DataProcessor {
	@SuppressWarnings("deprecation")
	@Bean
	Function<KStream<Integer, Customer>, KStream<Integer,Customer>> transactionMonitoring() {
		return input -> {

			Aggregator<Integer, Customer, Customer> amountAgg = (key, value, currentTotal) -> {
				currentTotal.setAccno(key);
				currentTotal.setDescription("Sum of Transactions");
				currentTotal.setTransaction(currentTotal.getTransaction() + value.getTransaction());
				return currentTotal;
			};

			return input.peek((k,v)-> System.out.println("received transaction Accno: " + k + " Amt: " + v.getTransaction()))
					.groupByKey()
					.windowedBy(SessionWindows.with(Duration.ofSeconds(30)))
					.aggregate(() -> Customer.builder().transaction(0F).build(), amountAgg, null)
					.toStream()
					.map((wk, val) -> KeyValue.pair(wk.key(), val))
					.filter((key, value) -> value.getTransaction() >= 500);
					
					
//					.aggregate(() -> Customer.builder().transaction(0F).build(), amountAgg)
//					.toStream()
//					.map((wk, val) -> KeyValue.pair(wk.key(), val))
//					.filter((key, value) -> value.getTransaction() >= 500);
			
//			return input.peek((k,v)-> System.out.println("received transaction Accno: " + k + " Amt: " + v.getTransaction()))
//					.groupByKey()
//					.windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(30)))
//					.aggregate(() -> Customer.builder().transaction(0F).build(), amountAgg)
//					.toStream()
//					.map((wk, val) -> KeyValue.pair(wk.key(), val))
//					.filter((key, value) -> value.getTransaction() >= 500);
		};
	}
}
