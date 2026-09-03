document.addEventListener("DOMContentLoaded", function () {

    // LOGIN PASSWORD & AUTOFILL FIX
    
    const password = document.getElementById("loginPassword");
    const toggleButton = document.getElementById("toggleLoginPassword");

    if (password && toggleButton) {
        password.value = "";

        let isMasked = true; // Tracks if password should be hidden
 // Mask characters as user types if state is set to hidden
        password.addEventListener("input", function () {
            if (isMasked && password.type !== "password") {
                password.type = "password";
            }
        });

        // Toggle visibility
        toggleButton.addEventListener("click", function (e) {
            e.preventDefault(); // Stop form focus shifts
            
            if (password.type === "password" || isMasked) {
                password.type = "text";
                toggleButton.textContent = "Hide";
                isMasked = false;
            } else {
                password.type = "password";
                toggleButton.textContent = "Show";
                isMasked = true;
            }
        });
    }

    // REGISTER PAGE
    
    const registerPassword = document.getElementById("registerPassword");
    const registerToggle = document.getElementById("registerPasswordBtn");

    if (registerPassword && registerToggle) {
        registerToggle.addEventListener("click", function (e) {
            e.preventDefault();
            if (registerPassword.type === "password") {
                registerPassword.type = "text";
                registerToggle.textContent = "Hide";
            } else {
                registerPassword.type = "password";
                registerToggle.textContent = "Show";
            }
        });
    }

    // STUDENT → SELECT TEACHER
    
    const role = document.getElementById("role");
    const teacherBox = document.getElementById("tb");

    if (role && teacherBox) {
        role.addEventListener("change", function () {
            if (role.value === "STUDENT") {
                teacherBox.style.display = "block";
            } else {
                teacherBox.style.display = "none";
            }
        });
    }

});