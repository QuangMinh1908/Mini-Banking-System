// ==========================================
// HÀM POP-UP DÙNG CHUNG (GLOBAL MODAL)
// ==========================================
function showConfirmModal(title, message, iconType, confirmCallback) {
    const modal = document.getElementById('globalConfirmModal');
    if (!modal) return; // Tránh lỗi nếu trang không có modal
    
    document.getElementById('globalModalTitle').textContent = title;
    document.getElementById('globalModalMessage').textContent = message;
    
    const iconContainer = document.getElementById('globalModalIcon');
    
    // Icon Đăng xuất / Cảnh báo lỗi (Màu đỏ)
    if (iconType === 'logout' || iconType === 'delete') {
        iconContainer.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>';
    } 
    // Icon Thông tin / Xác nhận hành động (Màu xanh dương)
    else if (iconType === 'info' || iconType === 'edit') {
        iconContainer.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>';
    }

    const oldConfirmBtn = document.getElementById('btnGlobalConfirm');
    const newConfirmBtn = oldConfirmBtn.cloneNode(true);
    oldConfirmBtn.parentNode.replaceChild(newConfirmBtn, oldConfirmBtn);
    
    newConfirmBtn.addEventListener('click', function() {
        confirmCallback();
        modal.classList.remove('active');
    });
    
    modal.classList.add('active');
}

// Đóng Pop-up khi bấm "Hủy"
const btnGlobalCancel = document.getElementById('btnGlobalCancel');
if (btnGlobalCancel) {
    btnGlobalCancel.addEventListener('click', function() {
        document.getElementById('globalConfirmModal').classList.remove('active');
    });
}

// 1. Áp dụng cho nút Đăng xuất
const logoutLink = document.getElementById('logoutLink');
if (logoutLink) {
    logoutLink.addEventListener('click', function (e) {
        e.preventDefault();
        showConfirmModal(
            'Xác nhận đăng xuất',
            'Bạn có chắc chắn muốn đăng xuất khỏi phiên làm việc này không?',
            'logout',
            function() { window.location.href = '/logout'; }
        );
    });
}

// XỬ LÝ POP-UP CHI TIẾT USER (ADMIN)
// ==========================================
function viewUserDetails(userId) {
    document.getElementById('detailId').textContent = 'Đang tải...';
    document.getElementById('detailFullName').textContent = '';
    document.getElementById('detailUsername').textContent = '';
    document.getElementById('detailPhone').textContent = '';
    document.getElementById('detailEmail').textContent = '';
    document.getElementById('detailGender').textContent = '';
    document.getElementById('detailCreatedAt').textContent = '';
    document.getElementById('detailAddress').textContent = '';
    document.getElementById('detailAccountsBody').innerHTML = '<tr><td colspan="2" style="text-align:center;">Đang tải dữ liệu...</td></tr>';
    
    userDetailsModal.classList.add('active');

    fetch(`/admin/api/user/${userId}`)
        .then(response => {
            if (!response.ok) throw new Error('Không tìm thấy dữ liệu');
            return response.json();
        })
        .then(data => {
            document.getElementById('detailId').textContent = '#KH' + data.id.toString().padStart(3, '0');
            document.getElementById('detailFullName').textContent = data.fullName || 'Chưa cập nhật';
            document.getElementById('detailUsername').textContent = data.username || 'Chưa cập nhật';
            document.getElementById('detailPhone').textContent = data.phoneNumber || 'Chưa cập nhật';
            document.getElementById('detailEmail').textContent = data.email || 'Chưa cập nhật';
            document.getElementById('detailGender').textContent = data.gender || 'Chưa cập nhật';
            
            if (data.createdAt) {
                const createdDate = new Date(data.createdAt);
                const formattedCreated = `${createdDate.getDate().toString().padStart(2, '0')}/${(createdDate.getMonth()+1).toString().padStart(2, '0')}/${createdDate.getFullYear()} ${createdDate.getHours().toString().padStart(2, '0')}:${createdDate.getMinutes().toString().padStart(2, '0')}`;
                document.getElementById('detailCreatedAt').textContent = formattedCreated;
            } else {
                document.getElementById('detailCreatedAt').textContent = 'Chưa cập nhật';
            }

            document.getElementById('detailAddress').textContent = data.address || 'Chưa cập nhật';

            const tbody = document.getElementById('detailAccountsBody');
            tbody.innerHTML = '';
            
            if (data.accounts && data.accounts.length > 0) {
                data.accounts.forEach(acc => {
                    const date = new Date(acc.dateOpen);
                    const formattedDate = `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth()+1).toString().padStart(2, '0')}/${date.getFullYear()}`;
                    
                    const row = `<tr>
                        <td><strong style="color: #2563eb;">${acc.accountNumber}</strong></td>
                        <td>${formattedDate}</td>
                    </tr>`;
                    tbody.innerHTML += row;
                });
            } else {
                tbody.innerHTML = '<tr><td colspan="2" style="text-align:center; color: #64748b;">Chưa có tài khoản nào</td></tr>';
            }
        })
        .catch(error => {
            console.error('Lỗi API:', error);
            document.getElementById('detailAccountsBody').innerHTML = '<tr><td colspan="2" style="text-align:center; color: #ef4444;">Lỗi tải dữ liệu!</td></tr>';
        });
}
//Đóng pop-up chi tiết user
const userDetailsModal = document.getElementById('userDetailsModal');
const btnCloseUserDetails = document.getElementById('btnCloseUserDetails');

// 1. Gắn sự kiện đóng bằng nút X (Chỉ gán 1 lần khi trang được tải)
if (btnCloseUserDetails) {
    btnCloseUserDetails.addEventListener('click', function() {
        if (userDetailsModal) {
            userDetailsModal.classList.remove('active');
        }
    });
}

// 2. Gắn sự kiện đóng khi bấm ra vùng nền mờ bên ngoài
if (userDetailsModal) {
    userDetailsModal.addEventListener('click', function(e) {
        if (e.target === userDetailsModal) {
            userDetailsModal.classList.remove('active');
        }
    });
}

// FORM CHỈNH SỬA (YÊU CẦU XÉT DUYỆT)
// ==========================================
document.addEventListener("DOMContentLoaded", function() {
    const editUserModal = document.getElementById('editUserModal');
    if (editUserModal) {
        const editForm = editUserModal.querySelector('form');
        
        if (editForm) {
            editForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                showConfirmModal(
                    'Xác nhận gửi yêu cầu',
                    'Bạn có chắc chắn muốn tạo yêu cầu thay đổi thông tin khách hàng này để chờ xét duyệt không?',
                    'info',
                    function() {
                        editForm.submit();
                    }
                );
            });
        }
    }
});