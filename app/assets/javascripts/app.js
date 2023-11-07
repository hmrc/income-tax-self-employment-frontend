// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
  window.history.replaceState(null, null, window.location.href);
}

document.addEventListener('DOMContentLoaded', function(event) {

  // handle back click
  var backLink = document.querySelector('.govuk-back-link');
  if (backLink !== null) {
    backLink.addEventListener('click', function(e){
      e.preventDefault();
      e.stopPropagation();
      window.history.back();
    });
  }

  // handle exclusive checkbox
  var checkboxes = document.querySelectorAll('.govuk-checkboxes__input');
  var exclusiveCheckbox = document.querySelector('[data-behaviour="exclusive"]');
  if (exclusiveCheckbox !== null) {
     checkboxes.forEach(function (checkbox) {
        checkbox.addEventListener('click', function(){
           if (checkbox === exclusiveCheckbox) {
              checkboxes.forEach(function (c) {
                 if (c !== exclusiveCheckbox) {
                    c.checked = false;
                 }
              });
           } else {
              checkboxes.forEach(function (c) {
                 if (c === exclusiveCheckbox) {
                    c.checked = false;
                 }
              });
           }
        });
     });
  }

});
