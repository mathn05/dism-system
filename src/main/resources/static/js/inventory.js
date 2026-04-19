(function () {
  'use strict';

  const loadedScriptUrls = new Set(
    Array.from(document.querySelectorAll('script[src]')).map(function (script) {
      return script.src;
    })
  );
  let sidebarBound = false;
  const NAV_DELAY = 140;

  function loadPageScripts(doc) {
    const scripts = Array.from(doc.querySelectorAll('script[src]'));
    return Promise.all(scripts.map(function (script) {
      const src = script.src;
      if (!src || loadedScriptUrls.has(src)) {
        return Promise.resolve();
      }

      return new Promise(function (resolve, reject) {
        const element = document.createElement('script');
        element.src = src;
        if (script.defer) element.defer = true;
        element.onload = function () {
          loadedScriptUrls.add(src);
          resolve();
        };
        element.onerror = reject;
        document.body.appendChild(element);
      });
    }));
  }

  async function initInventoryPage() {
    if (!document.getElementById('adjust-modal-form') || !document.getElementById('inventory-table-body')) {
      return;
    }

    const modal = document.getElementById('inventory-adjust-modal');
    const closeModalButton = document.getElementById('close-adjust-modal');
    const form = document.getElementById('adjust-modal-form');
    const operationField = document.getElementById('modal-operation');
    const asyncMessage = document.getElementById('inventory-async-message');
    const successMessage = document.getElementById('inventory-success-message');
    const errorMessage = document.getElementById('inventory-error-message');
    const trackedProductsValue = document.getElementById('tracked-products-value');
    const totalUnitsValue = document.getElementById('total-units-value');
    const lowStockCountValue = document.getElementById('low-stock-count-value');
    const lowStockThresholdLabel = document.getElementById('low-stock-threshold-label');
    const modalProductId = document.getElementById('modal-product-id');
    const modalProductName = document.getElementById('modal-product-name');
    const modalCurrentQuantity = document.getElementById('modal-current-quantity');
    const modalQuantity = document.getElementById('modal-quantity');
    const inventorySearch = document.getElementById('inventory-search');
    const deleteInventoryItemButton = document.getElementById('delete-inventory-item');
    const deleteUrl = form ? form.dataset.adjustUrl.replace('/adjust-ajax', '/delete-ajax') : '';

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

    function renderEmptyState() {
      const tableBody = document.getElementById('inventory-table-body');
      if (!tableBody) return;

      const existing = document.getElementById('inventory-empty-row');
      const hasRows = tableBody.querySelector('.inventory-row');
      if (!hasRows && !existing) {
        const row = document.createElement('tr');
        row.id = 'inventory-empty-row';
        row.innerHTML = '<td class="px-6 py-10 text-center text-sm text-slate-500" colspan="5">No inventory has been recorded for this station yet.</td>';
        tableBody.appendChild(row);
      }
      if (hasRows && existing) {
        existing.remove();
      }
    }

    function updateSummary(data) {
      if (trackedProductsValue && typeof data.trackedProducts !== 'undefined') trackedProductsValue.textContent = String(data.trackedProducts);
      if (totalUnitsValue) totalUnitsValue.textContent = String(data.totalUnits);
      if (lowStockCountValue) lowStockCountValue.textContent = String(data.lowStockCount);
      if (lowStockThresholdLabel) lowStockThresholdLabel.textContent = '\u2264 ' + data.lowStockThreshold + ' units';
    }

    function sortRows() {
      const tableBody = document.getElementById('inventory-table-body');
      if (!tableBody) return;

      const rows = Array.from(tableBody.querySelectorAll('.inventory-row'));
      rows.sort(function (a, b) {
        const stockDiff = Number(a.dataset.stockQuantity || '0') - Number(b.dataset.stockQuantity || '0');
        if (stockDiff !== 0) return stockDiff;
        return Number(a.dataset.defaultOrder || '0') - Number(b.dataset.defaultOrder || '0');
      });

      rows.forEach(function (row) {
        tableBody.appendChild(row);
      });
    }

    function updateRowAndSummary(data) {
      const productId = String(data.productId || '');
      const row = document.querySelector('tr[data-product-id="' + productId + '"]');
      if (row) {
        row.dataset.currentQuantity = String(data.updatedQuantity);
        row.dataset.stockQuantity = String(data.updatedQuantity);
        const stockValue = row.querySelector('.stock-value');
        const stockWrapper = row.querySelector('.stock-value-wrapper');
        const stockBar = row.querySelector('.stock-bar');
        if (stockValue) stockValue.textContent = String(data.updatedQuantity);
        const threshold = Number(data.lowStockThreshold || 0);
        const isLowStock = Number(data.updatedQuantity) <= threshold;
        if (stockWrapper) {
          stockWrapper.classList.toggle('text-red-600', isLowStock);
          stockWrapper.classList.toggle('text-slate-900', !isLowStock);
        }
        if (stockBar) {
          stockBar.classList.toggle('bg-red-500', isLowStock);
          stockBar.classList.toggle('bg-[#004692]', !isLowStock);
          stockBar.style.width = Math.min(Number(data.updatedQuantity), 100) + '%';
        }
      }
      updateSummary(data);
      renderEmptyState();
      sortRows();
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

      if (deleteInventoryItemButton) {
        deleteInventoryItemButton.addEventListener('click', async function () {
          const productId = modalProductId ? modalProductId.value.trim() : '';
          if (!productId) return;
          if (!window.confirm('Delete this product from inventory?')) return;

          const csrfField = form.querySelector('input[type="hidden"][name]');
          const payload = new URLSearchParams();
          payload.set('productId', productId);
          if (csrfField && csrfField.name) {
            payload.set(csrfField.name, csrfField.value);
          }

          try {
            const response = await fetch(deleteUrl, {
              method: 'POST',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
              body: payload.toString()
            });
            const data = await response.json();
            if (!response.ok || !data.success) {
              showAsyncMessage(false, data.message || 'Unable to delete product.');
              return;
            }

            updateRowAndSummary({
              productId: data.productId,
              updatedQuantity: 0,
              deleted: true,
              trackedProducts: data.trackedProducts,
              totalUnits: data.totalUnits,
              lowStockCount: data.lowStockCount,
              lowStockThreshold: data.lowStockThreshold
            });
            showAsyncMessage(true, data.message || 'Product deleted from inventory.');
            hideModal();
          } catch (_) {
            showAsyncMessage(false, 'Network error while deleting product. Please try again.');
          }
        });
      }

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
    renderEmptyState();
    sortRows();

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
      await loadPageScripts(doc);
      if (typeof window.initInventoryPage === 'function') await window.initInventoryPage();
      if (typeof window.initCustomerPage === 'function') await window.initCustomerPage();
      if (typeof window.initOrderPage === 'function') await window.initOrderPage();

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

  window.initInventoryPage = initInventoryPage;
  window.appLoadMain = appLoadMain;

  document.addEventListener('DOMContentLoaded', function () {
    const main = document.querySelector('main');
    if (main && !main.id) main.id = 'site-main';
    initInventoryPage().catch(function () {});
  });
})();
