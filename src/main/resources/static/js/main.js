'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;

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


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

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
