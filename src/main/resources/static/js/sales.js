(function () {
  'use strict';

  async function initSalePage() {
    const form = document.getElementById('sale-form');
    const linesContainer = document.getElementById('sale-lines');
    const addButton = document.getElementById('add-sale-line');
    const clientMessage = document.getElementById('sale-client-message');
    const successMessage = document.getElementById('sale-success-message');
    const errorMessage = document.getElementById('sale-error-message');
    if (!form || !linesContainer || !addButton) return;
    if (linesContainer.dataset.bound === 'true') return;
    linesContainer.dataset.bound = 'true';

    const template = linesContainer.querySelector('.sale-line');
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
      return Array.from(linesContainer.querySelectorAll('.sale-line'));
    }

    function getSelectedOption(select) {
      return select && select.selectedIndex >= 0 ? select.options[select.selectedIndex] : null;
    }

    function validateRows() {
      const seen = new Set();
      for (const row of getRows()) {
        const productSelect = row.querySelector('select[name="productId"]');
        const quantityInput = row.querySelector('input[name="quantity"]');
        const priceInput = row.querySelector('input[name="salePrice"]');
        const productId = productSelect ? productSelect.value.trim() : '';
        const quantity = quantityInput ? Number(quantityInput.value) : 0;
        const price = priceInput ? Number(priceInput.value) : NaN;
        const selectedOption = getSelectedOption(productSelect);
        const stock = selectedOption ? Number(selectedOption.dataset.stockQuantity || '0') : 0;

        if (!productId) {
          showClientMessage('error', 'Each sale line must have a product.');
          return false;
        }
        if (seen.has(productId)) {
          showClientMessage('error', 'A product can only appear once in a sale receipt.');
          return false;
        }
        seen.add(productId);

        if (!Number.isFinite(quantity) || quantity <= 0) {
          showClientMessage('error', 'Each sale line must have a quantity greater than zero.');
          return false;
        }
        if (quantity > stock) {
          showClientMessage('error', 'Sale quantity cannot exceed current stock for the selected product.');
          return false;
        }
        if (!Number.isFinite(price) || price < 0) {
          showClientMessage('error', 'Each sale line must have a sale price of zero or greater.');
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
        const rows = linesContainer.querySelectorAll('.sale-line');
        if (rows.length === 1) return;
        const row = button.closest('.sale-line');
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
      row.querySelectorAll('.remove-sale-line').forEach(bindRemove);
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
      const removeButton = clone.querySelector('.remove-sale-line');
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

  window.initSalePage = initSalePage;
  document.addEventListener('DOMContentLoaded', function () {
    initSalePage().catch(function () {});
  });
})();
