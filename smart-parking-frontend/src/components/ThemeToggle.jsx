import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';

/**
 * ThemeToggle Component
 * 
 * Purpose: A floating button that lets users switch between Light and Dark mode.
 * It remembers their choice in the browser's localStorage so it stays consistent
 * even if they refresh the page. It also listens to the Operating System's default theme.
 */
const storageKey = 'theme-preference';

const getColorPreference = () => {
    if (localStorage.getItem(storageKey))
        return localStorage.getItem(storageKey);
    else
        return window.matchMedia('(prefers-color-scheme: dark)').matches
            ? 'dark'
            : 'light';
};

const ThemeToggle = () => {
    const [theme, setTheme] = useState(getColorPreference());
    const location = useLocation();

    const reflectPreference = (currentTheme) => {
        document.firstElementChild.setAttribute('data-theme', currentTheme);
    };

    // Sync theme changes to DOM and LocalStorage
    useEffect(() => {
        reflectPreference(theme);
        localStorage.setItem(storageKey, theme);
    }, [theme]);

    // Handle system preference changes
    useEffect(() => {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        const handleChange = ({ matches: isDark }) => {
            setTheme(isDark ? 'dark' : 'light');
        };

        mediaQuery.addEventListener('change', handleChange);
        return () => mediaQuery.removeEventListener('change', handleChange);
    }, []);

    const onClick = () => {
        setTheme(prevTheme => (prevTheme === 'light' ? 'dark' : 'light'));
    };

    // Hide on /slots page as requested
    if (location.pathname === '/slots') {
        return null;
    }

    return (
        <button
            className="theme-toggle"
            id="theme-toggle"
            title="Toggles light & dark"
            aria-label={theme}
            aria-live="polite"
            onClick={onClick}
        >
            <svg
                className="sun-and-moon"
                aria-hidden="true"
                width="24"
                height="24"
                viewBox="0 0 24 24"
            >
                <mask className="moon" id="moon-mask">
                    <rect x="0" y="0" width="100%" height="100%" fill="white" />
                    <circle cx="24" cy="10" r="6" fill="black" />
                </mask>
                <circle
                    className="sun"
                    cx="12"
                    cy="12"
                    r="6"
                    mask="url(#moon-mask)"
                    fill="currentColor"
                />
                <g className="sun-beams" stroke="currentColor">
                    <line x1="12" y1="1" x2="12" y2="3" />
                    <line x1="12" y1="21" x2="12" y2="23" />
                    <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
                    <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
                    <line x1="1" y1="12" x2="3" y2="12" />
                    <line x1="21" y1="12" x2="23" y2="12" />
                    <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
                    <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
                </g>
            </svg>
        </button>
    );
};

export default ThemeToggle;
