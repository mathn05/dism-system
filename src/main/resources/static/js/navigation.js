(function () {
  'use strict';

  function setActiveSidebarLinkByPath() {
    const links = Array.from(document.querySelectorAll('aside a[href]'));
    if (!links.length) return;

    const current = location.pathname.replace(/\/$/, '') || '/';
    let best = null;
    let bestLen = -1;

    links.forEach(function (anchor) {
      let href = anchor.getAttribute('href') || anchor.getAttribute('data-href') || '';
      try {
        href = new URL(href, location.href).pathname.replace(/\/$/, '') || '/';
      } catch (_) {
        return;
      }
      if (current === href || (current.startsWith(href) && href.length > bestLen)) {
        best = anchor;
        bestLen = href.length;
      }
    });

    links.forEach(function (anchor) {
      anchor.classList.remove('bg-[#005FB8]', 'text-white', 'shadow-sm');
    });

    if (best) {
      best.classList.add('bg-[#005FB8]', 'text-white', 'shadow-sm');
    }
  }

  document.addEventListener('DOMContentLoaded', setActiveSidebarLinkByPath);
})();
