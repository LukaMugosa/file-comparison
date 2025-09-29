const form = document.getElementById('uploadForm');
const loading = document.getElementById('loading');
const error = document.getElementById('error');
const results = document.getElementById('results');
const submitBtn = document.getElementById('submitBtn');

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const file1 = document.getElementById('file1').files[0];
    const file2 = document.getElementById('file2').files[0];

    if (!file1 || !file2) {
        showError('Please select both files');
        return;
    }

    const formData = new FormData();
    formData.append('file1', file1);
    formData.append('file2', file2);

    loading.style.display = 'block';
    error.style.display = 'none';
    results.classList.remove('show');
    submitBtn.disabled = true;

    try {
        const response = await axios.post('http://localhost:8080/api/v1/reconcile-transactions', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });

        displayResults(response.data);
    } catch (err) {
        showError(err.response?.data?.message || err.message || 'An error occurred during reconciliation');
    } finally {
        loading.style.display = 'none';
        submitBtn.disabled = false;
    }
});

function showError(message) {
    error.textContent = message;
    error.style.display = 'block';
}

function displayResults(data) {
    // Display summary cards
    const summaryCards = document.getElementById('summaryCards');
    summaryCards.innerHTML = `
        <div class="summary-card">
            <h3>File 1 Records</h3>
            <div class="value">${data.totalRecordsInFile1}</div>
        </div>
        <div class="summary-card">
            <h3>File 2 Records</h3>
            <div class="value">${data.totalRecordsInFile2}</div>
        </div>
        <div class="summary-card">
            <h3>Matched Records</h3>
            <div class="value">${data.matchedRecords}</div>
        </div>
        <div class="summary-card">
            <h3>Unmatched in File 1</h3>
            <div class="value">${data.unmatchedRecordsInFile1}</div>
        </div>
        <div class="summary-card">
            <h3>Unmatched in File 2</h3>
            <div class="value">${data.unmatchedRecordsInFile2}</div>
        </div>
        <div class="summary-card">
            <h3>Match Percentage</h3>
            <div class="value">${data.matchPercentage.toFixed(2)}%</div>
        </div>
    `;

    // Display unmatched pairs
    const unmatchedPairs = document.getElementById('unmatchedPairs');
    unmatchedPairs.innerHTML = '';

    if (data.unmatchedTransactionPairs && data.unmatchedTransactionPairs.length > 0) {
        data.unmatchedTransactionPairs.forEach((pair, index) => {
            const pairDiv = document.createElement('div');
            pairDiv.className = 'transaction-pair';
            pairDiv.innerHTML = `
                ${renderTransaction(pair.transaction1, 'File 1 Transaction')}
                ${renderTransaction(pair.transaction2, 'File 2 Transaction')}
            `;
            unmatchedPairs.appendChild(pairDiv);
        });
    } else {
        unmatchedPairs.innerHTML = '<p style="text-align: center; color: #4caf50; font-weight: 600;">✅ All transactions matched!</p>';
    }

    results.classList.add('show');
}

function renderTransaction(transaction, header) {
    if (!transaction) {
        return `<div class="transaction empty">No matching transaction</div>`;
    }

    return `
        <div class="transaction">
            <div class="transaction-header">${header}</div>
            <div class="transaction-field">
                <span class="label">Profile Name:</span>
                <span class="value">${transaction.profileName || 'N/A'}</span>
            </div>
            <div class="transaction-field">
                <span class="label">Transaction ID:</span>
                <span class="value">${transaction.transactionID || 'N/A'}</span>
            </div>
            <div class="transaction-field">
                <span class="label">Date:</span>
                <span class="value">${transaction.transactionDate || 'N/A'}</span>
            </div>
            <div class="transaction-field">
                <span class="label">Amount:</span>
                <span class="value">${transaction.transactionAmount || 'N/A'}</span>
            </div>
            <div class="transaction-field">
                <span class="label">Type:</span>
                <span class="value">${transaction.transactionType || 'N/A'}</span>
            </div>
            <div class="transaction-field">
                <span class="label">Description:</span>
                <span class="value">${transaction.transactionDescription || 'N/A'}</span>
            </div>
            <div class="transaction-field">
                <span class="label">Narrative:</span>
                <span class="value">${transaction.transactionNarrative || 'N/A'}</span>
            </div>
            <div class="transaction-field">
                <span class="label">Wallet Reference:</span>
                <span class="value">${transaction.walletReference || 'N/A'}</span>
            </div>
        </div>
    `;
}