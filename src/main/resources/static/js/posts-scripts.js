const defaultTagFilterValue = '';
const defaultPageSizeValue = 10;

const tagFilter = document.getElementById('tagFilter');
const pageSize = document.getElementById('pageSize');
const paginationElement = document.getElementById('pagination');
const modalOverlay = document.getElementById('modalOverlay');
const openModalButton = document.getElementById('openModal');
const postForm = document.getElementById('postForm');
const tagsContainer = document.getElementById('tags');

const params = new URLSearchParams(window.location.search);
const tagFilterValue = params.get('tag') || defaultTagFilterValue;
const pageSizeValue = parseInt(params.get('size')) || defaultPageSizeValue;

tagFilter.value = tagFilterValue;
pageSize.value = pageSizeValue;

tagFilter.addEventListener('change', () => {
  const newTagFilterValue = tagFilter.value;
  const currentUrl = new URL(window.location.href);
  const params = currentUrl.searchParams;

  params.set('page', 0); // Request first page if we changed tag filter
  if (newTagFilterValue === '') {
    params.delete('tag');
  } else {
    params.set('tag', newTagFilterValue);
  }

  window.location.href = currentUrl.toString();
});

pageSize.addEventListener('change', () => {
  const newPageSizeValue = pageSize.value;
  const currentUrl = new URL(window.location.href);
  const params = currentUrl.searchParams;
  params.set('size', newPageSizeValue);
  params.set('page', 0); // Request first page if we changed page size

  window.location.href = currentUrl.toString();
});


document.addEventListener('DOMContentLoaded', function () {
    const buttons = document.querySelectorAll('.page-button');

    buttons.forEach(button => {
        if (button.classList.contains('active')) {
            return;
        }

        button.addEventListener('click', function () {
            const page = this.dataset.page;
            const url = new URL(window.location.href);
            url.searchParams.set('page', page);
            window.location.href = url.toString();
        });
    });
});


openModalButton.addEventListener('click', () => {
  modalOverlay.style.display = 'flex';
  document.body.classList.add('modal-open'); // block scroll
});


const closeModal = () => {
  modalOverlay.style.display = 'none';
  document.body.classList.remove('modal-open'); // unblock scroll
  postForm.reset();
};


// Close modal window when clicking outside the modal window
modalOverlay.addEventListener('click', (event) => {
  if (event.target === modalOverlay) {
    closeModal();
  }
});


tagsContainer.addEventListener('click', (event) => {
  const tag = event.target;
  if (tag.classList.contains('tag')) {
    tag.classList.toggle('selected');
  }
});


postForm.addEventListener('submit', (event) => {
  const selectedTags = Array.from(
    tagsContainer.querySelectorAll('.tag.selected')
  ).map(tag => tag.dataset.tag);

  const formData = new FormData(postForm);

  // Add tags into FormData
  selectedTags.forEach(tag => formData.append('tags', tag));

  // Make POST request
  fetch(postForm.action, {
    method: 'POST',
    body: formData,
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`Error: ${response.statusText}`);
      }
      return response.json();
    })
    .then(data => {
      console.log('Success:', data);
      closeModal();
      postForm.reset();

      // Reload current page
      window.location.href = window.location.href;
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Post creation errror. See JS console');
    });

  event.preventDefault();
});