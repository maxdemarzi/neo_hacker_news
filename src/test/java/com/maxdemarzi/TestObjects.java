package com.maxdemarzi;

import java.util.HashMap;

public class TestObjects {

    public static final HashMap<String, Object> validStoryInput = new HashMap<String, Object>(){{
        put("id", "1");
        put("url", "http://blog.programmableweb.com/2013/07/25/alchemyapi-updates-api-brings-deep-learning-to-the-masses/");
    }};

    public static final HashMap<String, Object> validUserInput = new HashMap<String, Object>(){{
        put("username", "maxdemarzi");
    }};

    public static final HashMap<String, String> pageProperties = new HashMap<String, String>() {{
        put("dbpedia", "http://dbpedia.org/resource/Apple_Inc.");
    }};

}
