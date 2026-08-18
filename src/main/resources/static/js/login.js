document.addEventListener("DOMContentLoaded", function() {
    const togglePasswordBtn = document.getElementById("togglePasswordBtn");
    const passwordInput = document.getElementById("password");

    if(togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener("click", function () {
            const isPassword = passwordInput.getAttribute("type") === "password";
            passwordInput.setAttribute("type", isPassword ? "text" : "password");
            this.textContent = isPassword ? "🙈" : "👁️";
            this.title = isPassword ? "Ẩn mật khẩu" : "Hiển thị mật khẩu";
        });
    }
});