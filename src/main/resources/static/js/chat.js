function addMessage(sender, text) {
    const chatBox = document.getElementById("fc-chat-box");
    const msg = document.createElement("div");
    msg.className = sender === "user" ? "fc-chat-user" : "fc-chat-bot";
    msg.innerText = text;
    chatBox.appendChild(msg);
    chatBox.scrollTop = chatBox.scrollHeight;
}
function handleSuggestion(text) {
    addMessage("user", "🧑 " + text);
    document.getElementById("fc-user-input").value = "";
    document.getElementById("fc-chat-suggestions").style.display = "none";
    sendToBot(text);
}
let hasGreeted = false;

function toggleChat() {
    const chatContainer = document.getElementById("fc-chat-box-container");
    const suggestionBox = document.getElementById("fc-chat-suggestions");

    if (chatContainer.style.display === "none" || chatContainer.style.display === "") {
        chatContainer.style.display = "flex";
        suggestionBox.style.display = "flex"; // hiện gợi ý khi mở lần đầu

        if (!hasGreeted) {
            addMessage("bot", "🎬 Xin chào bạn! Chúc bạn có một ngày tốt lành.");
            addMessage("bot", "🤖 Bạn cần mình hỗ trợ gì ha?");
            hasGreeted = true;
        }
    } else {
        chatContainer.style.display = "none";
    }
}


function sendMessage() {
    const input = document.getElementById("fc-user-input");
    const message = input.value.trim();
    if (!message) return;

    addMessage("user", "🧑 " + message);
    input.value = "";
    document.getElementById("fc-chat-suggestions").style.display = "none";
    sendToBot(message);
}

function sendToBot(message) {
    fetch("/api/chat", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({"message": message})
    })
        .then(res => res.text())
        .then(reply => {
            const youtubeMatch = reply.match(/https:\/\/youtu\.be\/([a-zA-Z0-9_-]+)/);
            if (youtubeMatch) {
                const videoId = youtubeMatch[1];
                addMessage("bot", "🎬 Trailer:");
                const chatBox = document.getElementById("fc-chat-box");
                const iframe = document.createElement("iframe");
                iframe.src = `https://www.youtube.com/embed/${videoId}`;
                iframe.allowFullscreen = true;
                chatBox.appendChild(iframe);
                chatBox.scrollTop = chatBox.scrollHeight;
            } else {
                addMessage("bot", "🤖 " + reply);
            }
        })
        .catch(() => addMessage("bot", "⚠️ Lỗi kết nối đến chatbot."));
}


    function showTooltip(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.style.opacity = '1';
    el.style.transform = 'translateX(0)';
    setTimeout(() => {
    el.style.opacity = '0';
    el.style.transform = 'translateX(40px)';
}, 3000); // hiển thị trong 3s
}

    // Lặp tooltip chat và telegram lần lượt
    setInterval(() => {
    showTooltip("tooltip-chat");
    setTimeout(() => showTooltip("tooltip-telegram"), 1800);
}, 6500);




document.getElementById("fc-user-input")
    .addEventListener("keypress", function(e) {
        if (e.key === "Enter") sendMessage();
    });
