class LoaderService {
    constructor() {
        this.createLoaderElement();
    }

    createLoaderElement() {
        // Create loader overlay
        const overlay = document.createElement('div');
        overlay.className = 'loader-overlay';
        
        // Create loader container
        const container = document.createElement('div');
        container.className = 'loader-container';
        
        // Create spinner
        const spinner = document.createElement('div');
        spinner.className = 'loader';
        
        // Create loading text
        const text = document.createElement('div');
        text.className = 'loader-text';
        text.textContent = 'Loading...';
        
        // Assemble the elements
        container.appendChild(spinner);
        container.appendChild(text);
        overlay.appendChild(container);
        
        // Add to body
        document.body.appendChild(overlay);
        
        // Store reference
        this.loaderElement = overlay;
    }

    show() {
        if (this.loaderElement) {
            this.loaderElement.style.display = 'flex';
        }
    }

    hide() {
        if (this.loaderElement) {
            this.loaderElement.style.display = 'none';
        }
    }
}

// Create global loader instance
const loaderService = new LoaderService();

// Intercept all fetch requests to show/hide loader
const originalFetch = window.fetch;
window.fetch = async function(...args) {
    try {
        loaderService.show();
        const response = await originalFetch.apply(this, args);
        return response;
    } finally {
        loaderService.hide();
    }
};

// Add error handling to hide loader
window.addEventListener('error', function(event) {
    loaderService.hide();
});
