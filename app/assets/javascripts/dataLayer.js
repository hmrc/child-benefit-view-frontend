window.dataLayer = window.dataLayer || [];
gtmDataLayer = document.getElementById('gtmDataLayer');
window.dataLayer.push({
    'event': gtmDataLayer && gtmDataLayer.getAttribute('event'),
    'service': 'child-benefit-service',
    'navigate': gtmDataLayer && gtmDataLayer.getAttribute('navigate'),
    'page_title': document.title,
    'timestamp': Date.now()
});
