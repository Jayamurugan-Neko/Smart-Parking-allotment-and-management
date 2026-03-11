import { useState, useEffect, useRef } from "react";

/**
 * SearchBar Component
 * 
 * Purpose: A custom search input field that remembers what the user searched for previously.
 * It shows a dropdown of past searches to act as autocomplete suggestions.
 * 
 * Props:
 * - onSearch(query): Function called when the user hits search.
 * - suggestions: array of suggestion strings for the dropdown (optional).
 * - userKey: string used to save the search history specifically for this user (optional).
 */
function SearchBar({ onSearch, suggestions = [], userKey }) {
  const [query, setQuery] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);
  const [history, setHistory] = useState([]);
  const ref = useRef();

  const storageKey = userKey ? `searchHistory_${userKey}` : `searchHistory_public`;

  useEffect(() => {
    try {
      const raw = localStorage.getItem(storageKey);
      if (raw) setHistory(JSON.parse(raw));
    } catch (e) {
      setHistory([]);
    }
  }, [storageKey]);

  useEffect(() => {
    const onDocClick = (e) => {
      if (!ref.current || ref.current.contains(e.target)) return;
      setShowDropdown(false);
    };
    document.addEventListener("click", onDocClick);
    return () => document.removeEventListener("click", onDocClick);
  }, []);

  const saveToHistory = (q) => {
    if (!q || !q.trim()) return;
    const next = [q.trim(), ...history.filter(h => h !== q.trim())].slice(0, 10);
    setHistory(next);
    try { localStorage.setItem(storageKey, JSON.stringify(next)); } catch {}
  };

  const handleSubmit = (e) => {
    e?.preventDefault();
    onSearch(query);
    saveToHistory(query);
    setShowDropdown(false);
  };

  const handlePick = (text) => {
    setQuery(text);
    onSearch(text);
    saveToHistory(text);
    setShowDropdown(false);
  };

  const combinedSuggestions = Array.from(new Set([...(history || []), ...suggestions]));

  return (
    <div ref={ref} style={{ marginBottom: 16 }}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8 }}>
        <input
          type="text"
          placeholder="Search by slot name, city, or village..."
          value={query}
          onChange={e => setQuery(e.target.value)}
          onFocus={() => setShowDropdown(true)}
          style={{ flex: 1, padding: 8 }}
        />
        <button type="submit">Search</button>
      </form>

      {showDropdown && combinedSuggestions.length > 0 && (
        <div style={{ background: '#fff', border: '1px solid #ddd', marginTop: 6, maxHeight: 220, overflow: 'auto', boxShadow: '0 6px 18px rgba(0,0,0,0.08)' }}>
          {combinedSuggestions.map((s, i) => (
            <div key={i} style={{ padding: 8, cursor: 'pointer', borderBottom: '1px solid #f1f1f1' }} onClick={() => handlePick(s)}>
              {s}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default SearchBar;
