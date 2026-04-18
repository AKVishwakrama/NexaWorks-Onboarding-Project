import { useState, useRef, useEffect } from 'react';
import { MessageCircle, X, Send, Bot, Loader } from 'lucide-react';
import { aiService } from '../../services/api';
import { useAuth } from '../../context/AuthContext';

export default function Chatbot() {
  const [open, setOpen]       = useState(false);
  const [input, setInput]     = useState('');
  const [loading, setLoading] = useState(false);
  const [msgs, setMsgs]       = useState([
    { role: 'assistant', content: "👋 Hi! I'm **NexaBot**, your AI onboarding assistant.\n\nAsk me about documents, progress, risk scores, meetings, training, or anything about NexaWorks!" }
  ]);
  const endRef = useRef(null);
  const { user } = useAuth();

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [msgs]);

  const send = async () => {
    if (!input.trim() || loading) return;
    const userMsg = input.trim();
    setInput('');
    setMsgs(m => [...m, { role: 'user', content: userMsg }]);
    setLoading(true);
    try {
      const history = msgs.slice(-8).map(m => ({ role: m.role, content: m.content }));
      const res = await aiService.chat(userMsg, history, user?.role, user?.name);
      setMsgs(m => [...m, { role: 'assistant', content: res.data.reply }]);
    } catch {
      setMsgs(m => [...m, { role: 'assistant', content: "Sorry, I'm having trouble connecting right now. Please try again shortly." }]);
    } finally { setLoading(false); }
  };

  const formatMsg = (text) => {
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br/>');
  };

  const quickQ = ["What documents do I need?", "Check my risk score", "Training schedule", "How to complete onboarding?"];

  return (
    <>
      {/* Toggle button */}
      <button onClick={() => setOpen(o => !o)}
              className="fixed bottom-6 right-6 z-50 w-14 h-14 rounded-full shadow-2xl flex items-center justify-center transition-all duration-300 hover:scale-110"
              style={{ background: 'linear-gradient(135deg,#4f46e5,#7c3aed)' }}>
        {open ? <X size={22} className="text-white" /> : <MessageCircle size={22} className="text-white" />}
        <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-emerald-400 border-2 border-white animate-pulse" />
      </button>

      {/* Chat window */}
      {open && (
        <div className="fixed bottom-24 right-6 z-50 w-96 h-[520px] rounded-2xl shadow-2xl flex flex-col overflow-hidden slide-up"
             style={{ background: 'var(--bg-card)', border: '1px solid var(--border)' }}>

          {/* Header */}
          <div className="flex items-center gap-3 px-4 py-3 text-white"
               style={{ background: 'linear-gradient(135deg,#4f46e5,#7c3aed)' }}>
            <div className="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center">
              <Bot size={18} />
            </div>
            <div>
              <div className="font-bold text-sm">NexaBot</div>
              <div className="text-xs text-indigo-200">AI Onboarding Assistant • Online</div>
            </div>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {msgs.map((m, i) => (
              <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                {m.role === 'assistant' && (
                  <div className="w-7 h-7 rounded-full bg-indigo-100 dark:bg-indigo-900/50 flex items-center justify-center mr-2 flex-shrink-0 mt-1">
                    <Bot size={14} className="text-indigo-600 dark:text-indigo-400" />
                  </div>
                )}
                <div className={`max-w-[78%] px-3.5 py-2.5 rounded-2xl text-sm leading-relaxed
                  ${m.role === 'user'
                    ? 'text-white rounded-br-sm'
                    : 'rounded-bl-sm'}`}
                     style={{
                       background: m.role === 'user'
                         ? 'linear-gradient(135deg,#4f46e5,#7c3aed)'
                         : 'var(--bg)',
                       color: m.role === 'user' ? 'white' : 'var(--text)',
                       border: m.role === 'assistant' ? '1px solid var(--border)' : 'none'
                     }}
                     dangerouslySetInnerHTML={{ __html: formatMsg(m.content) }} />
              </div>
            ))}
            {loading && (
              <div className="flex items-center gap-2 text-sm" style={{ color:'var(--text-muted)' }}>
                <Loader size={14} className="animate-spin" /> NexaBot is thinking...
              </div>
            )}
            <div ref={endRef} />
          </div>

          {/* Quick questions */}
          {msgs.length <= 1 && (
            <div className="px-4 pb-2 flex flex-wrap gap-1.5">
              {quickQ.map(q => (
                <button key={q} onClick={() => { setInput(q); }}
                        className="text-xs px-3 py-1.5 rounded-full border font-medium transition-all hover:border-indigo-400"
                        style={{ borderColor:'var(--border)', color:'var(--text-muted)' }}>
                  {q}
                </button>
              ))}
            </div>
          )}

          {/* Input */}
          <div className="p-3 border-t" style={{ borderColor:'var(--border)' }}>
            <div className="flex gap-2">
              <input value={input} onChange={e => setInput(e.target.value)}
                     onKeyDown={e => e.key === 'Enter' && !e.shiftKey && send()}
                     placeholder="Ask NexaBot anything..."
                     className="input-field flex-1 py-2.5 text-sm" />
              <button onClick={send} disabled={loading || !input.trim()}
                      className="w-10 h-10 rounded-xl flex items-center justify-center text-white transition-all disabled:opacity-40"
                      style={{ background: 'linear-gradient(135deg,#4f46e5,#7c3aed)' }}>
                <Send size={16} />
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
