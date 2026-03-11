import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import './styles/common.css';
import App from './App';

/**
 * index.js
 * 
 * Purpose: This is the very first file that runs when the React application starts (the entry point).
 * It tells React to grab the <App /> component and inject it into the raw HTML file 
 * located at `public/index.html` inside the div with the id 'root'.
 */

// Grab the HTML element where the entire React app will live
const root = ReactDOM.createRoot(document.getElementById('root'));

// Render (draw) the main App component inside that HTML element
root.render(
    <App />
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
