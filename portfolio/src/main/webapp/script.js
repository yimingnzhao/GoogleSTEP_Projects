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
 */
function loadComments(query) {
    // Creates the fetch URL with specified maximum limit of comments
    var fetchURL = '/data';
    fetchURL = (hasOnlyDigits(query)) ? fetchURL + '?limit=' + query : fetchURL;

    // Gets comment data and injects HTML to display the comments
    fetch(fetchURL).then((response) => response.json()).then((json) => {
        var display = '<ul>';
        for (var i = 0; i < json.length; i++) {
            display += '<li>' + json[i].message + '</li>';
        }
        display += '</ul>';
        $('#comments-section').find('p').html(display);
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
 * Executes when document is loaded
 */
 $(document).ready(function() {

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
        loadComments('');
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
        loadComments($('#comment-limit-input').val());
    });

    // Deletes all comments from database
    $('#comment-delete-button').click(function() {
        fetch('/data').then((response) => response.json()).then((json) => {
            
            var stringBuild = '';
            for (var i = 0; i < json.length; i++) {
                stringBuild += json[i].id + ',';
            }
            stringBuild = stringBuild.slice(0, -1);

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
    
 });