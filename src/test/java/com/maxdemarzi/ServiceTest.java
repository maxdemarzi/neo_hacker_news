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
    }

    @After
    public void tearDown() throws Exception {
        db.shutdown();

    }

    @Test
    public void shouldRespondToHelloWorld() {
        assertEquals("Hello World!", service.helloWorld());
    }

    @Test
    public void shouldCreateStory() throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(TestObjects.validStoryInput);
        Response response = service.createStory(body, db);
        int code = response.getStatus();
        assertEquals(Response.Status.CREATED.getStatusCode(), code);
        Thread.sleep(4000);
        int count = 0;
        try (Transaction tx = db.beginTx()) {
            Node story = db.findNode(Labels.Story, "id", TestObjects.validStoryInput.get("id"));

            for (Relationship r : story.getRelationships(RelationshipTypes.HAS_TOPIC, Direction.OUTGOING)){
                count++;
            }
        }
        assertEquals(11, count);
    }

}
