document.addEventListener("DOMContentLoaded", function() {
    const eyeIcon = document.getElementById("eyeIcon");
    const passwordInput = document.getElementById("password");

    if (eyeIcon && passwordInput) {
        eyeIcon.addEventListener("click", function () {
            const isPassword = passwordInput.getAttribute("type") === "password";
            
            passwordInput.setAttribute("type", isPassword ? "text" : "password");
            
            if (isPassword) {
                this.src = "/img/hide.png";
                this.alt = "Ẩn mật khẩu";
                this.title = "Ẩn mật khẩu";
            } else {
                this.src = "/img/view.png";
                this.alt = "Hiện mật khẩu";
                this.title = "Hiển thị mật khẩu";
            }
        });
    }
});