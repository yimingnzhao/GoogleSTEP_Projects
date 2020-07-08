// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.lang.StringBuffer;
import java.io.BufferedReader;
import java.util.*;

/** Deletes all comments stored in the database */
@WebServlet("/delete-data")
public class DeleteCommentsServlet extends HttpServlet {

    private static final long PARSE_LONG_EXCEPTION = -1;
    private static final String REQUEST_STRING_DELIMITER = ",";
    private static final String READ_REQUEST_EXCEPTION_MSG = "Exception: Failed to read request data";
    private static final String PARSE_LONG_EXCEPTION_MSG = "Exception: Unable to parse comment id as long";
    private static final String FIND_DATASTORE_ENTITY_EXCEPTION_MSG = "Exception: Entity of given comment id cannot be found";
    private static final String DATASTORE_COMMENT_KIND = "Comment";
    private static final String REDIRECT_URL = "/";

    /**
     * Adds comment data to database
     * @param request The request object
     * @param response The response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Initializes a buffer to build the request string
        StringBuffer buffer = new StringBuffer();
        String line = null;

        // Builds the string from the servlet request
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line); 
            }
        } catch (Exception e) {
            System.out.println(READ_REQUEST_EXCEPTION_MSG);
            return;
        }

        // Buffer string should be a list of ids seperated by the comma character
        String[] commentIds = buffer.toString().split(REQUEST_STRING_DELIMITER);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        for (String id : commentIds) {
            long numId = tryParseLong(id);
            if (numId == PARSE_LONG_EXCEPTION) {
                System.out.println(PARSE_LONG_EXCEPTION_MSG);
                continue;
            }
            // Creates a datastore Key object for the comment id to delete the key
            Key key = KeyFactory.createKey(DATASTORE_COMMENT_KIND, numId);
            try {
                Entity exist = datastore.get(key);
            } catch (Exception e) {
                System.out.println(FIND_DATASTORE_ENTITY_EXCEPTION_MSG);
                continue;
            }
            datastore.delete(key);
        }
        response.sendRedirect(REDIRECT_URL); 
    }

    /**
     * Abstracts out exceptions when parsing strings to longs
     * @param str The string to try to parse to a long
     * @return The long value of the string, or -1 if an exception is thrown
     */
    public long tryParseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return PARSE_LONG_EXCEPTION;
        }
    }
}