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
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");

        UserService userService = UserServiceFactory.getUserService();
        if (!userService.isUserLoggedIn()) {
            response.getWriter().println("");
            return;
        }

        String userId = userService.getCurrentUser().getUserId();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Filter propertyFilter = new FilterPredicate("id", FilterOperator.EQUAL, userId);
        Query query = new Query("UserData").setFilter(propertyFilter);
        PreparedQuery results = datastore.prepare(query);
        List<Entity> listResults = results.asList(FetchOptions.Builder.withDefaults());

        if (listResults.size() != 1) {
            response.getWriter().println("");
            return;
        } 

        String displayName = (String) listResults.get(0).getProperty("displayName");
        response.getWriter().println(displayName);
    }

    /**
     * Modifies display name of current user and changes the user'd database entry
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        if (!userService.isUserLoggedIn()) {
            response.sendRedirect("/#comments");
            return;
        }

        String id = userService.getCurrentUser().getUserId();
        String email = userService.getCurrentUser().getEmail();
        String displayName = request.getParameter("name");

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity entity = new Entity("UserData", id);
        entity.setProperty("id", id);
        entity.setProperty("displayName", displayName);
        entity.setProperty("email", email);
        datastore.put(entity);

        response.sendRedirect("/#comments");
    }
}