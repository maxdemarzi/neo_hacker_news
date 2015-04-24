package com.maxdemarzi;

import com.maxdemarzi.services.Alchemy;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.Schema;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Path("/service")
public class Service {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final PathFinder<org.neo4j.graphdb.Path> SHORTEST_PATH_AUTHORED_LEVEL_ONE =
            GraphAlgoFactory.shortestPath(
                    PathExpanders.forTypeAndDirection(RelationshipTypes.AUTHORED, Direction.INCOMING),
                    1);

    private static final PathFinder<org.neo4j.graphdb.Path> SHORTEST_PATH_COMMENTED_LEVEL_ONE =
            GraphAlgoFactory.shortestPath(
                    PathExpanders.forTypeAndDirection(RelationshipTypes.COMMENTED, Direction.INCOMING),
                    1);

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
                schema.constraintFor(Labels.Page)
                        .assertPropertyIsUnique("dbpedia")
                        .create();
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

    @POST
    @Path("/user")
    public Response createUser(String body, @Context GraphDatabaseService db) throws IOException {
        HashMap input = Validators.getValidUserInput(body);

        boolean exists = false;
        try ( Transaction tx = db.beginTx() ) {
            Node user = db.findNode(Labels.User, "username", input.get("username"));

            if (user == null) {
                user = db.createNode(Labels.User);
                user.setProperty("username", input.get("username"));
            } else {
                exists = true;
            }

            tx.success();
        }

        if (exists) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.CREATED).build();
        }
    }

    @POST
    @Path("/user/{username}/authored/{storyId}")
    public Response createAuthored(@PathParam("username") String username,
                                   @PathParam("storyId") String storyId, @Context GraphDatabaseService db) {
        boolean exists = false;
        try ( Transaction tx = db.beginTx() ) {
            Node user = db.findNode(Labels.User, "username", username);
            Node story = db.findNode(Labels.Story, "id", storyId);

            if (user == null ) {
                throw Exception.userNotFound;
            }
            if (story == null) {
                throw Exception.storyNotFound;
            }
            tx.acquireWriteLock(user);
            tx.acquireWriteLock(story);

            if (SHORTEST_PATH_AUTHORED_LEVEL_ONE.findSinglePath(story, user) == null) {
                user.createRelationshipTo(story, RelationshipTypes.AUTHORED);
            } else {
                exists = true;
            }

            tx.success();
        }

        if (exists) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.CREATED).build();
        }
    }

    @POST
    @Path("/user/{username}/commented/{storyId}")
    public Response createCommented(@PathParam("username") String username,
                                   @PathParam("storyId") String storyId, @Context GraphDatabaseService db) {
        boolean exists = false;
        try ( Transaction tx = db.beginTx() ) {
            Node user = db.findNode(Labels.User, "username", username);
            Node story = db.findNode(Labels.Story, "id", storyId);

            if (user == null ) {
                throw Exception.userNotFound;
            }
            if (story == null) {
                throw Exception.storyNotFound;
            }
            tx.acquireWriteLock(user);
            tx.acquireWriteLock(story);

            if (SHORTEST_PATH_COMMENTED_LEVEL_ONE.findSinglePath(story, user) == null) {
                user.createRelationshipTo(story, RelationshipTypes.COMMENTED);
            } else {
                exists = true;
            }

            tx.success();
        }

        if (exists) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.CREATED).build();
        }
    }
}
