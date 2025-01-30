const modalOverlay = document.getElementById('modalOverlay');
const commentForm = document.getElementById('commentForm');
const postForm = document.getElementById('postForm');
const likes = document.getElementById('likes');

const addComment = document.getElementById('addComment');
const editPost = document.getElementById('editPost');
const modalPost = document.getElementById("modalPost");
const modalComment = document.getElementById("modalComment");

const tagsContainer = document.getElementById('tags');


likes.addEventListener('click', () => {
   const postId = document.getElementById('postId').textContent;
   fetch(`${postId}/like`, {
       method: 'POST'
     })
       .then(response => {
         if (!response.ok) {
           throw new Error(`Error: ${response.statusText}`);
         }
       })
       .then(data => {
         window.location.href = window.location.href;
       })
       .catch(error => {
         console.error('Error:', error);
         alert('Like post error. See JS console');
       });
 });


addComment.addEventListener('click', () => {
  modalOverlay.style.display = 'flex';
  modalComment.style.display = 'block';
  document.body.classList.add('modal-open');
});


fillPostForm = () => {
  const actualTags = Array.from(document.querySelectorAll('#actualTags .tag')).map(tag => tag.textContent.trim());

  document.querySelectorAll('#tags .tag').forEach(tag => {
    const tagValue = tag.getAttribute('data-tag');

    if (actualTags.includes(tagValue)) {
      tag.classList.add('selected');
    }
  });

  const actualTitle = document.querySelector('#actualTitle h2').textContent.trim();
  document.querySelector('#title').value = actualTitle;

  const actualText = Array.from(document.querySelectorAll('#actualText'))  // ('#actualText p')
  .map(paragraph => paragraph.textContent.trim())
  .join('\n');
  document.querySelector('#postFormText').value = actualText;
}

editPost.addEventListener('click', () => {
  modalOverlay.style.display = 'flex';
  modalPost.style.display = 'block';
  document.body.classList.add('modal-open');

  fillPostForm();
});


const closeModal = () => {
  modalPost.style.display = 'none';
  modalComment.style.display = 'none';
  modalOverlay.style.display = 'none';
  document.body.classList.remove('modal-open');
  postForm.reset();
};


modalOverlay.addEventListener('click', (event) => {
  if (event.target === modalOverlay) {
    closeModal();
  }
});


commentForm.addEventListener('submit', (event) => {

  const postId = document.getElementById('postId').textContent;
  const formData = new FormData(commentForm);

  fetch(`${postId}/comments`, {
    method: 'POST',
    body: formData,
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`Error: ${response.statusText}`);
      }
    })
    .then(data => {
      closeModal();
      postForm.reset();

      window.location.href = window.location.href;
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Adding comment error. See JS console');
    });

  event.preventDefault();
});




tagsContainer.addEventListener('click', (event) => {
  const tag = event.target;
  if (tag.classList.contains('tag')) {
    tag.classList.toggle('selected');
  }
});


postForm.addEventListener('submit', (event) => {

  const selectedTags = Array.from(
    tagsContainer.querySelectorAll('#tags .tag.selected')
  ).map(tag => tag.dataset.tag);

  const formData = new FormData(postForm);

  selectedTags.forEach(tag => formData.append('tags', tag));

  fetch(postForm.action, {
    method: 'PATCH',
    body: formData,
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`Error: ${response.statusText}`);
      }
    })
    .then(data => {
      closeModal();
      postForm.reset();

      window.location.href = window.location.href;
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Post updating error. See JS console');
    });

  event.preventDefault();
});



document.querySelectorAll('.editable').forEach(div => {
  div.addEventListener('click', () => {
    const comment = div.closest('.comment');
    const commentId = comment.querySelector('.comment-id').textContent.trim();

    const text = div.textContent;
    // const text = div.textContent.trim();

    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.height = `${div.offsetHeight}px`;
    textarea.style.width = `${div.offsetWidth}px`;
    textarea.addEventListener('keydown', async (event) => {
      if (event.ctrlKey && event.key === 'Enter') {
        event.preventDefault();

        const newValue = textarea.value;

        try {
          const response = await fetch('/your-api-endpoint', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, text: newValue }),
          });

          if (!response.ok) {
            throw new Error(`Failed to update text. Status: ${response.status}`);
          }

          div.textContent = newValue;
          textarea.replaceWith(div);
        } catch (error) {
          console.error('Error:', error);
          alert('Failed to save changes.');
        }
      }
    });

    div.replaceWith(textarea);
    textarea.focus();
  });
});
