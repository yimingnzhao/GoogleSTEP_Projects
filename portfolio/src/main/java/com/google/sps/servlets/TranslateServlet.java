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

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servelet that translates inputted text */
@WebServlet("/translate")
public class TranslateServlet extends HttpServlet {

    private static final String REQUEST_MESSAGE_PARAM = "message";
    private static final String REQUEST_LANGUAGE_CODE_PARAM = "languageCode";
    private static final String REQUEST_MOCK_PARAM = "mock";
    private static final String RESPONSE_TEXT_CONTENT = "text/html; charset=UTF-8";
    private static final String RESPONSE_CHAR_ENCODING = "UTF-8";

    /**
     * Gets the translated message based on front end text and language code
     * @param request The request object
     * @param response The response object
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the request parameters
        String originalText = request.getParameter(REQUEST_MESSAGE_PARAM);
        String languageCode = request.getParameter(REQUEST_LANGUAGE_CODE_PARAM);
        String mock = request.getParameter(REQUEST_MOCK_PARAM);

        // Gives option to mock the Translation API for testing
        String translatedText;
        if (mock != null) {
            translatedText = originalText + languageCode;
        } else {
            Translate translate = TranslateOptions.getDefaultInstance().getService();
            Translation translation = 
                translate.translate(originalText, Translate.TranslateOption.targetLanguage(languageCode));
            translatedText = translation.getTranslatedText();
        }

        // Output the translation
        response.setContentType(RESPONSE_TEXT_CONTENT);
        response.setCharacterEncoding(RESPONSE_CHAR_ENCODING);
        response.getWriter().println(translatedText);
    }
}