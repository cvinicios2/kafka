package FinalAPI.com.finalApi;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import FinalAPI.com.finalApi.resources.ProductsResources;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Hello world!
 *
 */
public class FinalAPI extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(FinalAPI.class);

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

						vertx.deployVerticle(new FinalAPI(), options);
					}
				});
			}
		});

	}

	@Override
	public void start() {
		LOGGER.info("Verticle FinalAPI started");

		Router router = Router.router(vertx);

		Router subRouter = Router.router(vertx);

		ProductsResources resources = new ProductsResources();

		router.mountSubRouter("/api/", resources.getApiSubRouter(vertx));

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

				if (cookie != null) {
					name = cookie.getName();
				} else {
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

		vertx.createHttpServer(new HttpServerOptions().setCompressionSupported(true)).requestHandler(router::accept).listen(config().getInteger("http.port"),
				AsyncResult -> {

					if (AsyncResult.succeeded()) {
						LOGGER.info("Http server running on port: " + config().getInteger("http.port"));
					} else {
						LOGGER.error("Could not start a Http server" + AsyncResult.cause());
					}
				});

	}

	@Override
	public void stop() {
		LOGGER.info("Verticle FinalAPI stopped");
	}

	public String replaceAllTokens(String input, String token, String newValue) {

		String output = input;

		while (output.indexOf(token) != -1) {
			output = output.replace(token, newValue);
		}

		return output;
	}
}
