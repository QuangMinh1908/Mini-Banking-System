// ==========================================
// ADMIN-USER.JS - XỬ LÝ LOGIC TRANG QUẢN LÝ KHÁCH HÀNG
// ==========================================

// --- 1. XEM CHI TIẾT KHÁCH HÀNG ---
const userDetailsModal = document.getElementById('userDetailsModal');
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

            // Khởi tạo nút Mở tài khoản (Wizard)
            const btnCreateAccount = document.getElementById('btnCreateAccount');
            if (btnCreateAccount) {
                btnCreateAccount.onclick = function() {
                    const phone = data.phoneNumber || 'Không có SĐT';
                    openCreateAccountWizardWithUser(userId, data.fullName, phone);
                };
            }

            // Load danh sách tài khoản
            const tbody = document.getElementById('detailAccountsBody');
            tbody.innerHTML = '';
            if (data.accounts && data.accounts.length > 0) {
                data.accounts.forEach(acc => {
                    const date = new Date(acc.dateOpen);
                    const formattedDate = `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth()+1).toString().padStart(2, '0')}/${date.getFullYear()}`;
                    const row = `<tr onclick="window.location.href='/admin/account?searchAccNum=${acc.accountNumber}&ref=user&userId=${userId}'" style="cursor: pointer;">
                        <td><span style="color: #2563eb; font-weight: 600; text-decoration: underline;">${acc.accountNumber}</span></td>
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

// Đóng Modal User Details
const btnCloseUserDetails = document.getElementById('btnCloseUserDetails');
if (btnCloseUserDetails) btnCloseUserDetails.addEventListener('click', () => userDetailsModal.classList.remove('active'));
if (userDetailsModal) userDetailsModal.addEventListener('click', (e) => { if (e.target === userDetailsModal) userDetailsModal.classList.remove('active'); });


// --- 2. CHỈNH SỬA KHÁCH HÀNG ---
const editUserModal = document.getElementById('editUserModal');
let originalUserData = {};

function openEditModal(userId) {
    if (!editUserModal) return;
    document.getElementById('editId').value = userId;
    document.getElementById('editUserName').value = 'Đang tải...';
    editUserModal.classList.add('active');

    fetch(`/admin/api/user/${userId}`)
        .then(response => { if (!response.ok) throw new Error('Không tìm thấy dữ liệu'); return response.json(); })
        .then(data => {
            originalUserData = {
                fullName: data.fullName || '', phoneNumber: data.phoneNumber || '',
                email: data.email || '', address: data.address || '', gender: data.gender || ''
            };
            document.getElementById('editUserName').value = data.username || '';
            document.getElementById('editFullName').value = data.fullName || '';
            document.getElementById('editPhoneNumber').value = data.phoneNumber || '';
            document.getElementById('editEmail').value = data.email || '';
            document.getElementById('editAddress').value = data.address || '';
            document.getElementById('editGender').value = data.gender || '';
        })
        .catch(error => { alert('Lỗi: Không thể tải thông tin khách hàng!'); editUserModal.classList.remove('active'); });
}

// Xác nhận gửi Form Chỉnh sửa
document.addEventListener("DOMContentLoaded", function() {
    if (editUserModal) {
        const editForm = editUserModal.querySelector('form');
        if (editForm) {
            editForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const currentFullName = document.getElementById('editFullName');
                const currentPhone = document.getElementById('editPhoneNumber');
                const currentEmail = document.getElementById('editEmail');
                const currentAddress = document.getElementById('editAddress');
                const currentGender = document.getElementById('editGender');

                const hasChanges = currentFullName.value.trim() !== originalUserData.fullName || currentPhone.value.trim() !== originalUserData.phoneNumber ||
                                   currentEmail.value.trim() !== originalUserData.email || currentAddress.value.trim() !== originalUserData.address ||
                                   currentGender.value !== originalUserData.gender;

                if (!hasChanges) {
                    showConfirmModal('Không có thay đổi', 'Bạn chưa thay đổi bất kỳ thông tin nào.', 'info', function() {}); return; 
                }
                
                showConfirmModal('Xác nhận gửi yêu cầu', 'Gửi yêu cầu thay đổi thông tin?', 'info', function() {
                    if (currentFullName.value.trim() === originalUserData.fullName) currentFullName.value = '';
                    if (currentPhone.value.trim() === originalUserData.phoneNumber) currentPhone.value = '';
                    if (currentEmail.value.trim() === originalUserData.email) currentEmail.value = '';
                    if (currentAddress.value.trim() === originalUserData.address) currentAddress.value = '';
                    if (currentGender.value === originalUserData.gender) currentGender.value = '';
                    editForm.submit();
                });
            });
        }
    }
});

const btnCloseEditForm = document.getElementById('btnCloseEditForm');
if (btnCloseEditForm) btnCloseEditForm.addEventListener('click', () => editUserModal.classList.remove('active'));


// --- 3. XEM YÊU CẦU CHỈNH SỬA (CUỘN VÔ HẠN) ---
const requestsModal = document.getElementById('requestsModal');
const requestListContainer = document.getElementById('requestListContainer');
const loadingRequests = document.getElementById('loadingRequests');
let reqPage = 0, isReqLoading = false, hasMoreReqs = true;

function loadRequests() {
    if (isReqLoading || !hasMoreReqs) return;
    isReqLoading = true; loadingRequests.style.display = 'block';

    fetch(`/admin/api/requests?page=${reqPage}&size=5`)
        .then(response => response.json())
        .then(data => {
            if (data.content.length === 0 || data.last) hasMoreReqs = false;
            data.content.forEach(req => {
                const d = new Date(req.requestDate);
                const formattedDate = `${d.getDate().toString().padStart(2,'0')}/${(d.getMonth()+1).toString().padStart(2,'0')}/${d.getFullYear()} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`;
                let statusColor = req.status === 'PENDING' ? '#f59e0b' : (req.status === 'APPROVED' ? '#10b981' : '#ef4444');
                let statusBg = req.status === 'PENDING' ? '#fef3c7' : (req.status === 'APPROVED' ? '#d1fae5' : '#fee2e2');

                const card = document.createElement('div');
                card.style.cssText = 'border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem; background: #ffffff; margin-bottom: 1rem;';
                card.innerHTML = `
                    <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem; align-items: center;">
                        <strong style="font-size: 1.05rem;">TK: <span style="color: #2563eb;">${req.username}</span></strong>
                        <span style="background: ${statusBg}; color: ${statusColor}; padding: 0.25rem 0.75rem; border-radius: 99px; font-size: 0.75rem; font-weight: 600;">${req.status}</span>
                    </div>
                    <p style="font-size: 0.85rem; color: #64748b; margin-bottom: 0.75rem;">Ngày gửi: ${formattedDate}</p>
                    <div style="font-size: 0.9rem; background: #f8fafc; padding: 0.75rem; border-radius: 6px; border: 1px dashed #cbd5e1;">
                        <div><strong>Họ tên mới:</strong> ${req.newFullName || '<i>Không đổi</i>'}</div>
                        <div><strong>SĐT mới:</strong> ${req.newPhoneNumber || '<i>Không đổi</i>'}</div>
                        <div><strong>Email mới:</strong> ${req.newEmail || '<i>Không đổi</i>'}</div>
                        <div><strong>Địa chỉ mới:</strong> ${req.newAddress || '<i>Không đổi</i>'}</div>
                        <div><strong>Giới tính mới:</strong> ${req.newGender || '<i>Không đổi</i>'}</div>
                    </div>`;
                requestListContainer.appendChild(card);
            });
            reqPage++; isReqLoading = false; loadingRequests.style.display = 'none';
        })
        .catch(err => { loadingRequests.innerHTML = '<span style="color:#ef4444;">Lỗi tải dữ liệu!</span>'; });
}

document.getElementById('btnOpenRequests')?.addEventListener('click', () => {
    requestsModal.classList.add('active'); reqPage = 0; hasMoreReqs = true; requestListContainer.innerHTML = ''; loadRequests();
});
document.getElementById('btnCloseRequests')?.addEventListener('click', () => requestsModal.classList.remove('active'));
if (requestListContainer) requestListContainer.addEventListener('scroll', () => {
    if (requestListContainer.scrollTop + requestListContainer.clientHeight >= requestListContainer.scrollHeight - 20) loadRequests();
});


// --- 4. FORM WIZARD MỞ TÀI KHOẢN MỚI ---
const wizardModal = document.getElementById('createAccountWizardModal');
const confirmOverlay = document.getElementById('wizardConfirmOverlay');
const step1 = document.getElementById('wizard-step-1');
const step2 = document.getElementById('wizard-step-2');
const step3 = document.getElementById('wizard-step-3');
const ind1 = document.getElementById('step1-indicator');
const ind2 = document.getElementById('step2-indicator');
const ind3 = document.getElementById('step3-indicator');
const btnNextToStep2 = document.getElementById('btnNextToStep2');
const btnBackToStep1 = document.getElementById('btnBackToStep1');
const btnShowConfirm = document.getElementById('btnShowConfirm');

function resetWizard() {
    step1.style.display = 'block'; step2.style.display = 'none'; step3.style.display = 'none'; confirmOverlay.style.display = 'none';
    ind1.classList.add('active'); ind2.classList.remove('active'); ind3.classList.remove('active');
    document.getElementById('wizardSearchInput').value = '';
    document.getElementById('wizardSelectedUserBox').style.display = 'none';
    document.getElementById('wizardUserId').value = '';
    btnNextToStep2.setAttribute('disabled', 'true');
    btnNextToStep2.style.opacity = '0.5'; btnNextToStep2.style.cursor = 'not-allowed'; btnNextToStep2.style.pointerEvents = 'none';
}

function openCreateAccountWizard() { resetWizard(); wizardModal.classList.add('active'); }
function openCreateAccountWizardWithUser(userId, fullName, phone) {
    resetWizard(); selectUserForWizard(userId, fullName, phone); wizardModal.classList.add('active');
}

function closeWizard() { wizardModal.classList.remove('active'); confirmOverlay.style.display = 'none'; }
document.getElementById('btnCloseWizard')?.addEventListener('click', closeWizard);
document.getElementById('btnCancelWizardAll')?.addEventListener('click', closeWizard);

function selectUserForWizard(id, name, phone) {
    document.getElementById('wizardUserId').value = id;
    document.getElementById('wizardUserName').textContent = name;
    document.getElementById('wizardUserPhone').textContent = phone;
    document.getElementById('confirmOverlayName').textContent = name; 
    document.getElementById('wizardSelectedUserBox').style.display = 'block';
    
    btnNextToStep2.removeAttribute('disabled');
    btnNextToStep2.style.opacity = '1'; btnNextToStep2.style.cursor = 'pointer'; btnNextToStep2.style.pointerEvents = 'auto';
}

// Logic Tìm Kiếm (Dropdown)
const searchInput = document.getElementById('wizardSearchInput');
const searchResults = document.getElementById('wizardSearchResults');
if (searchInput && searchResults) {
    let searchTimeout = null;
    const executeSearch = () => {
        const keyword = searchInput.value.trim();
        if (!keyword) { searchResults.style.display = 'none'; return; }
        searchResults.innerHTML = '<div style="padding: 1rem; text-align: center; color: #64748b; font-size: 0.9rem;">Đang tìm kiếm...</div>';
        searchResults.style.display = 'block';

        fetch(`/admin/api/user/search?keyword=${encodeURIComponent(keyword)}`)
            .then(res => { if (!res.ok) throw new Error('Not Found'); return res.json(); })
            .then(userList => {
                searchResults.innerHTML = ''; 
                if (userList.length === 0) { searchResults.innerHTML = '<div style="padding: 1rem; text-align: center; color: #ef4444;">Không tìm thấy!</div>'; return; }
                userList.forEach(user => {
                    const phone = user.phoneNumber || 'Không có SĐT', name = user.fullName || 'Chưa cập nhật', username = user.username || 'N/A';
                    const item = document.createElement('div');
                    item.className = 'wizard-dropdown-item';
                    item.innerHTML = `<div class="item-title">${name}</div><div class="item-desc">ID: #KH${user.id.toString().padStart(3, '0')} • SĐT: ${phone} • TK: ${username}</div>`;
                    item.onclick = function() { selectUserForWizard(user.id, name, phone); searchResults.style.display = 'none'; };
                    searchResults.appendChild(item);
                });
            })
            .catch(() => { searchResults.innerHTML = '<div style="padding: 1rem; text-align: center; color: #ef4444;">Lỗi dữ liệu!</div>'; });
    };

    searchInput.addEventListener('input', () => { if (searchTimeout) clearTimeout(searchTimeout); if (!searchInput.value.trim()) { searchResults.style.display = 'none'; return; } searchTimeout = setTimeout(executeSearch, 1000); });
    searchInput.addEventListener('focus', () => { if (searchInput.value.trim() !== '' && searchResults.innerHTML.trim() !== '') searchResults.style.display = 'block'; });
    searchInput.addEventListener('keypress', (e) => { if (e.key === 'Enter') { e.preventDefault(); if (searchTimeout) clearTimeout(searchTimeout); executeSearch(); } });
    document.addEventListener('click', (e) => { const container = document.getElementById('wizardSearchContainer'); if (container && searchResults && !container.contains(e.target)) searchResults.style.display = 'none'; });
}

// Điều hướng Wizard
btnNextToStep2?.addEventListener('click', (e) => { e.preventDefault(); step1.style.display = 'none'; step2.style.display = 'block'; ind1.classList.remove('active'); ind2.classList.add('active'); });
btnBackToStep1?.addEventListener('click', (e) => { e.preventDefault(); step2.style.display = 'none'; step1.style.display = 'block'; ind2.classList.remove('active'); ind1.classList.add('active'); });
btnShowConfirm?.addEventListener('click', (e) => { e.preventDefault(); confirmOverlay.style.display = 'flex'; });
document.getElementById('btnCloseConfirmOverlay')?.addEventListener('click', (e) => { e.preventDefault(); confirmOverlay.style.display = 'none'; });

// Xác nhận API tạo tài khoản
const btnConfirmCreateAPI = document.getElementById('btnConfirmCreateAPI');
if (btnConfirmCreateAPI) {
    btnConfirmCreateAPI.onclick = (e) => {
        e.preventDefault();
        const userId = document.getElementById('wizardUserId').value;
        btnConfirmCreateAPI.innerHTML = 'Đang xử lý...'; btnConfirmCreateAPI.disabled = true;

        const accType = document.getElementById('wizardAccType').value;
        const limit = document.getElementById('wizardLimit').value;

        fetch(`/admin/api/user/${userId}/create-account`, { 
            method: 'POST', 
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                accountType: accType, 
                transactionLimit: limit 
            })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                confirmOverlay.style.display = 'none'; step2.style.display = 'none'; step3.style.display = 'block'; ind2.classList.remove('active'); ind3.classList.add('active');
                document.getElementById('successAccNum').textContent = data.accountNumber;
            } else alert('Lỗi: ' + data.error);
        })
        .finally(() => { btnConfirmCreateAPI.innerHTML = 'Đồng ý Tạo'; btnConfirmCreateAPI.disabled = false; });
    };
}

document.getElementById('btnFinishWizard')?.addEventListener('click', (e) => {
    e.preventDefault(); closeWizard();
    if (document.getElementById('userDetailsModal').classList.contains('active')) viewUserDetails(document.getElementById('wizardUserId').value);
    else window.location.reload();
});

// Mở lại chi tiết user khi back từ trang Account
document.addEventListener("DOMContentLoaded", function() {
    const openUserId = new URLSearchParams(window.location.search).get('openUserId');
    if (openUserId) setTimeout(() => { if (typeof viewUserDetails === 'function') viewUserDetails(openUserId); }, 150);
});