(function () {
  'use strict';

  let sidebarBound = false;
  const NAV_DELAY = 140;

  async function appInitMain() {
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
    const inventorySearch = document.getElementById('inventory-search');

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
        if (submitter && submitter.dataset && submitter.dataset.operation) {
          selectedOperation = submitter.dataset.operation;
        }
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

    wireOpenModalButtons();

    if (inventorySearch) {
      inventorySearch.addEventListener('input', function () {
        const keyword = inventorySearch.value.trim().toLowerCase();
        document.querySelectorAll('.inventory-row').forEach(function (row) {
          const haystack = row.dataset.search || '';
          row.classList.toggle('hidden', keyword.length > 0 && !haystack.includes(keyword));
        });
      });
    }

    if (!sidebarBound) {
      bindSidebarLinks();
      sidebarBound = true;
    }
  }

  function bindSidebarLinks() {
    const links = document.querySelectorAll('aside a[href]:not([href^="#"])');
    if (!links.length) return;

    let navTimeout = null;

    function clearNav() {
      if (navTimeout) {
        clearTimeout(navTimeout);
        navTimeout = null;
      }
    }

    links.forEach(function (link) {
      link.addEventListener('pointerdown', function (event) {
        if (event.button !== 0) return;
        link.classList.add('pressed');
      });

      ['pointerup', 'pointercancel', 'pointerout', 'pointerleave'].forEach(function (eventName) {
        link.addEventListener(eventName, function () {
          link.classList.remove('pressed');
          clearNav();
        });
      });

      link.addEventListener('click', function (event) {
        if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey || (event.button && event.button !== 0)) return;
        const raw = link.getAttribute('data-href') || link.getAttribute('href');
        if (!raw || raw === '#') return;

        let href;
        try {
          href = new URL(raw, location.href).href;
        } catch (_) {
          return;
        }

        if (new URL(href).origin !== location.origin) return;

        event.preventDefault();
        clearNav();
        navTimeout = setTimeout(function () {
          appLoadMain(href, true).then(function () {
            setActiveByUrl();
          }).catch(function () {
            window.location.assign(href);
          });
        }, NAV_DELAY);
      });
    });

    function setActiveSidebarLink(clicked) {
      const all = document.querySelectorAll('aside a[href]');
      all.forEach(function (anchor) {
        anchor.classList.remove('bg-[#005FB8]', 'text-white', 'shadow-sm');
      });
      if (clicked) clicked.classList.add('bg-[#005FB8]', 'text-white', 'shadow-sm');
    }

    function setActiveByUrl() {
      const all = Array.from(document.querySelectorAll('aside a[href]'));
      const current = location.pathname.replace(/\/$/, '') || '/';
      let best = null;
      let bestLen = -1;

      all.forEach(function (anchor) {
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

      if (best) setActiveSidebarLink(best);
    }
  }

  async function appLoadMain(url, addToHistory) {
    try {
      const response = await fetch(url, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' },
        credentials: 'same-origin'
      });
      if (!response.ok) throw new Error('Network response not ok');
      const text = await response.text();
      const parser = new DOMParser();
      const doc = parser.parseFromString(text, 'text/html');
      const newMain = doc.getElementById('site-main');
      const currentMain = document.getElementById('site-main');
      if (!newMain || !currentMain) {
        window.location.assign(url);
        return;
      }

      currentMain.replaceWith(newMain);
      if (doc.title) document.title = doc.title;
      await appInitMain();

      if (addToHistory) {
        history.pushState({ pjax: true }, '', url);
      }

      if (newMain.scrollIntoView) newMain.scrollIntoView({ behavior: 'auto' });
    } catch (_) {
      window.location.assign(url);
    }
  }

  window.addEventListener('popstate', function () {
    const url = location.pathname + location.search;
    appLoadMain(url, false).catch(function () {
      window.location.reload();
    });
  });

  window.appInitMain = appInitMain;
  window.appLoadMain = appLoadMain;

  document.addEventListener('DOMContentLoaded', function () {
    const main = document.querySelector('main');
    if (main && !main.id) main.id = 'site-main';
    appInitMain().catch(function () {});
  });
})();
