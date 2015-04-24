package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ServiceTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static GraphDatabaseService db;
    private static Service service;

    @Before
    public void setUp() {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        service = new Service(db);
        populate(db);
    }

    @After
    public void tearDown() throws Exception {
        db.shutdown();
    }

    public void populate(GraphDatabaseService db) {
        try (Transaction tx = db.beginTx()) {
            Node page = db.createNode(Labels.Page);
            page.setProperty("dbpedia", TestObjects.pageProperties.get("dbpedia"));
            tx.success();
        }
    }

    @Test
    public void shouldRespondToHelloWorld() {
        assertEquals("Hello World!", service.helloWorld());
    }

    @Test
    public void shouldMigrate() {
        assertEquals("Migrated!", service.migrate(db));
        assertEquals("Already Migrated!", service.migrate(db));
    }

    @Test
    public void shouldCreateStory() throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(TestObjects.validStoryInput);
        Response response = service.createStory(body, db);
        int code = response.getStatus();
        assertEquals(Response.Status.CREATED.getStatusCode(), code);
        Thread.sleep(4000);
        int topicCount = 0;
        int linkedDataCount = 0;
        try (Transaction tx = db.beginTx()) {
            Node story = db.findNode(Labels.Story, "id", TestObjects.validStoryInput.get("id"));

            for (Relationship r : story.getRelationships(RelationshipTypes.HAS_TOPIC, Direction.OUTGOING)){
                topicCount++;
            }

            Node page = db.findNode(Labels.Page, "dbpedia", TestObjects.pageProperties.get("dbpedia"));
            for (Relationship r : page.getRelationships(RelationshipTypes.IS_LINKED_DATA, Direction.INCOMING)){
                linkedDataCount++;
            }

        }
        assertEquals(26, topicCount);
        assertEquals(1, linkedDataCount);
    }

    @Test
    public void shouldCreateUser() throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(TestObjects.validUserInput);
        Response response = service.createUser(body, db);
        int code = response.getStatus();
        assertEquals(Response.Status.CREATED.getStatusCode(), code);
    }

}
