/**
 * Main Application Logic
 * Simple and direct - handles all user interactions
 */

// Global state - simple and direct
let allBooks = [];  // Store all books for filtering/sorting
let filteredBooks = [];  // Currently displayed books

// Check login status on page load
document.addEventListener('DOMContentLoaded', async () => {
    await checkLoginStatus();
    await loadBooks();
    await loadStats();
});

/**
 * Check if user is logged in
 */
async function checkLoginStatus() {
    const userInfoSpan = document.getElementById('user-info');
    const usernameSpan = document.getElementById('username');

    if (!userInfoSpan) return; // Not on a page with user info

    try {
        const user = await api.whoami();

        if (user && user.success) {
            // User is logged in
            usernameSpan.textContent = `${user.userType} ${user.username}`;
            userInfoSpan.style.display = 'inline';
        } else {
            // Not logged in
            userInfoSpan.style.display = 'none';
        }
    } catch (error) {
        // Not logged in or error
        userInfoSpan.style.display = 'none';
    }
}

/**
 * Load books and display them
 */
async function loadBooks() {
    const container = document.getElementById('books-container');

    if (!container) return; // Not on books page

    try {
        const response = await api.getBooks();
        allBooks = response.books || [];
        filteredBooks = [...allBooks];  // Start with all books

        displayBooks(filteredBooks);
        updateSearchInfo();
    } catch (error) {
        console.error('Error loading books:', error);
        container.innerHTML = '<p class="error">載入書籍失敗：' + error.message + '</p>';
    }
}

/**
 * Display books in grid
 */
function displayBooks(books) {
    const container = document.getElementById('books-container');

    if (books.length === 0) {
        container.innerHTML = '<p>目前沒有任何書籍</p>';
        return;
    }

    container.innerHTML = books.map(book => `
        <div class="book-card ${book.isAvailable ? '' : 'borrowed'}">
            <div class="book-header">
                <h3>${book.title}</h3>
                <span class="book-id">ID: ${book.id}</span>
            </div>
            <div class="book-info">
                <p><strong>作者：</strong>${book.author}</p>
                <p><strong>出版社：</strong>${book.publisher}</p>
            </div>
            <div class="book-status ${book.isAvailable ? 'available' : 'unavailable'}">
                ${book.isAvailable ? '✓ 可借閱' : '✗ 已借出'}
            </div>
            <div class="book-actions">
                ${book.isAvailable ?
                    `<button class="btn-borrow" onclick="borrowBook('${book.id}')">借書</button>` :
                    `<button class="btn-return" onclick="returnBook('${book.id}')">還書</button>`
                }
            </div>
        </div>
    `).join('');
}

/**
 * Load library statistics
 */
async function loadStats() {
    const statsDiv = document.getElementById('stats-info');

    if (!statsDiv) return; // Not on a page with stats

    try {
        const response = await api.getStats();
        statsDiv.innerHTML = `📊 ${response.statistics}`;
    } catch (error) {
        statsDiv.innerHTML = '統計資料載入失敗';
    }
}

/**
 * Borrow a book
 */
async function borrowBook(bookId) {
    try {
        const result = await api.borrowBook(bookId);
        alert('✓ ' + result.message);
        location.reload(); // Reload to update book list
    } catch (error) {
        if (error.message.includes('Authentication required')) {
            alert('請先登入才能借書');
            window.location.href = 'login.html';
        } else {
            alert('借書失敗：' + error.message);
        }
    }
}

/**
 * Return a book
 */
async function returnBook(bookId) {
    try {
        const result = await api.returnBook(bookId);
        alert('✓ ' + result.message);
        location.reload(); // Reload to update book list
    } catch (error) {
        if (error.message.includes('Authentication required')) {
            alert('請先登入才能還書');
            window.location.href = 'login.html';
        } else {
            alert('還書失敗：' + error.message);
        }
    }
}

/**
 * Logout user
 */
async function logout() {
    try {
        await api.logout();

        // Clear local storage
        localStorage.removeItem('username');
        localStorage.removeItem('userType');

        alert('已登出');
        location.reload();
    } catch (error) {
        alert('登出失敗：' + error.message);
    }
}

/**
 * Search books by title, author, or publisher
 * Simple client-side filtering - no backend needed for 5 books
 */
function searchBooks() {
    const searchInput = document.getElementById('search-input');
    if (!searchInput) return;

    const searchTerm = searchInput.value.toLowerCase().trim();

    if (searchTerm === '') {
        // No search term, show all books
        filteredBooks = [...allBooks];
    } else {
        // Filter books
        filteredBooks = allBooks.filter(book => {
            return book.title.toLowerCase().includes(searchTerm) ||
                   book.author.toLowerCase().includes(searchTerm) ||
                   book.publisher.toLowerCase().includes(searchTerm) ||
                   book.id.toLowerCase().includes(searchTerm);
        });
    }

    // Re-apply current sort
    sortBooks();
}

/**
 * Sort books based on selected option
 * Simple client-side sorting - Array.sort() is enough
 */
function sortBooks() {
    const sortSelect = document.getElementById('sort-select');
    if (!sortSelect) return;

    const sortBy = sortSelect.value;

    switch (sortBy) {
        case 'id':
            filteredBooks.sort((a, b) => a.id.localeCompare(b.id));
            break;
        case 'title':
            filteredBooks.sort((a, b) => a.title.localeCompare(b.title));
            break;
        case 'author':
            filteredBooks.sort((a, b) => a.author.localeCompare(b.author));
            break;
        case 'available':
            // Available books first, then unavailable
            filteredBooks.sort((a, b) => {
                if (a.isAvailable === b.isAvailable) {
                    return a.id.localeCompare(b.id);
                }
                return a.isAvailable ? -1 : 1;
            });
            break;
    }

    displayBooks(filteredBooks);
    updateSearchInfo();
}

/**
 * Update search result info
 */
function updateSearchInfo() {
    const infoDiv = document.getElementById('search-result-info');
    if (!infoDiv) return;

    const total = allBooks.length;
    const shown = filteredBooks.length;

    if (shown === total) {
        infoDiv.textContent = `顯示全部 ${total} 本書籍`;
        infoDiv.className = 'search-info';
    } else {
        infoDiv.textContent = `找到 ${shown} 本書籍（共 ${total} 本）`;
        infoDiv.className = 'search-info highlight';
    }
}
