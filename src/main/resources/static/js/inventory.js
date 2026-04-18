(function () {
  'use strict';

  // Track whether we've bound global sidebar handlers to avoid duplicates
  let sidebarBound = false;

  // NAV_DELAY used to show pressed animation before replacing content
  const NAV_DELAY = 140;

  // Initialize widgets and event handlers for the current main content.
  // This function can be called after we swap the main content via PJAX.
  async function appInitMain() {
    // Query elements that live inside the main content (may be absent on some pages)
    const modal = document.getElementById('inventory-adjust-modal');
    const closeModalButton = document.getElementById('close-adjust-modal');
    const form = document.getElementById('adjust-modal-form');
    const operationField = document.getElementById('modal-operation');
    const asyncMessage = document.getElementById('inventory-async-message');
    const successMessage = document.getElementById('inventory-success-message');
    const errorMessage = document.getElementById('inventory-error-message');
    const totalUnitsValue = document.getElementById('total-units-value');
    const lowStockCountValue = document.getElementById('low-stock-count-value');
    const lowStockThresholdLabel = document.getElementById('low-stock-threshold-label');
    const modalProductId = document.getElementById('modal-product-id');
    const modalProductName = document.getElementById('modal-product-name');
    const modalCurrentQuantity = document.getElementById('modal-current-quantity');
    const modalQuantity = document.getElementById('modal-quantity');

    // Utility to show/hide modal if present
    function showModal() {
      if (!modal) return;
      modal.classList.remove('hidden');
      modal.classList.add('flex');
    }
    function hideModal() {
      if (!modal) return;
      modal.classList.add('hidden');
      modal.classList.remove('flex');
      if (modalQuantity) modalQuantity.value = '1';
      if (operationField) operationField.value = 'increase';
    }

    function showAsyncMessage(isSuccess, message) {
      if (!asyncMessage) return;
      asyncMessage.className = 'rounded-xl px-4 py-3 text-sm font-semibold';
      if (isSuccess) {
        asyncMessage.classList.add('border', 'border-emerald-200', 'bg-emerald-50', 'text-emerald-800');
      } else {
        asyncMessage.classList.add('border', 'border-error-container', 'bg-error-container', 'text-on-error-container');
      }
      asyncMessage.textContent = message;
      asyncMessage.classList.remove('hidden');
      if (successMessage) successMessage.classList.add('hidden');
      if (errorMessage) errorMessage.classList.add('hidden');
    }

    function wireOpenModalButtons() {
      if (!document.querySelectorAll) return;
      const buttons = document.querySelectorAll('.open-adjust-modal');
      buttons.forEach(function (button) {
        button.addEventListener('click', function () {
          const row = this.closest('tr');
          const currentQty = row ? row.dataset.currentQuantity : '0';
          if (modalProductId) modalProductId.value = this.dataset.productId || '';
          if (modalProductName) modalProductName.textContent = this.dataset.productName || 'Product';
          if (modalCurrentQuantity) modalCurrentQuantity.textContent = currentQty || '0';
          showModal();
        });
      });
    }

    function updateRowAndSummary(data) {
      const productId = String(data.productId || '');
      const row = document.querySelector('tr[data-product-id="' + productId + '"]');
      if (row) {
        row.dataset.currentQuantity = String(data.updatedQuantity);
        const stockValue = row.querySelector('.stock-value');
        const stockWrapper = row.querySelector('.stock-value-wrapper');
        const stockBar = row.querySelector('.stock-bar');
        if (stockValue) stockValue.textContent = String(data.updatedQuantity);
        const threshold = Number(data.lowStockThreshold || 0);
        const isLowStock = Number(data.updatedQuantity) <= threshold;
        if (stockWrapper) {
          stockWrapper.classList.toggle('text-error', isLowStock);
          stockWrapper.classList.toggle('text-on-surface', !isLowStock);
        }
        if (stockBar) {
          stockBar.classList.toggle('bg-error', isLowStock);
          stockBar.classList.toggle('bg-primary', !isLowStock);
          stockBar.style.width = Math.min(Number(data.updatedQuantity), 100) + '%';
        }
      }
      if (totalUnitsValue) totalUnitsValue.textContent = String(data.totalUnits);
      if (lowStockCountValue) lowStockCountValue.textContent = String(data.lowStockCount);
      if (lowStockThresholdLabel) lowStockThresholdLabel.textContent = '\u2264 ' + data.lowStockThreshold + ' units';
    }

    // Wire modal/form only if present
    if (form) {
      const adjustUrl = form.dataset.adjustUrl;
      let selectedOperation = 'increase';
      form.querySelectorAll('button[type="submit"][data-operation]').forEach(function (button) {
        button.addEventListener('click', function () {
          selectedOperation = this.dataset.operation || 'increase';
          if (operationField) operationField.value = selectedOperation;
        });
      });

      if (closeModalButton) closeModalButton.addEventListener('click', hideModal);
      if (modal) {
        modal.addEventListener('click', function (event) {
          if (event.target === modal) hideModal();
        });
      }

      document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && modal && !modal.classList.contains('hidden')) hideModal();
      });

      form.addEventListener('submit', async function (event) {
        event.preventDefault();
        const submitter = event.submitter;
        if (submitter && submitter.dataset && submitter.dataset.operation) selectedOperation = submitter.dataset.operation;
        if (operationField) operationField.value = selectedOperation;
        const payload = new URLSearchParams(new FormData(form));
        try {
          const response = await fetch(adjustUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
            body: payload.toString()
          });
          const data = await response.json();
          if (!response.ok || !data.success) {
            showAsyncMessage(false, data.message || 'Unable to update stock.');
            return;
          }
          updateRowAndSummary(data);
          showAsyncMessage(true, data.message || 'Stock updated successfully.');
          hideModal();
        } catch (error) {
          showAsyncMessage(false, 'Network error while updating stock. Please try again.');
        }
      });
    }

    // Wire open modal buttons for the page
    wireOpenModalButtons();

    // Wire sidebar links globally one time (they live outside main)
    if (!sidebarBound) {
      bindSidebarLinks();
      sidebarBound = true;
    }
  }

  // Bind handlers for sidebar links (persistent area). This is done once.
  function bindSidebarLinks() {
    // Target anchors inside any aside (sidebar) so the sidebar stays persistent
    const links = document.querySelectorAll('aside a[href]:not([href^="#"])');
    if (!links || !links.length) return;

    let navTimeout = null;

    function clearNav() {
      if (navTimeout) {
        clearTimeout(navTimeout);
        navTimeout = null;
      }
    }

    links.forEach(link => {
      // pressed animation on pointerdown
      link.addEventListener('pointerdown', (e) => {
        // don't override middle-click or modifier actions
        if (e.button !== 0) return;
        link.classList.add('pressed');
      });
      // remove pressed state on pointerup/cancel
      ['pointerup', 'pointercancel', 'pointerout', 'pointerleave'].forEach(evt => {
        link.addEventListener(evt, () => {
          link.classList.remove('pressed');
          clearNav();
        });
      });

      // Intercept click to perform PJAX-like load for most same-origin sidebar links.
      // Do NOT intercept /dashboard so a full server-rendered navigation occurs (keeps dashboard identical to initial load).
      link.addEventListener('click', (e) => {
        // allow user to open in new tab / with modifier keys
        if (e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || (e.button && e.button !== 0)) return;
        const raw = link.getAttribute('data-href') || link.getAttribute('href');
        if (!raw || raw === '#') return;

        // Resolve and guard same-origin
        let href;
        try { href = new URL(raw, location.href).href; } catch (_) { return; }
        const pathname = new URL(href).pathname.replace(/\/$/, '') || '/';
        if (new URL(href).origin !== location.origin) return;

        // If navigating to the dashboard, allow a full navigation so the server-rendered dashboard is used
        if (pathname === '/dashboard' || pathname === '') {
          // Let the browser follow the link normally
          return;
        }

        // Otherwise intercept and do a PJAX-style load
        e.preventDefault();
        clearNav();
        navTimeout = setTimeout(() => {
          appLoadMain(href, true).then(() => {
            // ensure active is consistent with URL after load
            setActiveByUrl();
          }).catch(() => { window.location.assign(href); });
        }, NAV_DELAY);
      });

      // keyboard support
      link.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          const raw = link.getAttribute('data-href') || link.getAttribute('href');
          if (!raw || raw === '#') return;
          let href;
          try { href = new URL(raw, location.href).href; } catch (_) { return; }
          appLoadMain(href, true).then(() => { setActiveByUrl(); }).catch(() => { window.location.assign(href); });
        }
      });
    });

    function setActiveSidebarLink(clicked) {
      try {
        const all = document.querySelectorAll('aside a[href]');
        all.forEach(a => {
          a.classList.remove('bg-[#005FB8]', 'text-white', 'shadow-sm');
        });
        // add active appearance to clicked link
        if (clicked) clicked.classList.add('bg-[#005FB8]', 'text-white', 'shadow-sm');
      } catch (e) { /* silent */ }
    }

    // Set active sidebar item based on current location.pathname
    function setActiveByUrl() {
      try {
        const all = Array.from(document.querySelectorAll('aside a[href]'));
        // normalize pathnames and pick best match (longest prefix match)
        const current = location.pathname.replace(/\/$/, '') || '/';
        let best = null;
        let bestLen = -1;
        all.forEach(a => {
          let href = a.getAttribute('href') || a.getAttribute('data-href') || '';
          try { href = new URL(href, location.href).pathname.replace(/\/$/, '') || '/'; } catch (e) { return; }
          if (current === href) {
            best = a; bestLen = href.length; return;
          }
          // prefix match (e.g., /inventory/123 should match /inventory)
          if (current.startsWith(href) && href.length > bestLen) {
            best = a; bestLen = href.length;
          }
        });
        if (best) setActiveSidebarLink(best);
      } catch (e) { /* silent */ }
    }
  }

  // Load a page via fetch and swap its #site-main into the current document
  async function appLoadMain(url, addToHistory = true) {
    // absolute/relative same-origin guard
    try {
      const res = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' }, credentials: 'same-origin' });
      if (!res.ok) throw new Error('Network response not ok');
      const text = await res.text();
      const parser = new DOMParser();
      const doc = parser.parseFromString(text, 'text/html');
      const newMain = doc.getElementById('site-main');
      const currentMain = document.getElementById('site-main');
      if (!newMain || !currentMain) {
        // If structure is different, fallback to full navigation
        window.location.assign(url);
        return;
      }

      // Replace the main element (keeps sidebar intact)
      currentMain.replaceWith(newMain);

      // Update document title
      if (doc.title) document.title = doc.title;

      // Re-run initialization on the new content
      await appInitMain();

      // Push history state
      if (addToHistory) {
        try { history.pushState({ pjax: true }, '', url); } catch (e) { /* ignore */ }
      }

      // Scroll to top of content area
      if (newMain.scrollIntoView) newMain.scrollIntoView({ behavior: 'auto' });
    } catch (err) {
      // on any error, fallback to full navigation
      window.location.assign(url);
    }
  }

  // Handle back/forward — load content for the current location
  window.addEventListener('popstate', (e) => {
    // When the user navigates via browser controls, fetch the page and replace main
    const url = location.pathname + location.search;
    appLoadMain(url, false).catch(() => { window.location.reload(); });
  });

  // Expose to window so other scripts (or initial load) can call it
  window.appInitMain = appInitMain;
  window.appLoadMain = appLoadMain;

  // Run initial init for the first page load
  document.addEventListener('DOMContentLoaded', () => {
    // If main has no id, ensure there is an element with id site-main for swapping
    const main = document.querySelector('main');
    if (main && !main.id) main.id = 'site-main';
    // Initialize page widgets
    appInitMain().catch(() => { /* ignore init errors */ });
  });
})();

