'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var typingIndicator = document.querySelector('#typingIndicator');

var stompClient = null;
var username = null;
var typingTimeout = null;
var lastTypingTime = 0;

// Simple "Pop" sound
var notificationSound = new Audio("data:audio/wav;base64,UklGRl9vT19XQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YU"); // Short placeholder, will use a real one or just let the user know it's a placeholder if it fails.
// Let's use a better base64 for a real "pop" sound to ensure it works.
notificationSound = new Audio("https://actions.google.com/sounds/v1/cartoon/pop.ogg"); // Using a reliable URL for now as Base64 can be long.

var colors = [
    'bg-red', 'bg-pink', 'bg-purple', 'bg-indigo', 'bg-blue', 'bg-teal', 'bg-green', 'bg-orange'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');
        chatPage.style.display = 'block'; // Ensure it's visible

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({ sender: username, type: 'JOIN' })
    )

    // connectingElement.classList.add('hidden');
}


function onError(error) {
    alert('Could not connect to WebSocket server. Please refresh this page to try again!');
}


function sendMessage(event) {
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

function sendTypingSignal() {
    var now = new Date().getTime();
    if (!stompClient) return;

    // Throttle: send typing signal at most once every 1 second
    if (now - lastTypingTime > 1000) {
        var chatMessage = {
            sender: username,
            type: 'TYPING'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        lastTypingTime = now;
    }
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    // Handle TYPING message
    if (message.type === 'TYPING') {
        if (message.sender !== username) {
            showTypingIndicator(message.sender);
        }
        return; // Don't process as a chat message
    }

    // Hide typing indicator immediately if we receive a CHAT message from that user
    if (message.type === 'CHAT' && message.sender !== username) {
        hideTypingIndicator();
    }

    var messageElement = document.createElement('li');
    messageElement.classList.add('message-item');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');
        if (message.sender === username) {
            messageElement.classList.add('self');
        }

        var avatarElement = document.createElement('div');
        avatarElement.className = 'avatar ' + getAvatarColor(message.sender);
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        usernameElement.className = 'sender-name';
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);

        var messageContentElement = document.createElement('div');
        messageContentElement.className = 'message-content';

        messageContentElement.appendChild(usernameElement);

        var textElement = document.createElement('p');
        textElement.className = 'text-content';
        var messageText = document.createTextNode(message.content);
        textElement.appendChild(messageText);

        messageContentElement.appendChild(textElement);

        messageElement.appendChild(messageContentElement);
    }

    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        var textElement = document.createTextNode(message.content);
        messageElement.appendChild(textElement);
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function showTypingIndicator(sender) {
    var textSpan = typingIndicator.querySelector('span:first-child');
    textSpan.textContent = sender + ' is typing...';
    typingIndicator.classList.remove('hidden');

    if (typingTimeout) {
        clearTimeout(typingTimeout);
    }

    // Hide after 2.5 seconds of inactivity
    typingTimeout = setTimeout(hideTypingIndicator, 2500);
}

function hideTypingIndicator() {
    typingIndicator.classList.add('hidden');
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
messageInput.addEventListener('input', sendTypingSignal, true);
