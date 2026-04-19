(function () {
    'use strict';

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

    function postForm(url, formData) {
        return fetch(url, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            body: appendCsrf(formData),
            credentials: 'same-origin'
        }).then(function (response) {
            if (!response.ok) {
                return response.text().then(function (message) {
                    throw new Error(message || 'Request failed');
                });
            }
            return response.json();
        });
    }

    function renderMessage(type, message) {
        const box = document.getElementById('order-async-message');
        if (!box) return;

        box.className = 'mb-6 rounded-xl px-4 py-3 text-sm font-semibold';
        if (type === 'success') {
            box.classList.add('border', 'border-emerald-200', 'bg-emerald-50', 'text-emerald-800');
        } else {
            box.classList.add('border', 'border-error-container', 'bg-error-container', 'text-on-error-container');
        }
        box.textContent = message;
        box.classList.remove('hidden');
    }

    function initOrderPage() {
        const createForm = document.querySelector('form[action$="/order/create"]');
        if (!createForm || createForm.dataset.bound === 'true') return;
        createForm.dataset.bound = 'true';

        createForm.addEventListener('submit', function (event) {
            event.preventDefault();
            const formData = new FormData(createForm);

            postForm(createForm.action, formData)
                .then(function () {
                    renderMessage('success', 'Order created successfully.');
                    window.location.reload();
                })
                .catch(function (error) {
                    renderMessage('error', error.message || 'Failed to create order.');
                });
        });
    }

    window.initOrderPage = initOrderPage;

    document.addEventListener('DOMContentLoaded', function () {
        initOrderPage();
    });
})();
