// ==========================================
// HỆ THỐNG POP-UP DÙNG CHUNG (GLOBAL MODAL)
// ==========================================
function showConfirmModal(title, message, iconType, confirmCallback) {
    const modal = document.getElementById('globalConfirmModal');
    
    if (!modal) return; // Tránh lỗi nếu trang không có modal

    document.getElementById('globalModalTitle').textContent = title;
    document.getElementById('globalModalMessage').textContent = message;
    
    const iconContainer = document.getElementById('globalModalIcon');
    if (iconType === 'logout') {
        iconContainer.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>';
    } else if (iconType === 'delete') {
        iconContainer.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>';
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

// Đóng Pop-up khi bấm "Hủy bỏ"
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

// 2. Áp dụng cho nút Xóa Khách hàng (Nếu có)
function confirmDeleteUser(buttonElement) {
    showConfirmModal(
        'Cảnh báo Xóa Khách hàng',
        'Bạn có chắc chắn muốn xóa khách hàng này không? Mọi dữ liệu sẽ bị mất vĩnh viễn!',
        'delete',
        function() { 
            buttonElement.closest('form').submit(); 
        }
    );
}