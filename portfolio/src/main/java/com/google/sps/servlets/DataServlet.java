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
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.util.*;

/** Servlet that interacts with a Google DataStore database for a comments section */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private static final int NO_MAX_COMMENT_LIMIT = -1;

    /**
     * Gets database data for comments
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Gets possible limit to the maximum number of comments  
        String commentLimitString = request.getParameter("limit");
        int commentLimit = tryParseInt(commentLimitString);
        if (commentLimit <= 0) {
            commentLimit = NO_MAX_COMMENT_LIMIT;
        }

        // Gets list of most recent comments, based on the limit
        Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);
        Iterable<Entity> datastoreResults = null;
        if (commentLimit == NO_MAX_COMMENT_LIMIT) {
            datastoreResults = results.asIterable();
        } else {
            datastoreResults = results.asIterable(FetchOptions.Builder.withLimit(commentLimit));
        }

        // Converts Entity list to Comment list 
        List<Comment> comments = new ArrayList<>();
        for (Entity entity : datastoreResults) {
            long id = entity.getKey().getId();
            String message = (String) entity.getProperty("message");
            long timestamp = (long) entity.getProperty("timestamp");
            Comment comment = new Comment(id, message, timestamp);
            comments.add(comment);
        }
        
        // Converts object to JSON and returns to front-end
        Gson gson = new Gson();
        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(comments));
    }

    /**
     * Adds comment data to database
     * @param request The request object
     * @param response The response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = request.getParameter("message");
        long timestamp = System.currentTimeMillis();

        // Creates database entry Entity and populates its parameters
        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("message", message);
        commentEntity.setProperty("timestamp", timestamp);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        response.sendRedirect("/#contact");
    }

    /**
     * Abstracts out exceptions when parsing strings to ints
     * @param str The string to try to parse to an int
     * @return The int value of the string, or -1 if an exception is thrown
     */
    public int tryParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return NO_MAX_COMMENT_LIMIT;
        }
    }
}