package com.maxdemarzi.services;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.likethecolor.alchemy.api.Client;
import com.likethecolor.alchemy.api.call.AbstractCall;
import com.likethecolor.alchemy.api.call.RankedNamedEntitiesCall;
import com.likethecolor.alchemy.api.call.type.CallTypeUrl;
import com.likethecolor.alchemy.api.entity.NamedEntityAlchemyEntity;
import com.likethecolor.alchemy.api.entity.Response;
import com.maxdemarzi.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.lang.Exception;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Alchemy extends AbstractScheduledService {

    String apiKey = System.getenv("ALCHEMY_API_KEY");
    final Client alchemyClient = new Client(apiKey);

    private GraphDatabaseService db;
    public LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public final static Alchemy INSTANCE = new Alchemy();
    private Alchemy() {
        if (!this.isRunning()){
            this.startAsync();
            this.awaitRunning();
        }
    }

    public void setGraphDatabase(GraphDatabaseService db){
        this.db = db;
    }

    @Override
    protected void runOneIteration() throws Exception {
        final String id = queue.take();

        try ( Transaction tx = db.beginTx()) {
            Node story = db.findNode(Labels.Story, "id", id);
            if (story != null) {
                String url = (String)story.getProperty("url");

                AbstractCall<NamedEntityAlchemyEntity> rankedNamedEntitiesCall = new RankedNamedEntitiesCall(new CallTypeUrl(url));
                Response<NamedEntityAlchemyEntity> rankedNamedEntitiesResponse = alchemyClient.call(rankedNamedEntitiesCall);

                NamedEntityAlchemyEntity alchemyEntity;
                Iterator<NamedEntityAlchemyEntity> iter = rankedNamedEntitiesResponse.iterator();

                while(iter.hasNext()) {

                    alchemyEntity = iter.next();
                    Node topic = db.findNode(Labels.Topic, "name", alchemyEntity.getText());

                    if ( topic == null) {
                        topic = db.createNode(Labels.Topic);
                        topic.setProperty("name", alchemyEntity.getText());
                    }

                    Relationship hasTopic = story.createRelationshipTo(topic, RelationshipTypes.HAS_TOPIC);
                    hasTopic.setProperty("relevance", alchemyEntity.getScore());
                }

                tx.success();
            }
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS);
    }
}
