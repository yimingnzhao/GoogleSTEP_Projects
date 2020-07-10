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

// Map of pairs of strings for the typewriter text
const TYPEWRITER_TEXT = new Map([
    ['electrical engineers,', 'a computer scientist'],
    ['computer scientists,', 'an electrical engineer'],
    ['musicians,', 'a bass clarinetist'],
    ['my siblings,', 'a nerd'],
    ['Pokemon trainers,', 'a Snorlax'], 
    ['philosophers,', 'a virture seeker'],
]);

// URL and Pokemon types for fetching data from PokeAPI
const POKEAPI_TYPE_URL = 'https://pokeapi.co/api/v2/type/';
const POKEMON_TYPES = [
    'normal', 'fire', 'fighting', 'water',
    'flying', 'grass', 'poison', 'electric',
    'ground', 'psychic', 'rock', 'ice',
    'bug', 'dragon', 'ghost', 'dark', 
    'steel', 'fairy'
];
const POKEMON_TYPE_COLORS = [
    '#999999', '#ff3300', '#990000', '#3385ff',
    '#ccccff', '#33cc33', '#9900cc', '#ffd11a',
    '#e6ac00', '#cc66ff', '#cc8800', '#b3ecff',
    '#99cc00', '#6666ff', '#666699', '#595959',
    '#d0d0e1', '#ffccff'
];


/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * TxtRotate class defines the typewritter animation
 */
class TxtRotate {

    static animateLine1 = true;
    static animateLine2 = false;

    /**
     * Constructor of a TxtRotate object, and begins typewriter animation
     * @param {element} el The HTML element that will show typewriter animation
     * @param {array} toRotate The strings for the typewriter to rotate through
     * @param {number} period The period of a tick when displaying full string (in milliseconds)
     * @param {number} id The unique id of a 'txt-rotate' class object
     */
    constructor(el, toRotate, period, id) {
        this.toRotate = toRotate;
        this.el = el;
        this.period = period;
        this.id = id;
        this.txt = "";
        this.loopNum = 0;
        this.tick();
        this.isDeleting = false;
    }

    /**
     * Performs a single insertion or deletion of a character
     */
    tick() {
        // Gets the current string that will be displayed
        var i = this.loopNum % this.toRotate.length;
        var fullTxt = this.toRotate[i];

        // Updates the current string based on whether characters are being inserted or deleted
        if (this.id == 1 && TxtRotate.animateLine1 || this.id == 2 && TxtRotate.animateLine2) {
            if (this.isDeleting) {
                this.txt = fullTxt.substring(0, this.txt.length - 1);
            } else {
                this.txt = fullTxt.substring(0, this.txt.length + 1);
            }
            this.el.innerHTML = '<span class="wrap">'+this.txt+'</span>';
        }

        var self = this;

        // Gives randomness in speed of each intermediate tick
        var delta = 150 - Math.random() * 100;

        // Ensures deleting tick speeds are on average twice the speed of insertion ticks
        if (this.isDeleting) { delta /= 2; }

        // Uses default tick speeds when the string is complete of when the string is empty
        if (!this.isDeleting && this.txt === fullTxt) {
            delta = this.period;
            this.isDeleting = true;
            if (this.id == 1) {
                TxtRotate.animateLine1 = false;
                TxtRotate.animateLine2 = true;
            }
        } else if (this.isDeleting && this.txt === '') {
            this.isDeleting = false;
            this.loopNum++;
            delta = 500;
            if (this.id == 2) {
                TxtRotate.animateLine1 = true;
                TxtRotate.animateLine2 = false;
            }
        }

        // Sets the timeout for each tick
        setTimeout(function() {
            self.tick();
            }, delta);
    }
}

function setCookie(cname, cvalue) {
  document.cookie = cname + '=' + cvalue;
}

function getCookie(cname) {
  var name = cname + "=";
  var ca = document.cookie.split(';');
  for(var i = 0; i < ca.length; i++) {
    var c = ca[i];
    while (c.charAt(0) == ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return '';
}

/**
 * Begins typewriter animation for each 'txt-rotate' class tag
 */
function startTypewriterAnimation() {
    // Creates arrays for typewriter animation strings
    var rotateStrings1 = [];
    var rotateStrings2 = [];
    for (const [k, v] of TYPEWRITER_TEXT) {
        rotateStrings1.push(k);
        rotateStrings2.push(v);
    }

    // Gets each element of class 'txt-rotate' and begins a new animation for each one
    var elements = $('.txt-rotate');
    for (var i = 0; i < elements.length; i++) {
        var period = parseInt(elements[i].getAttribute('data-period'), 10) || 200;
        var id = parseInt(elements[i].getAttribute('data-id'), 10) || 0;
        if (id == 1) {
            new TxtRotate(elements[i], rotateStrings1, period, id);
        } else if (id == 2) {
            new TxtRotate(elements[i], rotateStrings2, period, id);
        }
    }

    // Inject CSS to create animated cursor 
    var css = document.createElement('style');
    css.type = 'text/css';
    css.innerHTML = '.txt-rotate > .wrap { border-right: 0.08em solid #666 }';
    document.body.appendChild(css);
}

/**
 * Loads comments from database, with option to specify the maximum loaded comments
 * @param {string} query The number of comments that should be fetched
 * @param {string} currentLanguage The language code to use
 */
function loadComments(query, currentLanguage) {
    // Creates the fetch URL with specified maximum limit of comments
    var fetchURL = '/data';
    fetchURL = (hasOnlyDigits(query)) ? fetchURL + '?limit=' + query : fetchURL;

    // Gets comment data and injects HTML to display the comments
    fetch(fetchURL).then((response) => response.json()).then((json) => {
        var display = '<table>';
        for (var i = 0; i < json.length; i++) {
            display += '<tr><td>';
            display += '<b>' + json[i].username + ': </b>';
            display += '<span class="message-text">' + json[i].message + '</span>';
            display += '</tr></td>';
        }
        display += '</table>';
        $('#comments-scroll').html(display);
        
        // Grabs the host language from the URL query
        const urlParams = new URLSearchParams(window.location.search);
        var languageCode = urlParams.get('hl');

        // The priority of language use is:
        //   1. Currently stored cookie
        //   2. Front end change via GET request, which uses the currentLanguage param
        //   3. Back end change via POST request, which uses the URL query
        //   4. Default language (English) if language codes are invalid
        if (getCookie('hl') != '') {
            languageCode = getCookie('hl');
        } else if ($('#language-select option[value="' + currentLanguage + '"]').index() >= 0) {
            languageCode = currentLanguage;
        } 
        var languageCodeIndex = $('#language-select option[value="' + languageCode + '"]').index();
        if (languageCodeIndex >= 0) {
            $('#language-select')[0].selectedIndex = languageCodeIndex;
        }
        
        // Continues to use current language code selection
        if ($('#language-select').val() != 'en') {
            translateComments($('#language-select').val());
        }
    });
}

/**
 * Ensures that the message field of the comment is not blank
 * @return {boolean} Whether the comment is valid or not
 */
function validateCommentForm() {
    var message = document.forms['comment-form']['message'].value;
    if (message == '' || $.trim(message) == '') {
        alert("Message field must be filled out");
        return false;
    }
    document.forms['comment-form']['language-code'].value = $('#language-select').val();
    setCookie('hl', $('#language-select').val());

    // Escapes HTML characters before submitting
    document.forms['comment-form']['message'].value = escapeHtml(message);
    return true;
}

/**
 * Ensures that the display name form is valid
 * @return {boolean} Whether the display name input is valid or not
 */
function validateNameForm() {
    var name = document.forms['display-form']['name'].value;

    // Ensures that the display name is not blank, is less than 20 chars, and is alphanumeric
    if (name == '' || $.trim(name) == '') {
        alert("Display name field must be filled out");
        return false;
    } else if ($.trim(name).length > 20) {
        alert("Display name can be at most 20 characters");
        return false;
    } else if (!isAlphanumeric($.trim(name))) {
        alert("Display name must be alphanumeric");
        return false;
    } 
    document.forms['display-form']['name'].value = $.trim(name);
    document.forms['display-form']['language-code'].value = $('#language-select').val();
    setCookie('hl', $('#language-select').val());
    return true;
}

/**
 * Edits the comment section based on current login status
 */
function manageLogin() {
    // Fetches the current login status and changes html based on the status
    fetch('/login').then((response) => response.json()).then((userAuth) => {
        if (userAuth.isLoggedIn) {
            var breaks = '<br><br>';
            var logoutButton = '<button onclick="document.location=\'' + userAuth.logoutURL + '\'">Logout</button>';
            $('#comments-input').append(breaks);
            $('#comments-input').append(logoutButton);
            showDisplayName();
        } else {
            var br = '<br>';
            var loginText = '<p>Please login to comment</p>';
            var loginButton = '<button onclick="document.location=\'' + userAuth.loginURL + '\'">Login</button';
            $('#comments-input').find('#current-display-name').hide();
            $('#comments-input').find('form').hide();
            $('.hideable-br').hide();
            $('#comments-input').append(loginText);
            $('#comments-input').append(loginButton);
            $('#comments-display').prepend(br);
            $('.comments-div').css('height', 'auto');
        }
    });
}

/**
 * Sets the current display name of the user
 */
function showDisplayName() {
    fetch('/user-data').then((response) => response.text()).then((text) => {
        var displayLocation = $('#current-display-name');
        if ($.trim(text) == '') {
            displayLocation.html('You have not set a display name.');
        } else {
            var span = '<span style="font-family: \'Raleway\', sans-serif;color: #807E7E;">' + text + '</span>';
            displayLocation.html('Your current display name is: ' + span);
        }
    });
}

/**
 * Determines if the input has only digits
 * @param {string} value The input to check
 * @return A boolean whether the input has only digits
 */
function hasOnlyDigits(value) {
    return /^\d+$/.test(value);
}

/**
 * Determines if the input is alphanumeric
 * @param {string} value The input to check
 * @return A boolean whether the input has only digits
 */
function isAlphanumeric(value) {
    return /^[0-9a-zA-Z]+$/.test(value);
}

/**
 * Escapes special characters used in HTML
 * @param {string} text The string to escape HTML characters
 * @return The text with escaped HTML characters
 */
function escapeHtml(text) {
  var map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, function(m) { return map[m]; });
}

/**
 * Fetches data and plots the type distribution of Pokemon and moves
 */
function drawPokemonDataCharts() {
    // Creates array of all API fetches
    var fetches = [];
    POKEMON_TYPES.forEach((type) => {
        fetches.push(fetch(POKEAPI_TYPE_URL + type));
    });

    // Waits for all fetches to be complete, then creates the graphs
    Promise.all(fetches).then((responses) => {
        return Promise.all(responses.map((response) => {
            return response.json();
        }));
    }).then((data) => {
        // Sets up tables to insert data to graph
        const pokemonTable = new google.visualization.DataTable();
        const movesTable = new google.visualization.DataTable();
        pokemonTable.addColumn('string', 'Type');
        pokemonTable.addColumn('number', 'Count');
        movesTable.addColumn('string', 'Type');
        movesTable.addColumn('number', 'Count');

        // Data insertion into the two tables
        data.forEach((typeData) => {
            pokemonTable.addRow([typeData.name, typeData.pokemon.length]);
            movesTable.addRow([typeData.name, typeData.moves.length]);
        });

        // Specifies the options of each graph and plots it in the given div
        var pokemonOptions = {
            title: 'Pokemon Species of Each Type',
            colors: POKEMON_TYPE_COLORS,
            chartArea: {'width': '80%', 'height': '80%'},
            legend: {'position':'right','alignment':'center'},
        }
        var movesOptions = {
            title: 'Pokemon Moves of Each Type',
            colors: POKEMON_TYPE_COLORS,
            chartArea: {'width': '80%', 'height': '80%'},
            legend: {'position':'right','alignment':'center'},
        }
        var pokemonChartDiv = document.getElementById('pokemon-chart');
        const pokemonChart = new google.visualization.PieChart(pokemonChartDiv);
        pokemonChart.draw(pokemonTable, pokemonOptions);
        var movesChartDiv = document.getElementById('moves-chart');
        const movesChart = new google.visualization.PieChart(movesChartDiv);
        movesChart.draw(movesTable, movesOptions);
    });
}

/**
 * Translates the comments backend fetches
 * @param {String} languageCode The language code to translate to
 */
function translateComments(languageCode) {
    // Gets all message texts and performs a fetch for each message
    $('.message-text').each((index, value) => {
        var message = value.innerText;
        value.innerText = 'Loading...';
        const params = new URLSearchParams();
        params.append('message', message);
        params.append('languageCode', languageCode);
        params.append('mock', 'mock');

        fetch('/translate', {
            method: 'POST',
            body: params,
        }).then((response) => response.text()).then((translatedMessage) => {
            value.innerText = translatedMessage;
        }); 
    });
}

/**
 * Executes when document is loaded
 */
 $(document).ready(function() {

     // Animates scroll behavior only when navigating within page
     $('.scroll').click(function() {
         $('html').css('scroll-behavior', 'smooth');
     });

     // Animates the picture slideshow
     $('#slideshow > div:gt(0)').hide();
     setInterval(function() {
         $('#slideshow > div:first')
         .fadeOut(1000)
         .next()
         .fadeIn(1000)
         .end()
         .appendTo('#slideshow');
     }, 3000);

    // Animates the typing animation
    window.onload = function() {
        startTypewriterAnimation();
        loadComments('', '');
    };

    // Opens modal for extra descriptions for of work and projects
    $('.modal-btn').click(function() {
        // Gets the text to insert into the modal from HTML
        var headerText = $(this).find('.modal-text-header').text();
        var bodyText = $(this).find('.modal-text').html();
        var techText = $(this).find('.modal-text-tech').text();
        // Inserts text to fields in the modal
        $('#modal-header-display').text(headerText);
        $('#modal-text-display').html(bodyText);
        // Inserts a list of technologies if applicable 
        if (techText) {
            $('#modal-tech-list').html('<b>Technologies:</b>' + techText);
            $('#modal-tech-list').css('display', 'block');
        } else {
            $('#modal-tech-list').css('display', 'none');
        }
        // Displays the modal
        $('#modal-div').css('display', 'block');
    });

    // Option to close modal for when the close button is clicked
    $('.close').click(function() {
        $('#modal-div').css('display', 'none');
    });
    
    // Option to close modal for when anywhere outside the modal is clicked
    $(window).click(function(event) {
        // Must check if the modal is open before closing the modal
        if (event.target ==  document.getElementById('modal-div')) {
            $('#modal-div').css('display', 'none');
        }
    });

    // Loads comments based on the limit passed
    $('#comment-limit-button').click(function() {
        setCookie('hl', $('#language-select').val());
        loadComments($('#comment-limit-input').val(), $('#language-select').val());
    });

    // Deletes all comments from database
    $('#comment-delete-button').click(function() {
        fetch('/data').then((response) => response.json()).then((json) => {
            // Builds the data to send to DeleteCommentsServlet
            var stringBuild = '';
            for (var i = 0; i < json.length; i++) {
                stringBuild += json[i].id + ',';
            }
            stringBuild = stringBuild.slice(0, -1);

            // Sends built data as a POST request and then reloads page
            $.ajax({
                url: '/delete-data',
                type: 'POST',
                data:stringBuild,
                contentType: 'text/plain; charset=UTF-8',
                dataType: 'text',
                success: () => {
                    location.reload();
                }
            });
        });
    });

    // Google Authentication API 
    manageLogin();

    // Google Chart API
    google.charts.load('current', {'packages':['corechart']});
    google.charts.setOnLoadCallback(drawPokemonDataCharts);

    // Google Translation API
    $('#language-select').change(function() {
        setCookie('hl', $(this).val());
        translateComments($(this).val());
    });
    
 });