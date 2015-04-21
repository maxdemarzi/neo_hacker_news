package com.maxdemarzi;

import com.maxdemarzi.services.Alchemy;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Path("/service")
public class Service {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Alchemy alchemy = Alchemy.INSTANCE;

    public Service(@Context GraphDatabaseService db){
        alchemy.setGraphDatabase(db);
    }

    @GET
    @Path("/helloworld")
    public String helloWorld() {
        return "Hello World!";
    }

    @GET
    @Path("/migrate")
    public String migrate(@Context GraphDatabaseService db) {
        boolean migrated;
        try (Transaction tx = db.beginTx()) {
            migrated = db.schema().getConstraints().iterator().hasNext();
        }

        if (migrated){
            return "Already Migrated!";
        } else {
            // Perform Migration
            try (Transaction tx = db.beginTx()) {
                Schema schema = db.schema();
                schema.constraintFor(Labels.Story)
                        .assertPropertyIsUnique("id")
                        .create();
                schema.constraintFor(Labels.Topic)
                        .assertPropertyIsUnique("name")
                        .create();
                tx.success();
            }
            // Wait for indexes to come online
            try (Transaction tx = db.beginTx()) {
                Schema schema = db.schema();
                schema.awaitIndexesOnline(1, TimeUnit.DAYS);
            }
            return "Migrated!";
        }
    }

    @POST
    @Path("/story")
    public Response createStory(String body, @Context GraphDatabaseService db) throws IOException, InterruptedException {
        HashMap input = Validators.getValidStoryInput(body);

        boolean exists = false;
        try ( Transaction tx = db.beginTx() ) {
            Node story = db.findNode(Labels.Story, "id", input.get("id"));

            if (story == null) {
                story = db.createNode(Labels.Story);
                story.setProperty("id", input.get("id"));
                story.setProperty("url", input.get("url"));
            } else {
                exists = true;
            }

            tx.success();
        }

        if (exists) {
            return Response.status(Response.Status.OK).build();
        } else {
            alchemy.queue.put((String)input.get("id"));
            return Response.status(Response.Status.CREATED).build();
        }

    }

}
