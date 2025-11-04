/**
 * API Client
 * Handles all HTTP requests to backend
 * Simple and direct - no frameworks needed
 */

const API_BASE = 'http://localhost:7070/api';

const api = {
    /**
     * Get all books
     */
    async getBooks() {
        const response = await fetch(`${API_BASE}/books`);
        if (!response.ok) throw new Error('Failed to fetch books');
        return response.json();
    },

    /**
     * Get single book by ID
     */
    async getBook(bookId) {
        const response = await fetch(`${API_BASE}/books?id=${bookId}`);
        if (!response.ok) throw new Error('Book not found');
        return response.json();
    },

    /**
     * Get library statistics
     */
    async getStats() {
        const response = await fetch(`${API_BASE}/stats`);
        if (!response.ok) throw new Error('Failed to fetch stats');
        return response.json();
    },

    /**
     * Get server status
     */
    async getStatus() {
        const response = await fetch(`${API_BASE}/status`);
        if (!response.ok) throw new Error('Server not responding');
        return response.json();
    },

    /**
     * Login
     * @param {string} username - User account
     * @param {number} password - User password (integer)
     */
    async login(username, password) {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',  // Include cookies
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Login failed');
        }

        return response.json();
    },

    /**
     * Logout
     */
    async logout() {
        const response = await fetch(`${API_BASE}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });

        if (!response.ok) throw new Error('Logout failed');
        return response.json();
    },

    /**
     * Get current user info (whoami)
     */
    async whoami() {
        const response = await fetch(`${API_BASE}/auth/whoami`, {
            credentials: 'include'
        });

        if (!response.ok) {
            // Not logged in
            return null;
        }

        return response.json();
    },

    /**
     * Borrow a book
     * @param {string} bookId - Book ID to borrow
     */
    async borrowBook(bookId) {
        const response = await fetch(`${API_BASE}/books/borrow`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ bookId })
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || 'Failed to borrow book');
        }

        return result;
    },

    /**
     * Return a book
     * @param {string} bookId - Book ID to return
     */
    async returnBook(bookId) {
        const response = await fetch(`${API_BASE}/books/return`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ bookId })
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || 'Failed to return book');
        }

        return result;
    }
};
