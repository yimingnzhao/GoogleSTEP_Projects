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
import com.google.gson.Gson;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.LanguageServiceClient;
import java.util.*;


/** Servlet that interacts with Google's NLP API */
@WebServlet("/nlp")
public class NaturalLanguageServlet extends HttpServlet {

    private static final String RESPONSE_JSON_CONTENT = "application/json;";
    private static final String REQUEST_TEXT_PARAM = "text";
    private static final String REQUEST_MOCK_MARAM = "mock";
    private static final String MOCK_ENTITY_1 = "Currently mocking NLP";
    private static final String MOCK_ENTITY_2 = "Your text input is:";
    private static final String MOCK_INFO = "Currently mocking NLP...";

    /**
     * Gets database data for comments
     * @param request The request object 
     * @param response The response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String text = request.getParameter(REQUEST_TEXT_PARAM);
        List<String> entities = new ArrayList<String>();
        Gson gson = new Gson();

        response.setContentType(RESPONSE_JSON_CONTENT);

        // If mocking, add predetermined strings to the entities list
        if (request.getParameter(REQUEST_MOCK_MARAM) != null) {
            entities.add(MOCK_ENTITY_1);
            entities.add(MOCK_ENTITY_2);
            entities.add(text);
            response.getWriter().println(gson.toJson(entities));
            System.out.println(MOCK_INFO);
            return;
        }

        // When not mocking, uses Google NLP API objects to build entities list
        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
            AnalyzeEntitiesRequest nlpRequest = 
                AnalyzeEntitiesRequest.newBuilder()
                .setDocument(doc)
                .setEncodingType(EncodingType.UTF16)
                .build();

            AnalyzeEntitiesResponse nlpResponse = language.analyzeEntities(nlpRequest);
            for (Entity entity : nlpResponse.getEntitiesList()) {
                entities.add(entity.getName());
            }
        }
        response.getWriter().println(new Gson().toJson(entities));
    }
}