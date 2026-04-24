(function () {
  'use strict';

  async function initImportPage() {
    const form = document.getElementById('import-form');
    const linesContainer = document.getElementById('import-lines');
    const addButton = document.getElementById('add-import-line');
    const clientMessage = document.getElementById('import-client-message');
    const successMessage = document.getElementById('import-success-message');
    const errorMessage = document.getElementById('import-error-message');
    if (!form || !linesContainer || !addButton) return;
    if (linesContainer.dataset.bound === 'true') return;
    linesContainer.dataset.bound = 'true';

    const template = linesContainer.querySelector('.import-line');
    if (!template) return;

    function showClientMessage(type, message) {
      if (!clientMessage) return;
      clientMessage.className = 'rounded-xl px-4 py-3 text-sm font-semibold';
      if (type === 'success') {
        clientMessage.classList.add('border', 'border-emerald-200', 'bg-emerald-50', 'text-emerald-800');
      } else {
        clientMessage.classList.add('border', 'border-red-200', 'bg-red-50', 'text-red-800');
      }
      clientMessage.textContent = message;
      clientMessage.classList.remove('hidden');
      if (successMessage) successMessage.classList.add('hidden');
      if (errorMessage) errorMessage.classList.add('hidden');
    }

    function hideClientMessage() {
      if (clientMessage) clientMessage.classList.add('hidden');
    }

    function getRows() {
      return Array.from(linesContainer.querySelectorAll('.import-line'));
    }

    function validateRows() {
      const seen = new Set();
      for (const row of getRows()) {
        const productSelect = row.querySelector('select[name="productId"]');
        const quantityInput = row.querySelector('input[name="quantity"]');
        const priceInput = row.querySelector('input[name="importPrice"]');
        const productId = productSelect ? productSelect.value.trim() : '';
        const quantity = quantityInput ? Number(quantityInput.value) : 0;
        const price = priceInput ? Number(priceInput.value) : NaN;

        if (!productId) {
          showClientMessage('error', 'Each import line must have a product.');
          return false;
        }
        if (seen.has(productId)) {
          showClientMessage('error', 'A product can only appear once in an import receipt.');
          return false;
        }
        seen.add(productId);

        if (!Number.isFinite(quantity) || quantity <= 0) {
          showClientMessage('error', 'Each import line must have a quantity greater than zero.');
          return false;
        }
        if (!Number.isFinite(price) || price < 0) {
          showClientMessage('error', 'Each import line must have an import price of zero or greater.');
          return false;
        }
      }

      hideClientMessage();
      return true;
    }

    function bindRemove(button) {
      if (button.dataset.bound === 'true') return;
      button.dataset.bound = 'true';
      button.addEventListener('click', function () {
        const rows = linesContainer.querySelectorAll('.import-line');
        if (rows.length === 1) return;
        const row = button.closest('.import-line');
        if (row) row.remove();
        validateRows();
      });
    }

    function bindRow(row) {
      row.querySelectorAll('select, input').forEach(function (field) {
        if (field.dataset.bound === 'true') return;
        field.dataset.bound = 'true';
        field.addEventListener('change', validateRows);
        field.addEventListener('input', validateRows);
      });
      row.querySelectorAll('.remove-import-line').forEach(bindRemove);
    }

    getRows().forEach(bindRow);

    addButton.addEventListener('click', function () {
      const clone = template.cloneNode(true);
      clone.querySelectorAll('input').forEach(function (input) {
        input.value = input.name === 'quantity' ? '1' : '0.00';
      });
      clone.querySelectorAll('select').forEach(function (select) {
        select.selectedIndex = 0;
      });
      const removeButton = clone.querySelector('.remove-import-line');
      if (removeButton) bindRemove(removeButton);
      bindRow(clone);
      linesContainer.appendChild(clone);
    }, { once: false });

    form.addEventListener('submit', function (event) {
      if (!validateRows()) {
        event.preventDefault();
      }
    });
  }

  window.initImportPage = initImportPage;
  document.addEventListener('DOMContentLoaded', function () {
    initImportPage().catch(function () {});
  });
})();
