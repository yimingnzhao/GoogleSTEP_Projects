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
import com.google.gson.Gson;
import com.google.sps.data.UserAuth;

/** Servlet that interacts with user login */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /**
     * Gets current login status
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserAuth userAuth;
        UserService userService = UserServiceFactory.getUserService();

        // Sets parameters of the UserAuth object based on current login status
        if (userService.isUserLoggedIn()) {
            String userEmail = userService.getCurrentUser().getEmail();
            String urlToRedirectToAfterUserLogsOut = "/";
            String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
            userAuth = new UserAuth(true, "", logoutUrl, userEmail);
        } else {
            String urlToRedirectToAfterUserLogsIn = "/";
            String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
            userAuth = new UserAuth(false, loginUrl, "", "");
        }

        // Converts object to JSON and returns to front-end
        Gson gson = new Gson();
        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(userAuth));
    }
}