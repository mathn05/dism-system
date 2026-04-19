(function () {
  'use strict';

  const loadedScriptUrls = new Set(
    Array.from(document.querySelectorAll('script[src]')).map(function (script) {
      return script.src;
    })
  );
  let sidebarBound = false;
  const NAV_DELAY = 140;

  function getCsrf() {
    const parameterMeta = document.querySelector('meta[name="_csrf_parameter"]');
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    return {
      parameter: parameterMeta ? parameterMeta.content : '_csrf',
      token: tokenMeta ? tokenMeta.content : ''
    };
  }

  function appendCsrf(formData) {
    const csrf = getCsrf();
    if (csrf.token && !formData.has(csrf.parameter)) {
      formData.append(csrf.parameter, csrf.token);
    }
    return formData;
  }

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

  async function initCustomerPage() {
    if (!document.getElementById('customer-form') || !document.getElementById('customer-table-body')) {
      return;
    }

    const modal = document.getElementById('customer-modal');
    const modalTitle = document.getElementById('customer-modal-title');
    const modalSubtitle = document.getElementById('customer-modal-subtitle');
    const closeModalButton = document.getElementById('close-customer-modal');
    const cancelModalButton = document.getElementById('cancel-customer-modal');
    const form = document.getElementById('customer-form');
    const modeField = document.getElementById('customer-form-mode');
    const customerIdField = document.getElementById('customer-id-field');
    const customerNameField = document.getElementById('customer-name-field');
    const customerPhoneField = document.getElementById('customer-phone-field');
    const customerAddressField = document.getElementById('customer-address-field');
    const deleteCustomerButton = document.getElementById('delete-customer-button');
    const customerSearch = document.getElementById('customer-search');
    const asyncMessage = document.getElementById('customer-async-message');
    const customerCountValue = document.getElementById('customer-count-value');
    const customerTableBody = document.getElementById('customer-table-body');

    function showAsyncMessage(type, message) {
      if (!asyncMessage) return;
      asyncMessage.className = 'rounded-xl px-4 py-3 text-sm font-semibold';
      if (type === 'success') {
        asyncMessage.classList.add('border', 'border-emerald-200', 'bg-emerald-50', 'text-emerald-800');
      } else {
        asyncMessage.classList.add('border', 'border-red-200', 'bg-red-50', 'text-red-800');
      }
      asyncMessage.textContent = message;
      asyncMessage.classList.remove('hidden');
    }

    function showModal() {
      if (!modal) return;
      modal.classList.remove('hidden');
      modal.classList.add('flex');
    }

    function hideModal() {
      if (!modal) return;
      modal.classList.add('hidden');
      modal.classList.remove('flex');
      if (form) form.reset();
      if (modeField) modeField.value = 'create';
      if (customerIdField) {
        customerIdField.readOnly = false;
        customerIdField.classList.remove('bg-slate-100', 'text-slate-500');
      }
      if (deleteCustomerButton) deleteCustomerButton.classList.add('hidden');
      if (modalTitle) modalTitle.textContent = 'Add customer';
      if (modalSubtitle) modalSubtitle.textContent = 'Create a customer profile for sale orders.';
    }

    function openCreateModal() {
      if (form) form.reset();
      if (modeField) modeField.value = 'create';
      if (customerIdField) {
        customerIdField.readOnly = false;
        customerIdField.classList.remove('bg-slate-100', 'text-slate-500');
      }
      if (modalTitle) modalTitle.textContent = 'Add customer';
      if (modalSubtitle) modalSubtitle.textContent = 'Create a customer profile for sale orders.';
      showModal();
      if (customerIdField) customerIdField.focus();
    }

    function openEditModal(customerId, customerName, customerPhone, customerAddress) {
      if (modeField) modeField.value = 'edit';
      if (customerIdField) {
        customerIdField.value = customerId || '';
        customerIdField.readOnly = true;
        customerIdField.classList.add('bg-slate-100', 'text-slate-500');
      }
      if (customerNameField) customerNameField.value = customerName || '';
      if (customerPhoneField) customerPhoneField.value = customerPhone || '';
      if (customerAddressField) customerAddressField.value = customerAddress || '';
      if (deleteCustomerButton) deleteCustomerButton.classList.remove('hidden');
      if (modalTitle) modalTitle.textContent = 'Edit customer';
      if (modalSubtitle) modalSubtitle.textContent = 'Update customer information while keeping the original ID.';
      showModal();
      if (customerNameField) customerNameField.focus();
    }

    function renderEmptyState() {
      if (!customerTableBody) return;
      const existing = document.getElementById('customer-empty-row');
      const hasRows = customerTableBody.querySelector('.customer-row');
      if (!hasRows && !existing) {
        const row = document.createElement('tr');
        row.id = 'customer-empty-row';
        row.innerHTML = '<td class="px-6 py-10 text-center text-sm text-slate-500" colspan="5">No customer records yet.</td>';
        customerTableBody.appendChild(row);
      }
      if (hasRows && existing) {
        existing.remove();
      }
    }

    function createRow(customer) {
      const row = document.createElement('tr');
      row.className = 'hover:bg-slate-50 transition-colors customer-row';
      row.innerHTML = ''
        + '<td class="px-4 py-4 font-headline text-sm font-bold text-[#004692]"></td>'
        + '<td class="px-4 py-4 text-sm font-semibold text-slate-900"></td>'
        + '<td class="px-4 py-4 text-sm text-slate-700"></td>'
        + '<td class="px-4 py-4 text-sm text-slate-600"></td>'
        + '<td class="px-4 py-4 text-right">'
        + '  <button class="open-edit-customer inline-flex items-center gap-2 rounded-lg bg-slate-100 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-200 transition-colors" type="button">'
        + '    <span class="material-symbols-outlined text-sm">edit</span>'
        + '    Edit'
        + '  </button>'
        + '  <button class="open-delete-customer ml-2 inline-flex items-center gap-2 rounded-lg bg-red-50 px-3 py-2 text-xs font-bold text-red-600 hover:bg-red-100 transition-colors" type="button">'
        + '    <span class="material-symbols-outlined text-sm">delete</span>'
        + '    Delete'
        + '  </button>'
        + '</td>';
      updateRow(row, customer);
      return row;
    }

    function updateRow(row, customer) {
      row.dataset.customerId = customer.id;
      row.dataset.customerName = customer.name;
      row.dataset.customerPhone = customer.phoneNumber;
      row.dataset.customerAddress = customer.address;
      row.dataset.search = (String(customer.id) + ' ' + String(customer.name) + ' ' + String(customer.phoneNumber) + ' ' + String(customer.address)).toLowerCase();
      const editButton = row.querySelector('.open-edit-customer');
      if (editButton) {
        editButton.dataset.customerId = customer.id;
        editButton.dataset.customerName = customer.name;
        editButton.dataset.customerPhone = customer.phoneNumber;
        editButton.dataset.customerAddress = customer.address;
      }
      const deleteButton = row.querySelector('.open-delete-customer');
      if (deleteButton) {
        deleteButton.dataset.customerId = customer.id;
        deleteButton.dataset.customerName = customer.name;
      }
      const cells = row.querySelectorAll('td');
      if (cells[0]) cells[0].textContent = customer.id;
      if (cells[1]) cells[1].textContent = customer.name;
      if (cells[2]) cells[2].textContent = customer.phoneNumber;
      if (cells[3]) cells[3].textContent = customer.address;
    }

    function findCustomerRow(customerId) {
      return Array.from(document.querySelectorAll('.customer-row')).find(function (row) {
        return row.dataset.customerId === customerId;
      }) || null;
    }

    function upsertCustomerRow(customer, isNew) {
      if (!customerTableBody) return;
      let row = findCustomerRow(customer.id);
      if (!row) {
        row = createRow(customer);
        customerTableBody.prepend(row);
      } else {
        updateRow(row, customer);
      }
      bindEditButtons();
      bindDeleteButtons();
      renderEmptyState();
      if (isNew && customerSearch && customerSearch.value.trim()) {
        applySearch(customerSearch.value.trim().toLowerCase());
      }
    }

    function applySearch(keyword) {
      document.querySelectorAll('.customer-row').forEach(function (row) {
        const haystack = row.dataset.search || '';
        row.classList.toggle('hidden', keyword.length > 0 && !haystack.includes(keyword));
      });
    }

    function bindEditButtons() {
      document.querySelectorAll('.open-edit-customer').forEach(function (button) {
        if (button.dataset.bound === 'true') return;
        button.dataset.bound = 'true';
        button.addEventListener('click', function () {
          openEditModal(
            this.dataset.customerId || '',
            this.dataset.customerName || '',
            this.dataset.customerPhone || '',
            this.dataset.customerAddress || ''
          );
        });
      });
    }

    function removeCustomerRow(customerId) {
      const row = findCustomerRow(customerId);
      if (row) row.remove();
      renderEmptyState();
    }

    function bindDeleteButtons() {
      document.querySelectorAll('.open-delete-customer').forEach(function (button) {
        if (button.dataset.bound === 'true') return;
        button.dataset.bound = 'true';
        button.addEventListener('click', async function () {
          const customerId = this.dataset.customerId || '';
          const customerName = this.dataset.customerName || customerId;
          await deleteCustomer(customerId, customerName);
        });
      });
    }

    async function deleteCustomer(customerId, customerName) {
      if (!customerId) return;
      if (!window.confirm('Delete customer "' + customerName + '"?')) return;

      const deleteUrl = form ? (form.dataset.deleteUrl || '') : '';
      const formData = appendCsrf(new FormData());
      formData.append('customerId', customerId);

      try {
        const response = await fetch(deleteUrl, {
          method: 'POST',
          headers: { 'X-Requested-With': 'XMLHttpRequest' },
          body: formData,
          credentials: 'same-origin'
        });
        const data = await response.json();
        if (!response.ok || !data.success) {
          showAsyncMessage('error', data.message || 'Unable to delete customer.');
          return;
        }

        removeCustomerRow(data.customerId);
        if (customerCountValue) customerCountValue.textContent = String(data.customerCount);
        showAsyncMessage('success', data.message || 'Customer deleted successfully.');
        hideModal();
      } catch (_) {
        showAsyncMessage('error', 'Network error while deleting customer. Please try again.');
      }
    }

    document.querySelectorAll('#open-create-customer, #open-create-customer-secondary').forEach(function (button) {
      button.addEventListener('click', openCreateModal);
    });

    if (closeModalButton) closeModalButton.addEventListener('click', hideModal);
    if (cancelModalButton) cancelModalButton.addEventListener('click', hideModal);
    if (modal) {
      modal.addEventListener('click', function (event) {
        if (event.target === modal) hideModal();
      });
    }

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape' && modal && !modal.classList.contains('hidden')) {
        hideModal();
      }
    });

    if (customerSearch) {
      customerSearch.addEventListener('input', function () {
        applySearch(customerSearch.value.trim().toLowerCase());
      });
    }

    if (form) {
      form.addEventListener('submit', async function (event) {
        event.preventDefault();

        const mode = modeField ? modeField.value : 'create';
        const createUrl = form.dataset.createUrl || '';
        const updateUrl = form.dataset.updateUrl || '';
        const submitUrl = mode === 'edit' ? updateUrl : createUrl;

        const formData = appendCsrf(new FormData(form));

        try {
          const response = await fetch(submitUrl, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            body: formData,
            credentials: 'same-origin'
          });
          const data = await response.json();
          if (!response.ok || !data.success) {
            showAsyncMessage('error', data.message || 'Unable to save customer.');
            return;
          }

          upsertCustomerRow(data.customer, mode !== 'edit');
          if (customerCountValue) customerCountValue.textContent = String(data.customerCount);
          showAsyncMessage('success', data.message || 'Customer saved successfully.');
          hideModal();
        } catch (_) {
          showAsyncMessage('error', 'Network error while saving customer. Please try again.');
        }
      });
    }

    if (deleteCustomerButton) {
      deleteCustomerButton.addEventListener('click', async function () {
        await deleteCustomer(
          customerIdField ? customerIdField.value.trim() : '',
          customerNameField ? customerNameField.value.trim() : ''
        );
      });
    }

    bindEditButtons();
    bindDeleteButtons();
    renderEmptyState();

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

    setActiveByUrl();
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

  window.initCustomerPage = initCustomerPage;
  window.appLoadMain = appLoadMain;

  document.addEventListener('DOMContentLoaded', function () {
    const main = document.querySelector('main');
    if (main && !main.id) main.id = 'site-main';
    initCustomerPage().catch(function () {});
  });
})();
