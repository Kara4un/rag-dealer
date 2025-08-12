const $ = (id) => document.getElementById(id);

function renderMarkdown(text) {
  try {
    if (window.marked && typeof marked.parse === 'function') {
      return marked.parse(text);
    }
  } catch (e) {}
  // Fallback: escape and convert line breaks
  const esc = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;');
  return esc.replace(/\n/g, '<br/>');
}

function formatTime(ts) {
  try {
    if (typeof ts === 'number') {
      return new Date(ts).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
    }
    if (typeof ts === 'string') {
      return new Date(ts).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
    }
  } catch(_) {}
  return new Date().toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
}

function showSnackbar(message) {
  const bar = $('snackbar');
  bar.textContent = message;
  bar.classList.add('show');
  setTimeout(() => bar.classList.remove('show'), 2500);
}

function addMessage(msg) {
  const wrapper = document.createElement('div');
  wrapper.className = `message ${msg.role}`;

  const avatar = document.createElement('div');
  avatar.className = 'avatar';
  avatar.textContent = msg.role === 'user' ? 'U' : 'A';

  const bubble = document.createElement('div');
  bubble.className = 'bubble';

  const content = document.createElement('div');
  content.className = 'content';
  content.innerHTML = renderMarkdown(msg.content);

  const meta = document.createElement('div');
  meta.className = 'meta';
  const ts = (msg.timestamp !== undefined && msg.timestamp !== null) ? msg.timestamp : Date.now();
  meta.textContent = formatTime(ts);

  bubble.appendChild(content);
  bubble.appendChild(meta);
  wrapper.appendChild(avatar);
  wrapper.appendChild(bubble);
  $('messages').appendChild(wrapper);
}

function scrollToBottom() {
  const el = $('messages');
  el.scrollTop = el.scrollHeight;
}

async function loadHistory() {
  try {
    const res = await fetch('/api/history');
    if (!res.ok) throw new Error('failed');
    const history = await res.json();
    $('messages').innerHTML = '';
    history.forEach(addMessage);
    scrollToBottom();
  } catch (e) {
    showSnackbar('Не удалось загрузить историю');
  }
}

async function sendMessage(evt) {
  if (evt) evt.preventDefault();
  const input = $('input');
  const btn = $('send');
  const typing = $('typing');
  const text = input.value.trim();
  if (!text) return;

  // optimistic render user message
  addMessage({role: 'user', content: text, timestamp: Date.now()});
  scrollToBottom();

  // lock UI
  input.value = '';
  input.disabled = true;
  btn.disabled = true;
  typing.hidden = false;

  try {
    const res = await fetch('/api/chat', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({text})
    });
    if (!res.ok) throw new Error('network');
    const data = await res.json();
    // Re-render last N messages from server to guarantee consistency
    $('messages').innerHTML = '';
    data.history.forEach(addMessage);
    scrollToBottom();
  } catch (e) {
    showSnackbar('Ошибка отправки. Проверьте соединение.');
  } finally {
    input.disabled = false;
    btn.disabled = false;
    typing.hidden = true;
    input.focus();
  }
}

$('composer').addEventListener('submit', sendMessage);
$('send').addEventListener('click', sendMessage);
$('input').addEventListener('keydown', (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  }
});

loadHistory();
window.addEventListener('load', scrollToBottom);
