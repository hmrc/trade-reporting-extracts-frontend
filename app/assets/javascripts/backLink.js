// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
  window.history.replaceState(null, null, window.location.href);
}

document.addEventListener('DOMContentLoaded', function(event) {

  // handle back click
  var backLink = document.querySelector('.govuk-back-link');
  if (backLink !== null && backLink.getAttribute("href") === "#") {
    backLink.addEventListener('click', function(e){
      e.preventDefault();
      e.stopPropagation();
      window.history.back();
    });
  }

  // handle download link click to trigger hidden button
    document.querySelectorAll('a.govuk-link[onclick]').forEach(function(link) {
      link.addEventListener('click', function(e) {
        e.preventDefault();
        var form = this.closest('form');
        if (form) {
          var button = form.querySelector('#submitbutton');
          if (button) {
            button.click();
          }
        }
      });
    });
});