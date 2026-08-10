//Đồng bộ giờ local ngay khi tải trang
document.addEventListener("DOMContentLoaded", function() {
    formatLocalTime();
});

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
                            formatLocalTime();
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

// Hàm chuyển đổi thời gian gốc của Server sang Local Time của trình duyệt
function formatLocalTime() {
    document.querySelectorAll('.local-time:not(.formatted)').forEach(el => {
        let utcStr = el.getAttribute('data-utc');
        if (utcStr && !utcStr.endsWith('Z')) {
            utcStr += 'Z';
        }

        const date = new Date(utcStr);
        const dd = String(date.getDate()).padStart(2, '0');
        const MM = String(date.getMonth() + 1).padStart(2, '0');
        const yyyy = date.getFullYear();
        const HH = String(date.getHours()).padStart(2, '0');
        const mm = String(date.getMinutes()).padStart(2, '0');
        
        el.textContent = `${dd}/${MM}/${yyyy} ${HH}:${mm}`;
        el.classList.add('formatted');
    });
}

// HÀM MỞ CHI TIẾT GIAO DỊCH (BIÊN LAI)
function openTxDetail(id) {
    const modal = document.getElementById('txDetailModal');
    
    fetch(`/api/transactions/${id}`)
        .then(res => {
            if (!res.ok) throw new Error('Failed');
            return res.json();
        })
        .then(data => {
            const amtStr = new Intl.NumberFormat('vi-VN').format(data.amount) + ' VND';
            
            let title = '', sign = '', color = '', icon = '';
            if (data.type === 'DEPOSIT') {
                title = 'Nạp tiền'; sign = '+'; color = '#10b981';
                icon = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2"><path d="M12 5v14M5 12l7-7 7 7"/></svg>';
            } else if (data.type === 'WITHDRAW') {
                title = 'Rút tiền'; sign = '-'; color = '#ef4444';
                icon = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><path d="M12 19V5M5 12l7 7 7-7"/></svg>';
            } else {
                title = data.direction === 'CREDIT' ? 'Nhận tiền chuyển khoản' : 'Chuyển khoản đi';
                sign = data.direction === 'CREDIT' ? '+' : '-';
                color = data.direction === 'CREDIT' ? '#10b981' : '#ef4444';
                icon = data.direction === 'CREDIT' 
                    ? '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2"><polyline points="15 10 20 15 15 20"></polyline><path d="M4 4v7a4 4 0 0 0 4 4h12"></path></svg>'
                    : '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><polyline points="9 14 4 9 9 4"></polyline><path d="M20 20v-7a4 4 0 0 0-4-4H4"></path></svg>';
            }

            let utcStr = data.transactionDate;
            if (utcStr && !utcStr.endsWith('Z')) utcStr += 'Z';
            const d = new Date(utcStr);
            const timeStr = `${String(d.getDate()).padStart(2,'0')}/${String(d.getMonth()+1).padStart(2,'0')}/${d.getFullYear()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;

            document.getElementById('dtIcon').innerHTML = icon;
            document.getElementById('dtAmount').textContent = `${sign}${amtStr}`;
            document.getElementById('dtAmount').style.color = color;
            document.getElementById('dtId').textContent = data.transactionId;
            document.getElementById('dtTime').textContent = timeStr;
            document.getElementById('dtType').textContent = title;
            document.getElementById('dtDesc').textContent = data.description || 'Không có nội dung';
            
            if(data.type === 'TRANSFER') {
                 document.getElementById('dtAccLabel').textContent = data.direction === 'CREDIT' ? 'Từ tài khoản' : 'Đến tài khoản';
                 document.getElementById('dtAcc').textContent = data.relatedAccountNumber || 'N/A';
            } else {
                 document.getElementById('dtAccLabel').textContent = 'Tài khoản giao dịch';
                 document.getElementById('dtAcc').textContent = data.accountNumber;
            }

            modal.classList.add('active');
        });
}