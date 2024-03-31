package com.abcbank.userinactivity.bankpocinactivitykstream.serdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.Serdes.WrapperSerde;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.abcbank.userinactivity.bankpocproducer.model.Customer;

public final class CustomSerdes {
	private CustomSerdes() {
	}

	public static Serde<Customer> Customer() {
		JsonSerializer<Customer> serializer = new JsonSerializer<>();
		JsonDeserializer<Customer> deserializer = new JsonDeserializer<>(Customer.class);
		return Serdes.serdeFrom(serializer, deserializer);
	}


    static public final class CustomerSerde extends WrapperSerde<Customer> {
        public CustomerSerde() {
			 super(new JsonSerializer<>(), new JsonDeserializer<>(Customer.class));
		}
    }
    
}