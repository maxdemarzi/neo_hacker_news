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

5. Configure Neo4j by adding a line to conf/neo4j-server.properties:

        org.neo4j.server.thirdparty_jaxrs_classes=com.maxdemarzi=/v1

6. Start Neo4j server.

7. Check that it is installed correctly over HTTP:

        :GET /v1/service/helloworld
        
8. Initialize the Database (Database migration):

This will create the schema of the database.

        :GET /v1/service/migrate

9. Create a View :POST /v1/service/story 

        {"id": "1", "url" : "http://www.politico.com/blogs/media/2012/02/detroit-news-ed-upset-over-romney-edit-115247.html"}        
        