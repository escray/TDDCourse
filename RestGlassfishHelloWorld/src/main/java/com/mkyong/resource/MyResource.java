package com.mkyong.resource;

import com.mkyong.User;
import com.mkyong.service.MessageService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path("/hello")
public class MyResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Jersey Jetty example";
    }

//    @Path("/{username}")
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public User hello(@PathParam("username") String name) {
//        return new User(0, name);
//    }

    @Path("/{name}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@PathParam("name") String name) {
        return "Jersey: hello " + name;
    }

    @Path("/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> helloList() {
        List<User> list = new ArrayList<>();
        list.add(new User(1, "mkyong"));
        list.add(new User(2, "zilap"));
        return list;
    }

    // DI via HK2
//    @Inject
//    private MessageService messageService;
//    @Path("/hk2")
//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public String helloHK2() {
//        return messageService.getHello();
//    }
}
