package FinalAPI.com.finalApi.resources;

import java.util.ArrayList;
import java.util.List;

import FinalAPI.com.finalApi.entity.Product;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ProductsResources {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductsResources.class);
	private Vertx vertx;
	
	public Router getApiSubRouter(Vertx vertx) {
		
		this.vertx = vertx;
		
		Router subRouter = Router.router(vertx);
		
		subRouter.route("/*").handler(this::defaultProcessorForAllAPI);
		subRouter.route("/1/products*").handler(BodyHandler.create());
		subRouter.get("/v1/products").handler(this::getAllProducts);
		subRouter.get("/v1/products/:id").handler(this::getProductById);
		subRouter.post("/v1/products").handler(this::addProduct);
		subRouter.put("/v1/products/:id").handler(this::updateProductById);
		subRouter.delete("/v1/products/:id").handler(this::deleteProductById);

		return subRouter;
	}

	public void defaultProcessorForAllAPI(RoutingContext context) {
		
		String authToken = context.request().getParam("authToken");
		System.out.println("Token: " + authToken);
		
		if (authToken == null || !authToken.equals("123")) {
			context.response().setStatusCode(401).putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
			.end(Json.encodePrettily(new JsonObject().put("error", "Not Authorized to use this APIs")));
			LOGGER.info("Falled basic auth check");
		}else {
			context.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			context.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE");
			
			//call next matching api
			System.out.println("next");
			context.next();

		}
		
		
	}
	
	public void getAllProducts(RoutingContext context) {

		JsonObject cmdJson = new JsonObject();
		
		cmdJson.put("cmd", "findAll");
		
		vertx.eventBus().send("com.teste.mongoservice", cmdJson.toString(), reply ->{
		
			if (reply.succeeded()) {
				JsonObject replyResults = new JsonObject(reply.result().body().toString());
				
				System.out.println("Got Reply message = " + replyResults.toString());
				
				context.response()
					.setStatusCode(200)
					.putHeader("content-type", "application/json")
					.end(Json.encodePrettily(replyResults));
				
			}		
		});
	}

	public void getProductById(RoutingContext context) {

		final String productId = context.request().getParam("id");
		
		JsonObject cmdJson = new JsonObject();
		
		cmdJson.put("cmd", "findById");
		cmdJson.put("productId", productId);
		
		vertx.eventBus().send("com.teste.mongoservice", cmdJson.toString(), reply ->{
			
			if (reply.succeeded()) {
				JsonObject replyResults = new JsonObject(reply.result().body().toString());
				
				System.out.println("Got Reply message = " + replyResults.toString());
				
				context.response()
					.setStatusCode(200)
					.putHeader("content-type", "application/json")
					.end(Json.encodePrettily(replyResults));
				
			}		
		});
	}

	public void addProduct(RoutingContext context) {

		JsonObject jsonBody = context.getBodyAsJson();

		String number = jsonBody.getString("number");
		String description = jsonBody.getString("description");

		Product newItem = new Product("", number, description);
		newItem.setId("11");

		context.response().setStatusCode(201).putHeader("content-type", "application/json")
				.end(Json.encodePrettily(newItem));
	}

	public void updateProductById(RoutingContext context) {

		final String productId = context.request().getParam("id");

		JsonObject jsonBody = context.getBodyAsJson();

		String number = jsonBody.getString("number");
		String description = jsonBody.getString("description");

		Product updatedItem = new Product(productId, number, description);

		context.response().setStatusCode(200).putHeader("content-type", "application/json")
				.end(Json.encodePrettily(updatedItem));
	}

	public void deleteProductById(RoutingContext context) {

		final String productId = context.request().getParam("id");

		context.response().setStatusCode(200).putHeader("content-type", "application/json").end();
	}

}
