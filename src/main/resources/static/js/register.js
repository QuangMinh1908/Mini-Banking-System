document.addEventListener("DOMContentLoaded", function() {
    // 1. Logic ẩn/hiện mật khẩu
    const togglePasswordBtn = document.getElementById("togglePasswordBtn");
    const passwordInput = document.getElementById("password");

    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener("click", function () {
            const isPassword = passwordInput.getAttribute("type") === "password";
            passwordInput.setAttribute("type", isPassword ? "text" : "password");
            this.textContent = isPassword ? "🙈" : "👁️";
            this.title = isPassword ? "Ẩn mật khẩu" : "Hiển thị mật khẩu";
        });
    }

    // 2. chặn nhấn Enter submit form ở Bước 1
    const step1Inputs = document.querySelectorAll('#step1 input');
    step1Inputs.forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                goToStep2();
            }
        });
    });
});

function goToStep2() {
    const step1Inputs = document.querySelectorAll('#step1 input');
    let isValid = true;
    
    step1Inputs.forEach(input => {
        if(!input.checkValidity()) {
            input.reportValidity();
            isValid = false;
        }
    });

    if(isValid) {
        const username = document.getElementById('username').value;
        const phoneNumber = document.getElementById('phoneNumber').value;
        const btn = document.querySelector('#step1 button');
        
        const originalText = btn.innerHTML;
        btn.innerHTML = 'Đang kiểm tra...';
        btn.disabled = true;

        fetch(`/api/auth/check-step1?username=${encodeURIComponent(username)}&phoneNumber=${encodeURIComponent(phoneNumber)}`)
            .then(res => res.json().then(data => ({ status: res.status, body: data })))
            .then(result => {
                if (result.status !== 200) {
                    alert(result.body.error);
                } else {
                    document.getElementById('step1').style.display = 'none';
                    document.getElementById('step2').style.display = 'block';
                }
            })
            .catch(err => {
                alert("Lỗi kết nối đến máy chủ!");
            })
            .finally(() => {
                btn.innerHTML = originalText;
                btn.disabled = false;
            });
    }
}

function goToStep1() {
    document.getElementById('step2').style.display = 'none';
    document.getElementById('step1').style.display = 'block';
}