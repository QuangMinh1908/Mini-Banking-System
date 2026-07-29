document.addEventListener("DOMContentLoaded", function() {
    // --- 1. ĐIỀU KHIỂN MỞ/ĐÓNG MODAL LỊCH SỬ GIAO DỊCH ---
    const txModal = document.getElementById('transactionHistoryModal');
    const btnOpenTx = document.getElementById('btnOpenTxHistory');
    const btnCloseTx = document.getElementById('btnCloseTxModal');

    if (btnOpenTx && txModal) {
        btnOpenTx.addEventListener('click', () => txModal.classList.add('active'));
    }
    if (btnCloseTx && txModal) {
        btnCloseTx.addEventListener('click', () => txModal.classList.remove('active'));
    }
    if (txModal) {
        txModal.addEventListener('click', (e) => {
            if (e.target === txModal) txModal.classList.remove('active');
        });
    }

    // --- 2. CUỘN VÔ TẬN (INFINITE SCROLL) TRONG MODAL ---
    const txList = document.getElementById('transactionList');
    const txModalScrollArea = document.getElementById('txModalScrollArea');
    const loadingIndicator = document.getElementById('loadingIndicator');
         
    let isFetching = false;
    let hasMoreTx = true;
    
    if (txList && txModalScrollArea) {
        txModalScrollArea.addEventListener('scroll', () => {
            if (isFetching || !hasMoreTx) return;
            
            if (txModalScrollArea.scrollTop + txModalScrollArea.clientHeight >= txModalScrollArea.scrollHeight - 50) {
                const lastItem = txList.lastElementChild;
                if (!lastItem) return;
                
                const lastDate = lastItem.getAttribute('data-date');
                const lastId = lastItem.getAttribute('data-id');
                isFetching = true;
                loadingIndicator.style.display = 'block';
                
                fetch(`/dashboard/transactions/more?lastDate=${encodeURIComponent(lastDate)}&lastId=${lastId}`)
                    .then(res => res.text())
                    .then(html => {
                        if (html.trim() === '') {
                            hasMoreTx = false; 
                        } else {
                            txList.insertAdjacentHTML('beforeend', html); 
                        }
                    })
                    .finally(() => {
                        isFetching = false;
                        loadingIndicator.style.display = 'none';
                    });
            }
        });
    }
});

// --- TÍNH NĂNG ẨN/HIỆN SỐ DƯ ---
const toggleButtons = document.querySelectorAll('.toggle-balance-btn');
toggleButtons.forEach(btn => {
    btn.addEventListener('click', function() {
        const card = this.closest('.bank-card');
        const balanceEl = card.querySelector('.balance-value');
        const eyeOpen = this.querySelector('.eye-open');
        const eyeClosed = this.querySelector('.eye-closed');
            
        const rawValue = balanceEl.getAttribute('data-raw-balance');
        const isHidden = balanceEl.getAttribute('data-hidden') === 'true';

        if (isHidden) {
            // Hiện lại số dư thật
            balanceEl.textContent = rawValue;
            balanceEl.setAttribute('data-hidden', 'false');
            eyeOpen.style.display = 'block';
            eyeClosed.style.display = 'none';
        } else {
            // Ẩn số dư thành dấu chấm
            balanceEl.textContent = '•••••••• VND';
            balanceEl.setAttribute('data-hidden', 'true');
            eyeOpen.style.display = 'none';
            eyeClosed.style.display = 'block';
        }
    });
});