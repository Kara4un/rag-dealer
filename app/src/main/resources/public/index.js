async function loadHistory() {
    const res = await fetch('/api/history');
    const history = await res.json();
    history.forEach(addMessage);
}

async function sendMessage() {
    const input = document.getElementById('input');
    const text = input.value;
    if (!text) {
        return;
    }
    const res = await fetch('/api/chat', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({text})
    });
    const data = await res.json();
    document.getElementById('messages').innerHTML = '';
    data.history.forEach(addMessage);
    input.value = '';
}

function addMessage(msg) {
    const div = document.createElement('div');
    div.className = msg.role;
    div.innerHTML = marked.parse(msg.content);
    document.getElementById('messages').appendChild(div);
}

document.getElementById('send').addEventListener('click', sendMessage);
loadHistory();
