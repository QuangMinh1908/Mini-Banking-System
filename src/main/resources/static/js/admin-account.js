// ==========================================
// ADMIN-ACCOUNT.JS - XỬ LÝ LOGIC TRANG QUẢN LÝ TÀI KHOẢN
// ==========================================

//Chi tiết tài khoản
const accountDetailsModal = document.getElementById('accountDetailsModal');
const btnCloseAccDetails = document.getElementById('btnCloseAccDetails');

function viewAccountDetails(accountNumber) {
    if (!accountDetailsModal) return;
    
    // 1. Reset giao diện về trạng thái Loading ban đầu
    document.getElementById('accOwnerName').textContent = 'Đang tải...';
    document.getElementById('accOwnerAvatar').textContent = '?';
    document.getElementById('accOwnerId').textContent = 'ID: ...';
    document.getElementById('accDetailNumber').textContent = accountNumber;
    document.getElementById('accDetailType').textContent = 'Đang tải...';
    document.getElementById('accDetailLimit').textContent = 'Đang tải...';
    document.getElementById('accDetailDate').textContent = 'Đang tải...';
    document.getElementById('accOwnerPhone').textContent = 'Đang tải...';
    document.getElementById('accOwnerEmail').textContent = 'Đang tải...';

    accountDetailsModal.classList.add('active');

    // 2. Fetch dữ liệu từ API
    fetch(`/admin/api/account/details/${accountNumber}`)
        .then(response => {
            if (!response.ok) throw new Error('Không tìm thấy dữ liệu');
            return response.json();
        })
        .then(data => {
            // 3. Lúc này biến "data" mới tồn tại để lấy dữ liệu đổ ra UI
            document.getElementById('accOwnerName').textContent = data.ownerName;
            document.getElementById('accOwnerAvatar').textContent = data.ownerName ? data.ownerName.charAt(0).toUpperCase() : 'U';
            document.getElementById('accOwnerId').textContent = 'ID Khách hàng: #KH' + data.ownerId.toString().padStart(3, '0');
            document.getElementById('accDetailNumber').textContent = data.accountNumber;
            
            // XỬ LÝ IN LOẠI TÀI KHOẢN VÀ HẠN MỨC
            document.getElementById('accDetailType').textContent = data.accountType === 'SAVING' ? 'Tài khoản Tiết kiệm' : 'Thanh toán Nội địa';
            
            let limitText = 'Cơ bản (50.000.000 VND)';
            if (data.transactionLimit === '500M') limitText = 'Nâng cao (500.000.000 VND)';
            if (data.transactionLimit === 'UNLIMITED') limitText = 'Không giới hạn';
            document.getElementById('accDetailLimit').textContent = limitText;
            
            if (data.dateOpen) {
                const date = new Date(data.dateOpen);
                document.getElementById('accDetailDate').textContent = `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth()+1).toString().padStart(2, '0')}/${date.getFullYear()}`;
            }
            
            document.getElementById('accOwnerPhone').textContent = data.ownerPhone || 'Chưa cập nhật';
            document.getElementById('accOwnerEmail').textContent = data.ownerEmail || 'Chưa cập nhật';
        })
        .catch(error => {
            document.getElementById('accOwnerName').textContent = 'Lỗi tải dữ liệu';
        });
}

if (btnCloseAccDetails) btnCloseAccDetails.addEventListener('click', () => accountDetailsModal.classList.remove('active'));
if (accountDetailsModal) accountDetailsModal.addEventListener('click', (e) => { if (e.target === accountDetailsModal) accountDetailsModal.classList.remove('active'); });

// Tự động mở Pop-up Account Detail nếu có param từ URL (khi click từ trang User qua)
document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const ref = urlParams.get('ref');
    const searchAccNum = urlParams.get('searchAccNum');

    if (ref === 'user' && searchAccNum) {
        setTimeout(() => { viewAccountDetails(searchAccNum.trim()); }, 150); 
    }
});

// Xử lý nút Back bên trong Pop-up Account Detail (Trở về User Detail)
document.addEventListener("DOMContentLoaded", function() {
    const modalBtnBack = document.getElementById('modalBtnBack');
    if (modalBtnBack) {
        const urlParams = new URLSearchParams(window.location.search);
        const ref = urlParams.get('ref');
        const userId = urlParams.get('userId');

        if (ref === 'user' && userId) {
            modalBtnBack.style.display = 'inline-flex';
            modalBtnBack.addEventListener('click', function(e) {
                e.preventDefault();
                window.location.href = `/admin?openUserId=${userId}`;
            });
        } else {
            modalBtnBack.style.display = 'none';
        }
    }
});

// Xử lý nút Back ở tiêu đề trang danh sách Account
document.addEventListener("DOMContentLoaded", function() {
    const mainBackBtn = document.getElementById('mainBackBtn');
    if (mainBackBtn) {
        mainBackBtn.addEventListener('click', function(e) {
            e.preventDefault();
            const userId = new URLSearchParams(window.location.search).get('userId');
            if (userId) {
                window.location.href = `/admin?openUserId=${userId}`;
            } else {
                window.location.href = '/admin';
            }
        });
    }
});