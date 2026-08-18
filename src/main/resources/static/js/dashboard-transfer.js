document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('rateLimited') === 'true') {
        alert('Bạn vừa thao tác quá nhanh. Vui lòng kiểm tra lại lịch sử giao dịch và thử lại sau ít giây.');
        urlParams.delete('rateLimited');
        const cleanUrl = window.location.pathname + (urlParams.toString() ? '?' + urlParams.toString() : '');
        window.history.replaceState({}, document.title, cleanUrl);
    }

    // Nút gạt Checkbox
    const typeCheckbox = document.getElementById('transferTypeCheckbox');
    
    // Các thành phần DOM
    const internalGroup = document.getElementById('internalTargetGroup');
    const externalGroup = document.getElementById('externalTargetGroup');
    const fromSelect = document.getElementById('fromAccountNumber');
    const internalSelect = document.getElementById('toAccountNumberInternal');
    const externalInput = document.getElementById('toAccountNumberExternal');
    const realTargetInput = document.getElementById('realToAccountNumber');

    const cardPreview = document.getElementById('cardPreviewContainer');
    const targetCardContainer = document.getElementById('targetCardContainer');
    const receiverInfoBox = document.getElementById('receiverInfoBox');
    
    let transferMode = 'internal'; 
    let typingTimer;
    let confirmedReceiverName = null;
    let lastCheckedAccount = null;

    // Phục hồi lại dữ liệu cũ nếu Server trả về do lỗi Validation
    if (realTargetInput.value) {
        let foundInInternal = Array.from(internalSelect.options).some(opt => opt.value === realTargetInput.value);
        if (foundInInternal) {
            typeCheckbox.checked = true;
            internalSelect.value = realTargetInput.value;
            transferMode = 'internal';
        } else {
            typeCheckbox.checked = false;
            externalInput.value = realTargetInput.value;
            transferMode = 'external';
            lookupReceiver();
        }
    } else {
        typeCheckbox.checked = true;
        transferMode = 'internal';
    }
    toggleViewMode();

    // 1. CHUYỂN ĐỔI CHẾ ĐỘ THÔNG QUA CHECKBOX
    if (typeCheckbox) {
        typeCheckbox.addEventListener('change', (e) => {
            transferMode = e.target.checked ? 'internal' : 'external';
            toggleViewMode();
        });
    }

    function toggleViewMode() {
        if (transferMode === 'internal') {
            internalGroup.style.display = 'block';
            externalGroup.style.display = 'none';
            targetCardContainer.style.display = 'block';
            receiverInfoBox.style.display = 'none';
            renderTargetCard();
        } else {
            internalGroup.style.display = 'none';
            externalGroup.style.display = 'block';
            targetCardContainer.style.display = 'none';
            
            if (externalInput.value.length >= 5) {
                receiverInfoBox.style.display = 'block';
                if (externalInput.value !== lastCheckedAccount) lookupReceiver();
            } else {
                receiverInfoBox.style.display = 'none';
            }
        }
    }

    // 2. LOGIC RENDER CARD (TÀI KHOẢN NGUỒN)
    function renderSourceCard() {
        const selectedOpt = fromSelect.options[fromSelect.selectedIndex];
        if (!selectedOpt || !selectedOpt.value) {
            cardPreview.innerHTML = `<div style="background: #e2e8f0; height: 210px; border-radius: 1.25rem; display: flex; align-items: center; justify-content: center; color: #94a3b8; border: 2px dashed #cbd5e1;">Vui lòng chọn tài khoản nguồn...</div>`;
            return;
        }

        const accNum = escapeHTML(selectedOpt.value);
        const balance = escapeHTML(selectedOpt.getAttribute('data-balance'));
        const limit = escapeHTML(selectedOpt.getAttribute('data-limit'));
        const date = escapeHTML(selectedOpt.getAttribute('data-date'));

        cardPreview.innerHTML = `
            <div class="bank-card payment" style="margin: 0 auto; width: 100%; max-width: 420px; min-height: 220px;">
                <div class="card-top-row">
                    <div>
                        <span class="card-type-label">Thẻ Thanh Toán</span>
                        <div class="card-chip"></div>
                    </div>
                    <span class="badge card-badge-glass">Hạn mức: ${limit}</span>
                </div>
                <div class="card-balance-section" style="margin-top: 1.5rem;">
                    <div class="card-balance-header">
                        <p class="card-balance-label">Số dư khả dụng</p>
                    </div>
                    <h2 class="card-balance-amount">${balance}</h2>
                </div>
                <div class="card-footer-row" style="margin-top: auto;">
                    <div>
                        <span class="card-footer-sub">SỐ TÀI KHOẢN (STK)</span>
                        <strong class="card-acc-number">${accNum}</strong>
                    </div>
                    <div class="card-date-box">
                        <span class="card-footer-sub">MỞ NGÀY</span>
                        <span class="card-date-val">${date}</span>
                    </div>
                </div>
            </div>
        `;
    }

    // 3. LOGIC RENDER CARD (TÀI KHOẢN ĐÍCH KHI CHUYỂN NỘI BỘ)
    function renderTargetCard() {
        if (transferMode !== 'internal') return;
        
        const selectedOpt = internalSelect.options[internalSelect.selectedIndex];
        if (!selectedOpt || !selectedOpt.value) {
            targetCardContainer.innerHTML = `<div style="background: #e2e8f0; height: 210px; border-radius: 1.25rem; display: flex; align-items: center; justify-content: center; color: #94a3b8; border: 2px dashed #cbd5e1;">Vui lòng chọn tài khoản nhận...</div>`;
            return;
        }

        const accNum = escapeHTML(selectedOpt.value);
        const type = selectedOpt.getAttribute('data-type');
        const balance = escapeHTML(selectedOpt.getAttribute('data-balance'));
        const date = escapeHTML(selectedOpt.getAttribute('data-date'));

        let badgeHtml = '';
        let cardClass = '';
        let typeLabel = '';

        if (type === 'PAYMENT') {
            cardClass = 'payment';
            typeLabel = 'Thẻ Thanh Toán';
            const limit = escapeHTML(selectedOpt.getAttribute('data-limit'));
            badgeHtml = `<span class="badge card-badge-glass">Hạn mức: ${limit}</span>`;
        } else {
            cardClass = 'saving';
            typeLabel = 'Sổ Tiết Kiệm';
            const interest = escapeHTML(selectedOpt.getAttribute('data-interest'));
            const term = escapeHTML(selectedOpt.getAttribute('data-term'));
            badgeHtml = `<span class="badge card-badge-glass">Lãi: ${interest}%/năm (${term} tháng)</span>`;
        }

        targetCardContainer.innerHTML = `
            <div class="bank-card ${cardClass}" style="margin: 0 auto; width: 100%; max-width: 420px; min-height: 220px; filter: brightness(0.95);">
                <div class="card-top-row">
                    <div>
                        <span class="card-type-label">${typeLabel}</span>
                        <div class="card-chip"></div>
                    </div>
                    ${badgeHtml}
                </div>
                <div class="card-balance-section" style="margin-top: 1.5rem;">
                    <div class="card-balance-header">
                        <p class="card-balance-label">Số dư hiện tại</p>
                    </div>
                    <h2 class="card-balance-amount" style="font-size: 1.4rem;">${balance}</h2>
                </div>
                <div class="card-footer-row" style="margin-top: auto;">
                    <div>
                        <span class="card-footer-sub">SỐ TÀI KHOẢN (STK) NHẬN</span>
                        <strong class="card-acc-number">${accNum}</strong>
                    </div>
                </div>
            </div>
        `;
    }

    // Gắn sự kiện thay đổi Tài khoản Nguồn
    if (fromSelect) {
        fromSelect.addEventListener('change', function() {
            renderSourceCard();
            
            // Xóa tài khoản đích nếu bị trùng
            if (transferMode === 'internal' && internalSelect.value === fromSelect.value) {
                internalSelect.value = '';
                renderTargetCard();
            }
            if (transferMode === 'external' && externalInput.value.trim().length >= 5) {
                lookupReceiver(); 
            }
        });
        renderSourceCard(); 
    }

    // Gắn sự kiện thay đổi Tài khoản Nhận (Nội bộ)
    if (internalSelect) {
        internalSelect.addEventListener('change', function() {
            if (internalSelect.value === fromSelect.value && internalSelect.value !== '') {
                showConfirmModal('Lỗi chuyển khoản', 'Không thể chọn tài khoản đích trùng với tài khoản nguồn!', 'delete', () => {});
                internalSelect.value = '';
            }
            renderTargetCard();
        });
    }

    // 4. LOGIC TRA CỨU NGƯỜI NHẬN & BẮT LỖI TRÙNG STK (NGOẠI BỘ)
    function lookupReceiver() {
        if (transferMode !== 'external') return;

        const targetAccNum = externalInput.value.trim();
        const sourceAccNum = fromSelect.value; 

        if (targetAccNum.length < 5) {
            receiverInfoBox.style.display = 'none';
            confirmedReceiverName = null;
            lastCheckedAccount = null;
            return;
        }

        if (targetAccNum === sourceAccNum) {
            confirmedReceiverName = null;
            lastCheckedAccount = targetAccNum;
            receiverInfoBox.style.display = 'block';
            receiverInfoBox.style.background = '#fef2f2';
            receiverInfoBox.style.border = '1px solid #fecaca';
            receiverInfoBox.innerHTML = `
                <div style="display: flex; align-items: flex-start; gap: 0.75rem;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#b91c1c" stroke-width="2" style="flex-shrink: 0; margin-top: 2px;"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>
                    <div><strong style="display: block; margin-bottom: 0.25rem; font-size: 1rem; color: #991b1b;">Lỗi chuyển khoản</strong><span style="font-size: 0.9rem; color: #b91c1c;">Không thể chuyển tiền đến chính tài khoản nguồn!</span></div>
                </div>`;
            return;
        }

        if (targetAccNum === lastCheckedAccount && confirmedReceiverName !== null) return;
        
        confirmedReceiverName = null;
        lastCheckedAccount = null;
        receiverInfoBox.style.display = 'block';
        receiverInfoBox.style.background = '#f8fafc';
        receiverInfoBox.style.border = '1px solid #e2e8f0';
        receiverInfoBox.innerHTML = '<span style="color: #64748b; animation: pulse 1.5s infinite;">Đang tra cứu thông tin...</span>';

        fetch(`/api/transfer/lookup-receiver?accountNumber=${targetAccNum}`)
            .then(res => { if (!res.ok) throw { status: res.status }; return res.json(); })
            .then(data => {
                confirmedReceiverName = data.fullName;
                lastCheckedAccount = targetAccNum;
                receiverInfoBox.style.background = '#f0fdf4';
                receiverInfoBox.style.border = '1px solid #bbf7d0';
                receiverInfoBox.innerHTML = `
                    <div style="display: flex; align-items: flex-start; gap: 0.75rem;">
                        <div style="background: #dcfce7; padding: 0.5rem; border-radius: 50%; color: #16a34a; flex-shrink: 0;"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg></div>
                        <div style="flex: 1;"><div style="font-size: 0.85rem; color: #166534; opacity: 0.85; text-transform: uppercase; font-weight: 600; margin-bottom: 0.25rem;">Người thụ hưởng</div><div style="font-size: 1.15rem; font-weight: 700; color: #14532d; margin-bottom: 0.25rem;">${escapeHTML(data.fullName)}</div><div style="font-size: 0.95rem; font-family: monospace; font-weight: 600; color: #166534;">STK: ${escapeHTML(targetAccNum)}</div></div>
                    </div>`;
            })
            .catch(err => {
                if (err.status === 429) {
                    receiverInfoBox.style.background = '#fffbeb';
                    receiverInfoBox.style.border = '1px solid #fde68a';
                    receiverInfoBox.innerHTML = `
                        <div style="display: flex; align-items: flex-start; gap: 0.75rem;">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#d97706" stroke-width="2" style="flex-shrink: 0; margin-top: 2px;"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                            <div><strong style="display: block; margin-bottom: 0.25rem; font-size: 1rem; color: #b45309;">Thao tác quá nhanh</strong><span style="font-size: 0.9rem; color: #d97706;">Vui lòng đợi một lát!</span></div>
                        </div>`;
                } else {
                    receiverInfoBox.style.background = '#fef2f2';
                    receiverInfoBox.style.border = '1px solid #fecaca';
                    receiverInfoBox.innerHTML = `
                        <div style="display: flex; align-items: flex-start; gap: 0.75rem;">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#b91c1c" stroke-width="2" style="flex-shrink: 0; margin-top: 2px;"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>
                            <div><strong style="display: block; margin-bottom: 0.25rem; font-size: 1rem; color: #991b1b;">Không tìm thấy</strong><span style="font-size: 0.9rem; color: #b91c1c;">Tài khoản không tồn tại.</span></div>
                        </div>`;
                }
            });
    }

    if (externalInput) {
        externalInput.addEventListener('input', function() {
            clearTimeout(typingTimer);
            typingTimer = setTimeout(lookupReceiver, 800); 
        });

        externalInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault(); 
                clearTimeout(typingTimer);
                if (externalInput.value.trim() !== lastCheckedAccount) lookupReceiver();
            }
        });
    }

    // --- 4. XÁC NHẬN SUBMIT FORM ---
    const transferForm = document.getElementById('transferForm');
    const submitBtn = document.getElementById('btnSubmitTransfer');

    if (transferForm && submitBtn) {
        transferForm.addEventListener('submit', function(e) {
            if (submitBtn.disabled) { e.preventDefault(); return; }
            e.preventDefault();

            // Lấy giá trị đích tùy theo chế độ
            const toAcc = transferMode === 'internal' ? internalSelect.value : externalInput.value.trim();
            const fromAcc = fromSelect.value;
            const amountVal = document.getElementById('amount').value;
            
            if (!toAcc) {
                showConfirmModal('Thông báo', 'Vui lòng nhập hoặc chọn tài khoản đích!', 'delete', () => {});
                return;
            }

            if (toAcc === fromAcc) {
                showConfirmModal('Lỗi', 'Không thể chuyển tiền đến chính tài khoản nguồn!', 'delete', () => {});
                return;
            }

            // Gán dữ liệu vào thẻ input ẩn để gửi về Spring Boot
            realTargetInput.value = toAcc;

            if (!transferForm.checkValidity()) {
                transferForm.reportValidity();
                return;
            }

            const amountFormatted = Number(amountVal).toLocaleString('vi-VN');
            const receiverLine = transferMode === 'internal' 
                ? `Tài khoản của bạn (STK: <b>${escapeHTML(toAcc)}</b>)`
                : ((confirmedReceiverName && toAcc === lastCheckedAccount) 
                    ? `<b>${escapeHTML(confirmedReceiverName)}</b> (STK: ${escapeHTML(toAcc)})` 
                    : `STK: <b>${escapeHTML(toAcc)}</b>`);

            showConfirmModal(
                'Xác nhận chuyển khoản',
                `Bạn sắp chuyển <b>${escapeHTML(amountFormatted)} VND</b> đến ${receiverLine}. Vui lòng kiểm tra kỹ trước khi xác nhận.`,
                'info',
                function() {
                    submitBtn.disabled = true;
                    submitBtn.textContent = 'Đang xử lý...';
                    transferForm.submit();
                }
            );
        });
    }
});