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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.gson.Gson;
import com.google.sps.data.UserAuth;
import java.util.*;

/** Servlet that communicates user information */
@WebServlet("/user-data")
public class UserDataServlet extends HttpServlet {

    private static final String RESPONSE_TEXT_CONTENT = "text/plain; charset=UTF-8";
    private static final String EMPTY_RESPONSE = "";
    private static final String DATASTORE_USER_DATA_KIND = "UserData";
    private static final String DATASTORE_USER_DATA_ID_PARAM = "id";
    private static final String DATASTORE_USER_DATA_NAME_PARAM = "displayName";
    private static final String DATASTORE_USER_DATA_EMAIL_PARAM = "email"; 
    private static final String REQUEST_NAME_PARAM = "name";
    private static final String REQUEST_LANGUAGE_CODE_PARAM = "language-code";
    private static final String REDIRECT_URL_PATH = "/";
    private static final String REDIRECT_URL_QUERY = "?";
    private static final String REDIRECT_URL_QUERY_LANGUAGE_PARAM = "hl=";
    private static final String REDIRECT_URL_FRAGMENT = "#comments";
    private static String LANGUAGE_CODES_ARRAY[] = {"en", "zh", "es", "hi", "ar"};

    /**
     * Gets the potential display name of the current user
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(RESPONSE_TEXT_CONTENT);

        // Breaks from method if the user is not logged in
        UserService userService = UserServiceFactory.getUserService();
        if (!userService.isUserLoggedIn()) {
            response.getWriter().println(EMPTY_RESPONSE);
            return;
        }

        // Queries the current user from the current user id
        String userId = userService.getCurrentUser().getUserId();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Filter propertyFilter = new FilterPredicate(DATASTORE_USER_DATA_ID_PARAM, FilterOperator.EQUAL, userId);
        Query query = new Query(DATASTORE_USER_DATA_KIND).setFilter(propertyFilter);
        PreparedQuery results = datastore.prepare(query);
        List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

        // Breaks from method if there is not exactly one query result
        if (listResults.size() != 1) {
            response.getWriter().println(EMPTY_RESPONSE);
            return;
        } 

        String displayName = (String) listResults.get(0).getProperty(DATASTORE_USER_DATA_NAME_PARAM);
        response.getWriter().println(displayName);
    }

    /**
     * Modifies display name of current user and changes the user's database entry
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

        // Gets necessary data from request and UserService
        String id = userService.getCurrentUser().getUserId();
        String email = userService.getCurrentUser().getEmail();
        String displayName = request.getParameter(REQUEST_NAME_PARAM);
        String languageCode = request.getParameter(REQUEST_LANGUAGE_CODE_PARAM);

        // Builds redirect URL based on whether the request langauge code is valid
        String redirectURL = REDIRECT_URL_PATH;
        if (languageCode != null && Arrays.asList(LANGUAGE_CODES_ARRAY).indexOf(languageCode) >= 0) {
            redirectURL += REDIRECT_URL_QUERY + REDIRECT_URL_QUERY_LANGUAGE_PARAM + languageCode;
        }
        redirectURL += REDIRECT_URL_FRAGMENT;

        // Sets the display name of the current user and stores it in the database
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity entity = new Entity(DATASTORE_USER_DATA_KIND, id);
        entity.setProperty(DATASTORE_USER_DATA_ID_PARAM, id);
        entity.setProperty(DATASTORE_USER_DATA_NAME_PARAM, displayName);
        entity.setProperty(DATASTORE_USER_DATA_EMAIL_PARAM, email);
        datastore.put(entity);

        response.sendRedirect(redirectURL);
    }
}