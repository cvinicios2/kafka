package com.teste.kafka.CamelKafka;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class CamelKafka {

	public static void main(String[] args) throws Exception {
		
		CamelContext context = new DefaultCamelContext();
		
		try {
			
			context.addRoutes(new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {

					from("kafka:topico1?brokers=192.168.33.153:9092"
			        		 + "&consumersCount=1"
			                + "&seekTo=beginning"
			                + "&groupId=group1")
			                .routeId("FromKafka").process(proc -> {
			                	System.out.println(proc.getIn().getBody().toString());
			                })
			            .log("${body}");
				}
			});
						
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		context.start();
		Thread.sleep(5 * 60 * 1000);
		context.stop();

	}
}
