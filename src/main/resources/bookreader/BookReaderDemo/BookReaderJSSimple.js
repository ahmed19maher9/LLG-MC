//
// This file shows the minimum you need to provide to BookReader to display a book
//
// Copyright(c)2008-2009 Internet Archive. Software license AGPL version 3.

// Create the BookReader object
function instantiateBookReader(selector, extraOptions) {
  selector = selector || '#BookReader';
  extraOptions = extraOptions || {};
  const params = new URLSearchParams(window.location.search);
  const requestedBook = extraOptions.book || params.get('book');
  const targetEl = document.querySelector(selector);

  if (!requestedBook) {
    console.error('BookReader immersion mode requires a "book" query parameter');
    if (targetEl) {
      targetEl.innerHTML = '<div style="padding:24px;font-family:sans-serif;color:#f1f5f9;background:#1e293b;">'
        + '<h2>Missing book parameter</h2>'
        + '<p>Launch this viewer from the application after selecting a PDF.</p>'
        + '</div>';
    }
    return;
  }

  const folderName = decodeURIComponent(requestedBook.replace(/\+/g, ' '));
  if (!/^[-_a-zA-Z0-9]+$/.test(folderName)) {
    console.error('Invalid book folder requested:', folderName);
    if (targetEl) {
      targetEl.innerHTML = '<div style="padding:24px;font-family:sans-serif;color:#f1f5f9;background:#1e293b;">'
        + '<h2>Invalid book folder</h2>'
        + '<p>The requested book name contained unsupported characters.</p>'
        + '</div>';
    }
    return;
  }

  const manifestUrl = '../books/' + folderName + '/manifest.json?ts=' + Date.now();

  fetch(manifestUrl, { cache: 'no-store' })
    .then(response => {
      if (!response.ok && response.status !== 0) {
        throw new Error('Failed to load manifest: ' + response.status);
      }
      return response.text().then(text => {
        if (!text) {
          throw new Error('Manifest response was empty');
        }
        try {
          return JSON.parse(text);
        } catch (err) {
          throw new Error('Manifest was not valid JSON');
        }
      });
    })
    .then(manifest => {
      const opts = {
        ppi: 100,
        data: manifest.data || [],
        bookTitle: manifest.bookTitle || 'BookReader PDF',
        bookUrl: '',
        bookUrlText: '',
        bookUrlTitle: '',
        thumbnail: manifest.thumbnail || '',
        imagesBaseURL: '../BookReader/images/',
        ui: 'full',
        el: selector,
        startFullscreen: true,
        showInfo: false,
        showShare: false,
        showLogo: false,
      };

      if (!opts.data.length) {
        throw new Error('Manifest did not contain any page data');
      }

      $.extend(opts, extraOptions);
      const br = new BookReader(opts);
      br.init();
      window.llgBookReader = br;
    })
    .catch(error => {
      console.error('Unable to initialize BookReader from manifest', error);
      if (targetEl) {
        targetEl.innerHTML = '<div style="padding:24px;font-family:sans-serif;color:#f1f5f9;background:#1e293b;">'
          + '<h2>Error loading book</h2>'
          + '<p>' + (error && error.message ? error.message : 'Unknown error') + '</p>'
          + '<p>Check that the generated manifest.json exists under <code>bookreader/books/' + folderName + '/</code>.</p>'
          + '</div>';
      }
    });
}
