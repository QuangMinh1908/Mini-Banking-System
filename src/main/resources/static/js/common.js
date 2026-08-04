// ==========================================
// COMMON.JS - HÀM DÙNG CHUNG CHO TẤT CẢ CÁC TRANG
// ==========================================

// 1. Hàm hiển thị Pop-up xác nhận chung
function showConfirmModal(title, message, iconType, confirmCallback) {
    const modal = document.getElementById('globalConfirmModal');
    if (!modal) return;
    
    document.getElementById('globalModalTitle').textContent = title;
    document.getElementById('globalModalMessage').innerHTML = message;
    
    const iconContainer = document.getElementById('globalModalIcon');
    if (iconType === 'logout' || iconType === 'delete') {
        iconContainer.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>';
    } else if (iconType === 'info' || iconType === 'edit') {
        iconContainer.innerHTML = '<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>';
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

// 2. Đóng Pop-up khi bấm "Hủy"
const btnGlobalCancel = document.getElementById('btnGlobalCancel');
if (btnGlobalCancel) {
    btnGlobalCancel.addEventListener('click', function() {
        document.getElementById('globalConfirmModal').classList.remove('active');
    });
}

const logoutLink = document.getElementById('logoutLink');
if (logoutLink) {
    logoutLink.addEventListener('click', function (e) {
        e.preventDefault();
        showConfirmModal(
            'Xác nhận đăng xuất',
            'Bạn có chắc chắn muốn đăng xuất khỏi phiên làm việc này không?',
            'logout',
            function() { 
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '/logout';                
                const csrfMeta = document.querySelector('meta[name="_csrf"]');
                if (csrfMeta) {
                    const csrfInput = document.createElement('input');
                    csrfInput.type = 'hidden';
                    csrfInput.name = '_csrf';
                    csrfInput.value = csrfMeta.getAttribute('content');
                    form.appendChild(csrfInput);
                }
                document.body.appendChild(form);
                form.submit(); 
            }
        );
    });
}

// 4. NHẢY TRANG
document.addEventListener("DOMContentLoaded", function() {
    const jumpInputs = document.querySelectorAll('.jump-page-input');
    
    jumpInputs.forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const page = parseInt(this.value);
                const maxPage = parseInt(this.getAttribute('max'));
                
                if (page >= 1 && page <= maxPage) {
                    const urlParams = new URLSearchParams(window.location.search);
                    urlParams.set('page', page - 1); 
                    window.location.search = urlParams.toString();
                } else {
                    showConfirmModal(
                        'Lỗi nhập liệu',
                        `Vui lòng nhập trang từ <b>1</b> đến <b>${maxPage}</b>.`,
                        'delete', 
                        function() {}
                    );
                    this.value = '';
                }
            }
        });
    });
});

// 5. TAG TÌM KIẾM
function renderSearchTags(activeTags) {
    const tagsContainer = document.getElementById('activeSearchTags');
    if (!tagsContainer || activeTags.length === 0) return;
    tagsContainer.innerHTML = '';
    const maxTags = 3;
    
    activeTags.forEach((tag, index) => {
        if (index < maxTags) {
            const tagElement = document.createElement('span');
            tagElement.className = 'search-tag';
            
            const tagText = document.createElement('span');
            tagText.textContent = tag.text;
            tagElement.appendChild(tagText);
            
            const closeBtn = document.createElement('button');
            closeBtn.type = 'button';
            closeBtn.className = 'tag-close-btn';
            closeBtn.innerHTML = '&times;';
            
            closeBtn.onclick = function(e) {
                e.preventDefault();
                document.querySelector(`input[name="${tag.inputName}"]`).value = '';
                document.querySelector('form.filter-bar').submit();
            };
            
            tagElement.appendChild(closeBtn);
            tagsContainer.appendChild(tagElement);
        }
    });
    
    if (activeTags.length > maxTags) {
        const dotsElement = document.createElement('span');
        dotsElement.className = 'search-tag-ellipsis';
        dotsElement.textContent = '...';
        tagsContainer.appendChild(dotsElement);
    }
}

// --- 6. FORM WIZARD MỞ TÀI KHOẢN MỚI ---
function toggleAccountFields() {
    const accType = document.getElementById('wizardAccType').value;
    const paymentGrp = document.getElementById('paymentConfigGroup');
    const savingGrp = document.getElementById('savingConfigGroup');
    
    if (accType === 'SAVING') {
        paymentGrp.style.display = 'none';
        savingGrp.style.display = 'grid';
    } else {
        paymentGrp.style.display = 'block';
        savingGrp.style.display = 'none';
    }
}

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
            .then(response => {
                searchResults.innerHTML = ''; 
                if (!response.data || response.data.length === 0) { 
                    searchResults.innerHTML = '<div style="padding: 1rem; text-align: center; color: #ef4444;">Không tìm thấy!</div>'; 
                    return; 
                }

                response.data.forEach(user => {
                    const phone = user.phoneNumber || 'Không có SĐT', name = user.fullName || 'Chưa cập nhật', username = user.username || 'N/A';
                    const item = document.createElement('div');
                    item.className = 'wizard-dropdown-item';
                    item.innerHTML = `<div class="item-title">${name}</div><div class="item-desc">ID: #KH${user.id.toString().padStart(3, '0')} - SĐT: ${phone} - TK: ${username}</div>`;
                    item.onclick = function() { selectUserForWizard(user.id, name, phone); searchResults.style.display = 'none'; };
                    searchResults.appendChild(item);
                });
            })
            .catch(() => { searchResults.innerHTML = '<div style="padding: 1rem; text-align: center; color: #ef4444;">Không tìm thấy kết quả phù hợp, vui lòng thử lại!</div>'; });
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
        const userId = document.getElementById('wizardUserId').value;
        btnConfirmCreateAPI.innerHTML = 'Đang xử lý...'; btnConfirmCreateAPI.disabled = true;
        
        // Cập nhật lấy thêm term và interest
        const accType = document.getElementById('wizardAccType').value;
        const limit = document.getElementById('wizardLimit').value;
        const term = document.getElementById('wizardTerm').value;
        const interest = document.getElementById('wizardInterest').value;
        
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        
        fetch(`/admin/api/user/${userId}/create-account`, { 
              method: 'POST', 
              headers: { 
                  'Content-Type': 'application/json',
                 [csrfHeader]: csrfToken
             },
             body: JSON.stringify({ 
                  accountType: accType, 
                  transactionLimit: limit,
                  termMonths: term,
                  interestRate: interest
              })
        })
        .then(async res => {
            if (res.status === 429) {
                alert("Bấm quá nhanh! Vui lòng thao tác chậm lại.");
                throw new Error("Spam Click Blocked");
            }
            return res.json();
        })
        .then(response => {
            if (response.status === 'SUCCESS') {
                confirmOverlay.style.display = 'none'; 
                step2.style.display = 'none'; 
                step3.style.display = 'block'; 
                ind2.classList.remove('active'); 
                ind3.classList.add('active');
                
                document.getElementById('successAccNum').textContent = response.data;
            } else {
                alert('Lỗi: ' + response.message);
            }
        })
        .finally(() => { btnConfirmCreateAPI.innerHTML = 'Tạo'; btnConfirmCreateAPI.disabled = false; });
    };
}

document.getElementById('btnFinishWizard')?.addEventListener('click', (e) => {
    e.preventDefault();
    closeWizard();
    const userDetailsModal = document.getElementById('userDetailsModal');
    if (userDetailsModal && userDetailsModal.classList.contains('active') && typeof viewUserDetails === 'function') {
        viewUserDetails(document.getElementById('wizardUserId').value);
    } else {
        window.location.reload();
    }
});

// Mở lại chi tiết user khi back từ trang Account
document.addEventListener("DOMContentLoaded", function() {
    const openUserId = new URLSearchParams(window.location.search).get('openUserId');
    if (openUserId) setTimeout(() => { if (typeof viewUserDetails === 'function') viewUserDetails(openUserId); }, 150);
});