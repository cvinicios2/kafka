package formalApi.formalApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import formalApi.entity.Product;
import formalApi.resources.ProductResources;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class APIVertx extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(APIVertx.class);

	public static void main(String[] args) {

		Vertx vertx = Vertx.vertx();

		ConfigRetriever configRetriever = ConfigRetriever.create(vertx);

		configRetriever.getConfig(config -> {

			if (config.succeeded()) {
				JsonObject configJson = config.result();

//				System.out.println(configJson.encodePrettily());

				DeploymentOptions options = new DeploymentOptions().setConfig(configJson);

				vertx.deployVerticle(new APIVertx(), options);

			}
		});

	}

	@Override
	public void start() {
		LOGGER.info("Verticle App started");

		Router router = Router.router(vertx);
		
		Router subRouter = Router.router(vertx);
		
		ProductResources resources = new ProductResources();
		
		router.mountSubRouter("/api/", resources.getApiSubRouter(vertx));
		
//		router.route("/api*").handler(this::defaultProcessorForAllAPI);
//		router.route("/api/v1/products*").handler(BodyHandler.create());
//		router.get("/api/v1/products").handler(this::getAllProducts);
//		router.get("/api/v1/products/:id").handler(this::getProductById);
//		router.post("/api/v1/products").handler(this::addProduct);
//		router.put("/api/v1/products/:id").handler(this::updateProductById);
//		router.delete("/api/vq/products/:id").handler(this::deleteProductById);

		subRouter.route("/*").handler(resources::defaultProcessorForAllAPI);
		subRouter.route("/1/products*").handler(BodyHandler.create());
		subRouter.get("/v1/products").handler(resources::getAllProducts);
		subRouter.get("/v1/products/:id").handler(resources::getProductById);
		subRouter.post("/v1/products").handler(resources::addProduct);
		subRouter.put("/v1/products/:id").handler(resources::updateProductById);
		subRouter.delete("/v1/products/:id").handler(resources::deleteProductById);
		
		router.mountSubRouter("/api/", subRouter);

		router.get("/yo.html").handler(routingContext -> {
			
			Cookie cookie = routingContext.getCookie("name");

			ClassLoader classLoader = getClass().getClassLoader();

			File file = new File(classLoader.getResource("webroot/yo.html").getFile());

			String mappedHtml = "";

			try {
				StringBuilder result = new StringBuilder("");

				Scanner scanner = new Scanner(file);

				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					result.append(line).append("\n");
				}
				scanner.close();

				mappedHtml = result.toString();
				
				String name = "Unknown 2";
				
				if (cookie 	!= null ) {
					name = cookie.getName();
				}else {
					cookie = Cookie.cookie("name", "Vinicios");
					cookie.setPath("/");
					cookie.setMaxAge(365 * 24 * 60 * 60);
					System.out.println("nome: " + cookie.getName());
					routingContext.addCookie(cookie);
				}
				

				mappedHtml = replaceAllTokens(mappedHtml, "{name}", name);

			} catch (IOException e) {
				e.printStackTrace();
			}

			routingContext.response().putHeader("content-type", "text/html").end(mappedHtml);
		});

		router.route().handler(StaticHandler.create().setCachingEnabled(false));

		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port"),
				AsyncResult -> {
					
					if (AsyncResult.succeeded()) {
						LOGGER.info("Http server running on port: " + config().getInteger("http.port"));
					}else {
						LOGGER.error("Could not start a Http server" + AsyncResult.cause());
					}
				});

	}

	public String replaceAllTokens(String input, String token, String newValue) {

		String output = input;

		while (output.indexOf(token) != -1) {
			output = output.replace(token, newValue);
		}

		return output;
	}
	
	

	@Override
	public void stop() {
		LOGGER.info("Verticle App stopped");
	}
}
