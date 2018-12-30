package FinalAPI.com.finalApi;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class MongoDB extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(MongoDB.class);

	private static MongoClient mongoClient = null;

	public static void main(String[] args) {

		VertxOptions vertxOptions = new VertxOptions();
		vertxOptions.setClustered(true);

		Vertx.clusteredVertx(vertxOptions, res -> {

			if (res.succeeded()) {
				Vertx vertx = res.result();

				ConfigRetriever configRetriever = ConfigRetriever.create(vertx);

				configRetriever.getConfig(config -> {

					if (config.succeeded()) {
						JsonObject configJson = config.result();

						DeploymentOptions options = new DeploymentOptions().setConfig(configJson);

						vertx.deployVerticle(new MongoDB(), options);
					}
				});
			}
		});

	}

	@Override
	public void start() {
		LOGGER.info("Verticle MongoDB started");

		JsonObject dbconfig = new JsonObject();

		dbconfig.put("connection_string", "mongodb://" + config().getString("mongodb.host") + ":"
				+ config().getInteger("mongodb.port") + "/" 
				+ config().getString("mongodb.databasename"));
		dbconfig.put("useObjectId", true);

		System.out.println(dbconfig);
		mongoClient = MongoClient.createShared(vertx, dbconfig);
		
		MongoManager mongoManager = new MongoManager(mongoClient);
		mongoManager.registerConsumer(vertx);

	}

	@Override
	public void stop() {
		LOGGER.info("Verticle MongoDB stopped");
	}
}
