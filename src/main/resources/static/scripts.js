// ==========================================
// HÀM POP-UP DÙNG CHUNG (GLOBAL MODAL)
// ==========================================
function showConfirmModal(title, message, iconType, confirmCallback) {
    const modal = document.getElementById('globalConfirmModal');
    if (!modal) return;
    
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
const userDetailsModal = document.getElementById('userDetailsModal');
const btnCloseUserDetails = document.getElementById('btnCloseUserDetails');

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
                    
                    const row = `<tr onclick="window.location.href='/admin/account?searchAccNum=${acc.accountNumber}&ref=user&userId=${userId}'" style="cursor: pointer;">
                        <td>
                            <span style="color: #2563eb; font-weight: 600; text-decoration: underline;" 
                                    title="Đến trang quản lý tài khoản này">
                                ${acc.accountNumber}
                            </span>
                        </td>
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

// ==========================================
// XỬ LÝ POP-UP CHỈNH SỬA USER
// ==========================================
const editUserModal = document.getElementById('editUserModal');
const btnCloseEditForm = document.getElementById('btnCloseEditForm');

if (btnCloseEditForm) {
    btnCloseEditForm.addEventListener('click', () => editUserModal.classList.remove('active'));
}
if (editUserModal) {
    editUserModal.addEventListener('click', (e) => {
        if (e.target === editUserModal) editUserModal.classList.remove('active');
    });
}

function openEditModal(userId) {
    if (!editUserModal) return;

    document.getElementById('editId').value = userId;
    document.getElementById('editUserName').value = 'Đang tải...';
    document.getElementById('editFullName').value = '';
    document.getElementById('editPhoneNumber').value = '';
    document.getElementById('editEmail').value = '';
    document.getElementById('editAddress').value = '';
    document.getElementById('editGender').value = '';

    editUserModal.classList.add('active');

    fetch(`/admin/api/user/${userId}`)
        .then(response => {
            if (!response.ok) throw new Error('Không tìm thấy dữ liệu');
            return response.json();
        })
        .then(data => {
            document.getElementById('editUserName').value = data.username || '';
            document.getElementById('editFullName').value = data.fullName || '';
            document.getElementById('editPhoneNumber').value = data.phoneNumber || '';
            document.getElementById('editEmail').value = data.email || '';
            document.getElementById('editAddress').value = data.address || '';
            document.getElementById('editGender').value = data.gender || '';
        })
        .catch(error => {
            console.error('Lỗi API:', error);
            alert('Lỗi: Không thể tải thông tin khách hàng!');
            editUserModal.classList.remove('active');
        });
}

//Xán nhận gửi request thay đổi thông tin khách hàng
document.addEventListener("DOMContentLoaded", function() {
    const editUserModal = document.getElementById('editUserModal');
    if (editUserModal) {
        const editForm = editUserModal.querySelector('form');
        
        if (editForm) {
            editForm.addEventListener('submit', function(e) {
                e.preventDefault();
                
                showConfirmModal(
                    'Xác nhận gửi yêu cầu',
                    'Bạn có chắc chắn muốn tạo yêu cầu thay đổi thông tin khách hàng này để chờ cấp trên xét duyệt không?',
                    'info', 
                    function() {
                        editForm.submit();
                    }
                );
            });
        }
    }
});

// TÍNH NĂNG XEM YÊU CẦU & CUỘN VÔ HẠN
// ===================================
const requestsModal = document.getElementById('requestsModal');
const btnOpenRequests = document.getElementById('btnOpenRequests');
const btnCloseRequests = document.getElementById('btnCloseRequests');
const requestListContainer = document.getElementById('requestListContainer');
const loadingRequests = document.getElementById('loadingRequests');

let reqPage = 0;
let isReqLoading = false;
let hasMoreReqs = true;

// Đóng mở Modal
if (btnOpenRequests) {
    btnOpenRequests.addEventListener('click', () => {
        requestsModal.classList.add('active');
        reqPage = 0;
        hasMoreReqs = true;
        requestListContainer.innerHTML = ''; 
        loadRequests();
    });
}
if (btnCloseRequests) {
    btnCloseRequests.addEventListener('click', () => requestsModal.classList.remove('active'));
}
if (requestsModal) {
    requestsModal.addEventListener('click', (e) => {
        if (e.target === requestsModal) requestsModal.classList.remove('active');
    });
}

// Hàm Fetch dữ liệu từ API
function loadRequests() {
    if (isReqLoading || !hasMoreReqs) return;
    
    isReqLoading = true;
    loadingRequests.style.display = 'block';

    fetch(`/admin/api/requests?page=${reqPage}&size=5`)
        .then(response => response.json())
        .then(data => {
            const content = data.content;
            
            if (content.length === 0 || data.last) {
                hasMoreReqs = false;
            }

            content.forEach(req => {
                const date = new Date(req.requestDate);
                const formattedDate = `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth()+1).toString().padStart(2, '0')}/${date.getFullYear()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
                
                let statusColor = req.status === 'PENDING' ? '#f59e0b' : (req.status === 'APPROVED' ? '#10b981' : '#ef4444');
                let statusBg = req.status === 'PENDING' ? '#fef3c7' : (req.status === 'APPROVED' ? '#d1fae5' : '#fee2e2');

                const card = document.createElement('div');
                card.style.cssText = 'border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem; background: #ffffff;';
                card.innerHTML = `
                    <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem; align-items: center;">
                        <strong style="font-size: 1.05rem;">TK: <span style="color: #2563eb;">${req.username}</span></strong>
                        <span style="background: ${statusBg}; color: ${statusColor}; padding: 0.25rem 0.75rem; border-radius: 99px; font-size: 0.75rem; font-weight: 600;">${req.status}</span>
                    </div>
                    <p style="font-size: 0.85rem; color: #64748b; margin-bottom: 0.75rem;">Ngày gửi: ${formattedDate}</p>
                    <div style="font-size: 0.9rem; background: #f8fafc; padding: 0.75rem; border-radius: 6px; border: 1px dashed #cbd5e1;">
                        <div style="margin-bottom: 0.25rem;"><strong>Họ tên mới:</strong> ${req.newFullName || '<i>Không đổi</i>'}</div>
                        <div style="margin-bottom: 0.25rem;"><strong>SĐT mới:</strong> ${req.newPhoneNumber || '<i>Không đổi</i>'}</div>
                        <div style="margin-bottom: 0.25rem;"><strong>Email mới:</strong> ${req.newEmail || '<i>Không đổi</i>'}</div>
                        <div style="margin-bottom: 0.25rem;"><strong>Địa chỉ mới:</strong> ${req.newAddress || '<i>Không đổi</i>'}</div>
                        <div><strong>Giới tính mới:</strong> ${req.newGender || '<i>Không đổi</i>'}</div>
                    </div>
                `;
                requestListContainer.appendChild(card);
            });

            reqPage++;
            isReqLoading = false;
            loadingRequests.style.display = 'none';
        })
        .catch(err => {
            console.error('Lỗi API tải Request:', err);
            isReqLoading = false;
            loadingRequests.innerHTML = '<span style="color:#ef4444;">Lỗi tải dữ liệu!</span>';
        });
}

// Lắng nghe sự kiện Cuộn chuột (Scroll)
if (requestListContainer) {
    requestListContainer.addEventListener('scroll', () => {
        if (requestListContainer.scrollTop + requestListContainer.clientHeight >= requestListContainer.scrollHeight - 20) {
            loadRequests();
        }
    });
}

// ==========================================
// XỬ LÝ POP-UP CHI TIẾT TÀI KHOẢN
// ==========================================
const accountDetailsModal = document.getElementById('accountDetailsModal');
const btnCloseAccDetails = document.getElementById('btnCloseAccDetails');

if (btnCloseAccDetails) {
    btnCloseAccDetails.addEventListener('click', () => {
        if (accountDetailsModal) accountDetailsModal.classList.remove('active');
    });
}
if (accountDetailsModal) {
    accountDetailsModal.addEventListener('click', (e) => {
        if (e.target === accountDetailsModal) accountDetailsModal.classList.remove('active');
    });
}

function viewAccountDetails(accountNumber) {
    if (!accountDetailsModal) return;
    
    document.getElementById('accOwnerName').textContent = 'Đang tải...';
    document.getElementById('accOwnerAvatar').textContent = '?';
    document.getElementById('accOwnerId').textContent = 'ID: ...';
    document.getElementById('accDetailNumber').textContent = accountNumber;
    document.getElementById('accDetailDate').textContent = 'Đang tải...';
    document.getElementById('accOwnerPhone').textContent = 'Đang tải...';
    document.getElementById('accOwnerEmail').textContent = 'Đang tải...';

    accountDetailsModal.classList.add('active');

    fetch(`/admin/api/account/details/${accountNumber}`)
        .then(response => {
            if (!response.ok) throw new Error('Không tìm thấy dữ liệu');
            return response.json();
        })
        .then(data => {
            document.getElementById('accOwnerName').textContent = data.ownerName;
            document.getElementById('accOwnerAvatar').textContent = data.ownerName ? data.ownerName.charAt(0).toUpperCase() : 'U';
            document.getElementById('accOwnerId').textContent = 'ID Khách hàng: #KH' + data.ownerId.toString().padStart(3, '0');
            
            document.getElementById('accDetailNumber').textContent = data.accountNumber;
            
            if (data.dateOpen) {
                const date = new Date(data.dateOpen);
                document.getElementById('accDetailDate').textContent = `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth()+1).toString().padStart(2, '0')}/${date.getFullYear()}`;
            }
            
            document.getElementById('accOwnerPhone').textContent = data.ownerPhone || 'Chưa cập nhật';
            document.getElementById('accOwnerEmail').textContent = data.ownerEmail || 'Chưa cập nhật';
        })
        .catch(error => {
            console.error('Lỗi API:', error);
            document.getElementById('accOwnerName').textContent = 'Lỗi tải dữ liệu';
        });
}

// ==========================================
// TỰ ĐỘNG MỞ POP-UP KHI CHUYỂN TỪ TRANG KHÁCH HÀNG
// ==========================================
document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const ref = urlParams.get('ref');
    const searchAccNum = urlParams.get('searchAccNum');

    if (ref === 'user' && searchAccNum) {
        setTimeout(() => {
            if (typeof viewAccountDetails === 'function') {
                viewAccountDetails(searchAccNum.trim());
            }
        }, 150); 
    }
});

// ==========================================
// XỬ LÝ NÚT QUAY LẠI BÊN TRONG POP-UP CHI TIẾT TÀI KHOẢN
// ==========================================
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

// ==========================================
// TỰ ĐỘNG MỞ LẠI POP-UP CHI TIẾT KHÁCH HÀNG KHI QUAY VỀ TỪ TRANG TÀI KHOẢN
// ==========================================
document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const openUserId = urlParams.get('openUserId');

    if (openUserId) {
        setTimeout(() => {
            if (typeof viewUserDetails === 'function') {
                viewUserDetails(openUserId);
            }
        }, 150);
    }
});

// ==========================================
// XỬ LÝ NÚT QUAY LẠI Ở NGOÀI GIAO DIỆN TRANG TÀI KHOẢN
// ==========================================
document.addEventListener("DOMContentLoaded", function() {
    const mainBackBtn = document.getElementById('mainBackBtn');
    if (mainBackBtn) {
        mainBackBtn.addEventListener('click', function(e) {
            e.preventDefault();
            const urlParams = new URLSearchParams(window.location.search);
            const userId = urlParams.get('userId');

            if (userId) {
                window.location.href = `/admin?openUserId=${userId}`;
            } else {
                window.location.href = '/admin';
            }
        });
    }
});