const API = "http://localhost:9090";

function authHeader() {
    return {
        "Authorization": "Basic " + btoa(localStorage.user + ":" + localStorage.pass)
    };
}

function login() {
    localStorage.user = u.value;
    localStorage.pass = p.value;
    location.href = "dashboard.html";
}

function register() {
    fetch(API + "/auth/register", {
        method: "POST",
        headers: {"Content-Type":"application/json"},
        body: JSON.stringify({
            fullName: fn.value,
            username: u.value,
            password: p.value
        })
    }).then(() => alert("OK"));
}

function upload() {
    let fd = new FormData();
    fd.append("file", file.files[0]);
    fd.append("category", cat.value);

    fetch(API + "/templates/upload", {
        method: "POST",
        headers: authHeader(),
        body: fd
    }).then(() => alert("Отправлено админу"));
}
