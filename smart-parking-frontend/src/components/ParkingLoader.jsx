import React from 'react';
import '../styles/ParkingLoader.css';

const ParkingLoader = ({ message = "Finding best spot..." }) => {
    return (
        <div className="parking-loader-container">
            <div className="parking-sign-3d">
                <div className="parking-sign-face front">
                    <span className="parking-letter">P</span>
                </div>
                <div className="parking-sign-face back">
                    <span className="parking-letter">P</span>
                </div>
            </div>
            <div className="loader-shadow"></div>
            <div className="loader-text">{message}</div>
        </div>
    );
};

export default ParkingLoader;
