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