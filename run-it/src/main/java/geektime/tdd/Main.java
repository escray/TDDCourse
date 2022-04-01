package geektime.tdd;

import geektime.tdd.resources.StudentsResource;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {
    public static void main(String[] args) throws Exception {
        ResourceConfig config = new ResourceConfig(StudentsResource.class);
//        config.register(StudentsResource.class);
        JettyHttpContainerFactory.createServer(UriBuilder.fromUri("http://localhost").port(8080).build(), config).start();
//        JettyHttpContainerFactory.createServer(URI.create("http://localhost:8080/"), config, true);
    }
}
