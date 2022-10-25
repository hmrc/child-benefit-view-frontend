window.dataLayer = window.dataLayer || [];
gtmDataLayer = document.getElementById('gtmDataLayer');
window.dataLayer.push({
    'event': gtmDataLayer && gtmDataLayer.getAttribute('data-event'),
    'service': 'child-benefit-service',
    'navigate': gtmDataLayer && gtmDataLayer.getAttribute('data-navigate'),
    'page_title': document.title,
    'timestamp': Date.now()
});
