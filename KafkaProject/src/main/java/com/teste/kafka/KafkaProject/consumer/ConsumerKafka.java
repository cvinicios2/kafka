package com.teste.kafka.KafkaProject.consumer;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

public class ConsumerKafka {

	public static void main(String[] args) {

		Properties prop = new Properties();
		prop.put("bootstrap.servers", "192.168.33.153:9092,192.168.33.153:9093,192.168.33.153:9094");
		prop.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		prop.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		prop.put("enable.auto.commit", false);
		prop.put("group.id", "topico");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(prop);

		ArrayList<String> topics = new ArrayList<String>();
		topics.add("topico2");

		consumer.subscribe(topics);

		TopicPartition partition = new TopicPartition("topico2", 0);
		boolean flag = true;
		try {
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(1000);
				System.out.println("size: " + records.count());

				if (flag) {
					consumer.seek(partition, 1);
					flag = false;
				}
				
				
				for (ConsumerRecord<String, String> rec : records) {
					System.out.println(rec.value());
//					consumer.commitSync();
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			consumer.close();
		}

	}

}
