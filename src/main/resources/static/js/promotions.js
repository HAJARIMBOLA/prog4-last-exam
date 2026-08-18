document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll(".download-graduates-link").forEach(function (link) {
    link.addEventListener("click", function (event) {
      event.preventDefault();
      fetch(link.href, { credentials: "same-origin" })
        .then(function (response) {
          if (!response.ok) {
            throw new Error("Download failed");
          }
          return response.json();
        })
        .then(function (data) {
          window.location.href = data.downloadUrl;
        })
        .catch(function () {
          alert("Le téléchargement a échoué. Réessaie plus tard.");
        });
    });
  });
});
