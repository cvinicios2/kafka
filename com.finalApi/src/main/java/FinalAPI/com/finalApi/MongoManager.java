package FinalAPI.com.finalApi;

import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

public class MongoManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoManager.class);
	
	private MongoClient mongoClient;

	public MongoManager(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
	
	public void registerConsumer(Vertx vertx) {
		
		vertx.eventBus().consumer("com.teste.mongoservice", message ->{
		
		System.out.println("Service Message: " + message.body());

		JsonObject inputJson = new JsonObject(message.body().toString());
		
		if (inputJson.getString("cmd").equals("findAll")) {	
			getAllProducts(message);
		}
		
		if (inputJson.getString("cmd").equals("findById")) {	
			getProductById(message, inputJson.getString("productId"));
		}
		
		
//		message.reply(new JsonObject().put("responseCode", "OK").put("message", "Response message"));
		
	});
		
	}
	
	
	
	public void getAllProducts(Message<Object> message) {
		
		FindOptions findOptions = new FindOptions();
//		findOptions.setLimit(1);

		mongoClient.findWithOptions("products", new JsonObject(), findOptions, results -> {

			try {
				List<JsonObject> objects = results.result();

				if (objects != null && objects.size() != 0) {
					System.out.println("got some data len=" + objects.size());

					JsonObject jsonResponse = new JsonObject();

					jsonResponse.put("products", objects);
					message.reply(jsonResponse.toString());

				}else {
					JsonObject jsonResponse = new JsonObject();
					jsonResponse.put("error", "No items found");
					message.reply(jsonResponse.toString());
				}

			} catch (Exception e) {
				LOGGER.info("getAllProducts failed  exception= " + e.getLocalizedMessage());

				JsonObject jsonResponse = new JsonObject();

				jsonResponse.put("error", "Exception and no Items Found");
				
				message.reply(jsonResponse.toString());

		}

		});

	}

	public void getProductById(Message<Object> message, String productId) {
		
		FindOptions findOptions = new FindOptions();
//		findOptions.setLimit(1);

		mongoClient.findWithOptions("products", new JsonObject().put("_id", productId), findOptions, results -> {

			try {
				List<JsonObject> objects = results.result();

				if (objects != null && objects.size() != 0) {
					System.out.println("got some data len=" + objects.size());

					JsonObject jsonResponse = objects.get(0);

					message.reply(jsonResponse.toString());

				}else {
					JsonObject jsonResponse = new JsonObject();
					jsonResponse.put("error", "No items found");
					message.reply(jsonResponse.toString());
				}

			} catch (Exception e) {
				LOGGER.info("getAllProducts failed  exception= " + e.getLocalizedMessage());

				JsonObject jsonResponse = new JsonObject();

				jsonResponse.put("error", "Exception and no Items Found");
				
				message.reply(jsonResponse.toString());

		}

		});

	}

}
