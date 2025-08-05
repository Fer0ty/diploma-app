import React from 'react';
import './LoadingScreen.css';

const LoadingScreen = () => {
    return (
        <div className="loading-screen">
            <div className="loading-container">
                <div className="loading-spinner-large"></div>
                <p className="loading-text">Загрузка...</p>
            </div>
        </div>
    );
};

export default LoadingScreen;