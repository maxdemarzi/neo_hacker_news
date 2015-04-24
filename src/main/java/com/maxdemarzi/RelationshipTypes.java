package com.maxdemarzi;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    HAS_TOPIC,
    AUTHORED,
    COMMENTED,
    IS_LINKED_DATA,
    HAS_CATEGORY,
    HAS_LINK,
    HAS_ONTOLOGY
}
