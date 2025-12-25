/**
 * Login page initialization script.
 * Handles error display from OAuth2 authentication failures.
 */
(function() {
    'use strict';

    /**
     * Displays error message in the error container.
     * @param {string} message - The error message to display
     */
    function showError(message) {
        var errorContainer = document.getElementById('error-container');
        if (errorContainer) {
            var errorDiv = document.createElement('div');
            errorDiv.className = 'error-message';
            errorDiv.textContent = message;
            errorContainer.appendChild(errorDiv);
        }
    }

    /**
     * Maps error codes to user-friendly messages.
     * @param {string} errorCode - The error code from URL parameters
     * @returns {string} User-friendly error message
     */
    function getErrorMessage(errorCode) {
        var errorMessages = {
            'authentication_failed': 'Authentication failed. Please try again or contact support.',
            'access_denied': 'Access was denied. Please try again.',
            'invalid_request': 'Invalid authentication request. Please try again.',
            'server_error': 'A server error occurred. Please try again later.'
        };

        return errorMessages[errorCode] || 'An error occurred during authentication. Please try again.';
    }

    /**
     * Initializes the login page by checking for error parameters.
     */
    function init() {
        var urlParams = new URLSearchParams(window.location.search);
        var error = urlParams.get('error');

        if (error) {
            var errorMessage = getErrorMessage(error);
            showError(errorMessage);

            // Log the error to console (without sensitive data)
            console.error('Login error occurred:', error);
        }
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
