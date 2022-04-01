package geektime.tdd.resources;

import geektime.tdd.model.Student;
import geektime.tdd.model.StudentRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.IntStream;

@Path("/students")
public class StudentsResource {
    private StudentRepository repository;

    @Inject
    public StudentsResource(StudentRepository repository) {
        this.repository = repository;
    }

//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public String hello() {
//        return "Jersey Jetty example.";
//    }


    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") long id) {

        return repository.findByIdFromDB(id).map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Student> all() {
        return repository.all();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response save(MultivaluedMap<String, String> form) {
        List<String> firstNames = form.get("students[first_name]");
        List<String> lastNames = form.get("students[last_name]");
        List<String> emails = form.get("students[email]");

        IntStream.range(0, firstNames.size())
                .mapToObj(it -> new Student(firstNames.get(it)
                        , lastNames.get(it)
                        , emails.get(it)))
                .forEach(repository::saveToDB);

        return Response.created(null).build();
    }
}
