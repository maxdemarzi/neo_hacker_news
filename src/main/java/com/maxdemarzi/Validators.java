package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

public class Validators {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static HashMap getValidStoryInput(String body) throws IOException {
        HashMap input;

        // Parse the input
        try {
            input = objectMapper.readValue( body, HashMap.class);
        } catch (Exception e) {
            throw Exception.invalidInput;
        }
        // Make sure it has an id parameter
        if(!input.containsKey("id")){
            throw Exception.missingIdParameter;
        }

        if (input.get("id") == "") {
            throw Exception.invalidIdParameter;
        }

        // Make sure it has a url parameter
        if(!input.containsKey("url")){
            throw Exception.missingURLParameter;
        }

        return input;
    }

    public static HashMap getValidUserInput(String body) throws IOException {
        HashMap input;

        // Parse the input
        try {
            input = objectMapper.readValue( body, HashMap.class);
        } catch (Exception e) {
            throw Exception.invalidInput;
        }
        // Make sure it has an id parameter
        if(!input.containsKey("username")){
            throw Exception.missingIdParameter;
        }
        // Make sure the username is not blank
        if(input.get("username") == "") {
            throw Exception.invalidUsernameParameter;
        }

        return input;
    }
}
