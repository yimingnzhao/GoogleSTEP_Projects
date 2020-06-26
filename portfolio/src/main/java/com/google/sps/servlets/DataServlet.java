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

import java.util.*;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private List<String> pokemon;

    @Override
    public void init() {
        pokemon = Arrays.asList(new String[]{"bulbasaur", "charmander", "squirtle"});
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String json = convertToJson();
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    private String convertToJson() {
        String json = "{";
        json += "\"array\": [";
        for (int i = 0; i < this.pokemon.size(); i++) {
            json += "\"" + this.pokemon.get(i) + "\",";
        } 
        json = json.substring(0, json.length() - 1);
        json += "]}";
        return json;
    }
}
