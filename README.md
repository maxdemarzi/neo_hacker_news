Neo Hacker News
===============

Let's build a recommendation engine on top of Hacker News


# Pre-Requisites

You will need to [Register for an Alchemy API Key](http://www.alchemyapi.com/api/register.html)

# Instructions

1. Set your environment variable ALCHEMY_API_KEY equal to your key.

2. Build it:

        mvn clean package

3. Copy target/HackerNews-1.0.jar to the plugins/ directory of your Neo4j server.

4. Download and copy additional jars to the plugins/ directory of your Neo4j server.

        wget http://repo1.maven.org/maven2/joda-time/joda-time/2.7/joda-time-2.7.jar
        wget http://repo1.maven.org/maven2/com/google/guava/guava/18.0/guava-18.0.jar
        wget http://repo1.maven.org/maven2/com/likethecolor/alchemy/1.1.6/alchemy-1.1.6.jar
        wget http://repo1.maven.org/maven2/commons-validator/commons-validator/1.4.1/commons-validator-1.4.1.jar
        
5. Configure Neo4j by adding a line to conf/neo4j-server.properties:

        org.neo4j.server.thirdparty_jaxrs_classes=com.maxdemarzi=/v1

6. Generate or Download (3.2 GB) the DBPedia graph.db to your neo4j/data folder. See [dbpedia-importer](https://github.com/kbastani/neo4j-dbpedia-importer)

        wget https://s3-us-west-1.amazonaws.com/neo4j-sample-datasets/dbpedia/dbpedia-store.tar.bz2
        
7. Enable allow_store_upgrade in neo4j/config/neo4j.properties
        
        allow_store_upgrade=true
        
8. Start Neo4j server.

9. Check that it is installed correctly over HTTP:

        :GET /v1/service/helloworld
        
10. Initialize the Database (Database migration):

This will create the schema of the database.

        :GET /v1/service/migrate

11. Create a View :POST /v1/service/story 

        {"id": "1", "url" : "http://blog.programmableweb.com/2013/07/25/alchemyapi-updates-api-brings-deep-learning-to-the-masses/"}        
        