var socket = null;
var isopen = false;

// Awwwwww yis globals!
var feedback = {};

function timeSince(date) {

    var seconds = Math.floor((new Date() - date) / 1000);

    var interval = Math.floor(seconds / 31536000);

    if (interval > 1) {
        return interval + " years";
    }
    interval = Math.floor(seconds / 2592000);
    if (interval > 1) {
        return interval + " months";
    }
    interval = Math.floor(seconds / 86400);
    if (interval > 1) {
        return interval + " days";
    }
    interval = Math.floor(seconds / 3600);
    if (interval > 1) {
        return interval + " hours";
    }
    interval = Math.floor(seconds / 60);
    if (interval > 1) {
        return interval + " minutes";
    }
    return Math.floor(seconds) + " seconds";
}

window.onload = function() {

    console.log("Connecting...");
    socket = new WebSocket("ws://127.0.0.1:9000/openwebsocket");

    socket.onopen = function() {
        console.log("Websocket open!");
        isopen = true;

        var message = "{\"message\": \"hey\"}";
        console.log("Sending: " + message);
        socket.send(message);
    };

    socket.onmessage = function(e) {
        var feedback_json = e.data;
        console.log("New feedback message received: " + feedback_json);
        feedback = jQuery.parseJSON(feedback_json);
        console.log(feedback);

        // Fade out and in
        $('main').fadeOut(
            2000,
            function() {
                setTimeout(swapFeedback, 250)
            }
        );
    };

    socket.onclose = function(e) {
        console.log("Connection closed:");
        console.log(e);
        socket = null;
        isopen = false;
    };
};

function swapFeedback() {
    //feedback
    $('main').fadeIn(4000);
    $('#Order').text("Order #" + feedback.orderNumber);
    $('#feedback').html(feedback.feedbackText);

    $('#NPS').text(feedback.npsScore);

    if ('timestampUtc' in feedback) {
        var dt = new Date();
        dt.setTime(feedback.timestampUtc);
        $('#datetime').text(' - ' + timeSince(dt) + ' ago');
    } else {
        $('#datetime').text('');
    }

    $('.star').remove();
    for (i = 0; i < feedback.npsScore; i++) {
        $("#stars").append('<div class="star star-full"></div>');
    }
    for (i = 0; i < (10 - feedback.npsScore); i++) {
        $("#stars").append('<div class="star"></div>');
    }

    $("body").removeClass();
    $("body").addClass("nps-" + feedback.npsScore);

    $('#location').text(feedback.store.name);
}