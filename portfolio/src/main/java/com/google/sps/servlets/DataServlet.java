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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.lang.Long;
import java.util.*;

/** Servlet that interacts with a Google DataStore database for a comments section */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private static final int NO_MAX_COMMENT_LIMIT = -1;
    private static final String DEFAULT_DISPLAY_NAME = "Anon. User";
    private static final String RESPONSE_JSON_CONTENT = "application/json;";
    private static final String REQUEST_COMMENT_LIMIT_PARAM = "limit";
    private static final String REQUEST_MESSAGE_PARAM = "message";
    private static final String DATASTORE_COMMENT_KIND = "Comment";
    private static final String DATASTORE_COMMENT_MESSAGE_PARAM = "message";
    private static final String DATASTORE_COMMENT_TIMESTAMP_PARAM = "timestamp";
    private static final String DATASTORE_COMMENT_USER_ID_PARAM = "userId";
    private static final String DATASTORE_USER_DATA_KIND = "UserData";
    private static final String DATASTORE_USER_DATA_ID_PARAM = "id";
    private static final String DATASTORE_USER_DATA_NAME_PARAM = "displayName";
    private static final String REDIRECT_URL_PATH = "/";
    private static final String REDIRECT_URL_FRAGMENT = "#comments";



    /**
     * Gets database data for comments
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Gets possible limit to the maximum number of comments  
        String commentLimitString = request.getParameter(REQUEST_COMMENT_LIMIT_PARAM);
        int commentLimit = tryParseInt(commentLimitString);
        if (commentLimit <= 0) {
            commentLimit = NO_MAX_COMMENT_LIMIT;
        }

        // Gets list of most recent comments, based on the limit
        Query commentQuery = new Query(DATASTORE_COMMENT_KIND).addSort(DATASTORE_COMMENT_TIMESTAMP_PARAM, SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery commentResults = datastore.prepare(commentQuery);
        Iterable<Entity> datastoreResults = null;
        if (commentLimit == NO_MAX_COMMENT_LIMIT) {
            datastoreResults = commentResults.asIterable();
        } else {
            datastoreResults = commentResults.asIterable(FetchOptions.Builder.withLimit(commentLimit));
        }

        // Gets a mapping of user ids to display names from the database
        Map<String, String> userDisplayNames = new HashMap<>();
        Query userQuery = new Query(DATASTORE_USER_DATA_KIND);
        PreparedQuery userResults = datastore.prepare(userQuery);
        for (Entity entity : userResults.asIterable()) {
            String userId = (String) entity.getProperty(DATASTORE_USER_DATA_ID_PARAM);
            String displayName = (String) entity.getProperty(DATASTORE_USER_DATA_NAME_PARAM);
            userDisplayNames.put(userId, displayName);
        }

        // Converts Entity list to Comment list 
        List<Comment> comments = new ArrayList<>();
        for (Entity entity : datastoreResults) {
            long id = entity.getKey().getId();
            String userId = (String) entity.getProperty(DATASTORE_COMMENT_USER_ID_PARAM);
            String username = userDisplayNames.getOrDefault(userId, DEFAULT_DISPLAY_NAME);
            String message = (String) entity.getProperty(DATASTORE_COMMENT_MESSAGE_PARAM);
            long timestamp = (long) entity.getProperty(DATASTORE_COMMENT_TIMESTAMP_PARAM);

            Comment comment = new Comment(id, username, message, timestamp);
            comments.add(comment);
        }
        
        // Converts object to JSON and returns to front-end
        Gson gson = new Gson();
        response.setContentType(RESPONSE_JSON_CONTENT);
        response.getWriter().println(gson.toJson(comments));
    }

    /**
     * Adds comment data to database
     * @param request The request object
     * @param response The response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Breaks from method if the user is not logged in
        UserService userService = UserServiceFactory.getUserService();
        if (!userService.isUserLoggedIn()) {
            response.sendRedirect(REDIRECT_URL_PATH);
            return;
        }

        // Gets necessary data from system and request
        String userId = userService.getCurrentUser().getUserId();
        String userEmail = userService.getCurrentUser().getEmail();
        String message = request.getParameter(REQUEST_MESSAGE_PARAM);
        long timestamp = System.currentTimeMillis();

        // Builds redirect URL
        String redirectURL = REDIRECT_URL_PATH + REDIRECT_URL_FRAGMENT;

        // Creates database entry Entity and populates its parameters
        Entity commentEntity = new Entity(DATASTORE_COMMENT_KIND);
        commentEntity.setProperty(DATASTORE_COMMENT_USER_ID_PARAM, userId);
        commentEntity.setProperty(DATASTORE_COMMENT_MESSAGE_PARAM, message);
        commentEntity.setProperty(DATASTORE_COMMENT_TIMESTAMP_PARAM, timestamp);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        response.sendRedirect(redirectURL);
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