$(document).ready(function() {
    $('#nlp-submit').click(() => {
        var params = new URLSearchParams();
        params.append('text', $('#nlp-input').val());
        
        // NOTE: To mock the NLP API (for local testing), add the following:
        //params.append('mock', 'mock');

        $('#nlp-output').text('Loading...');
        fetch('/nlp', {
            method: 'POST',
            body: params,
        }).then((response) => response.json()).then((list) => {
            var output = '';
            list.forEach((entity) => {
                output += entity + '<br>'
            });
            $('#nlp-output').html(output);
        }); 
    });
});