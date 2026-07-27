document.addEventListener("DOMContentLoaded", function() {
    // --- XỬ LÝ KÉO XUỐNG VÔ TẬN (INFINITE SCROLL) ---
    const txList = document.getElementById('transactionList');
    const scrollArea = document.querySelector('.content-scroll-area');
    const loadingIndicator = document.getElementById('loadingIndicator');
    
    let isFetching = false;
    let hasMoreTx = true;

    if (txList && scrollArea) {
        scrollArea.addEventListener('scroll', () => {
            if (isFetching || !hasMoreTx) return;

            // Kiểm tra xem người dùng đã cuộn gần xuống đáy chưa (cách đáy 50px)
            if (scrollArea.scrollTop + scrollArea.clientHeight >= scrollArea.scrollHeight - 50) {
                const lastItem = txList.lastElementChild;
                if (!lastItem) return;

                // Lấy con trỏ Key-set
                const lastDate = lastItem.getAttribute('data-date');
                const lastId = lastItem.getAttribute('data-id');

                isFetching = true;
                loadingIndicator.style.display = 'block';

                // Gửi API Fetch để lấy Fragment html
                fetch(`/dashboard/transactions/more?lastDate=${encodeURIComponent(lastDate)}&lastId=${lastId}`)
                    .then(res => res.text())
                    .then(html => {
                        if (html.trim() === '') {
                            hasMoreTx = false; // Hết dữ liệu
                        } else {
                            txList.insertAdjacentHTML('beforeend', html); // Ghép nối vào UI
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