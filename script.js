// Function to validate email format
function isValidEmail(email) {
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailPattern.test(email);
}

// Function to validate password strength
function isValidPassword(password) {
    // At least 8 characters, 1 uppercase, 1 lowercase, 1 number, and 1 special character
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    return passwordPattern.test(password);
}

// Function to toggle password visibility
function togglePassword() {
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.querySelector('.password-toggle');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.classList.remove('fa-eye-slash');
        toggleIcon.classList.add('fa-eye');
    } else {
        passwordInput.type = 'password';
        toggleIcon.classList.remove('fa-eye');
        toggleIcon.classList.add('fa-eye-slash');
    }
}

// Function to validate form
function validateForm(event) {
    event.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const emailError = document.getElementById('emailError');
    const passwordError = document.getElementById('passwordError');
    let isValid = true;

    // Reset error messages
    emailError.textContent = '';
    passwordError.textContent = '';

    // Validate email
    if (!email) {
        emailError.textContent = 'Email is required';
        isValid = false;
    } else if (!isValidEmail(email)) {
        emailError.textContent = 'Please enter a valid email address';
        isValid = false;
    }

    // Validate password
    if (!password) {
        passwordError.textContent = 'Password is required';
        isValid = false;
    } else if (!isValidPassword(password)) {
        passwordError.textContent = 'Password must be at least 8 characters long and contain uppercase, lowercase, number and special character';
        isValid = false;
    }

    // If form is valid, handle form submission
    if (isValid) {
        const rememberMe = document.getElementById('rememberMe').checked;
        
        // Save email if remember me is checked
        if (rememberMe) {
            localStorage.setItem('rememberedEmail', email);
        } else {
            localStorage.removeItem('rememberedEmail');
        }

        // Here you would typically send the data to your server
        console.log('Form submitted successfully', {
            email,
            password,
            rememberMe
        });
        
        // For demo purposes, show success message
        alert('Login successful!');
    }

    return isValid;
}

// Function to get current username from meta tag
function getCurrentUsername() {
    const usernameMeta = document.querySelector('meta[name="current-username"]');
    return usernameMeta ? usernameMeta.content : null;
}

// Function to check if user is creator of task
function isTaskCreator(task) {
    const currentUsername = getCurrentUsername();
    return currentUsername && task.creator && task.creator.username === currentUsername;
}

// Function to create collaborative todo card
function createCollaborativeTodoElement(todo) {
    const currentUsername = getCurrentUsername();
    console.log('Current Username:', currentUsername);
    console.log('Todo Creator:', todo.creator);
    
    // Check if the current user is the creator
    const isCreator = todo.creator && currentUsername === todo.creator.username;
    console.log('Is Creator:', isCreator);
    
    const collaborators = todo.collaborators.map(user => 
        `<li>${user.username}</li>`
    ).join('');

    return `
        <div class="todo-card" data-todo-id="${todo.id}">
            <div class="todo-card-header">
                <span class="todo-status ${todo.completed ? 'completed' : 'pending'}">
                    ${todo.completed ? 'Completed' : 'Pending'}
                </span>
                <div class="todo-actions">
                    <button class="action-btn toggle-btn" onclick="toggleCollaborativeTodo(this)"
                            data-todo-id="${todo.id}"
                            title="${todo.completed ? 'Mark as Pending' : 'Mark as Completed'}">
                        <i class="fas ${todo.completed ? 'fa-times-circle' : 'fa-check-circle'}"></i>
                    </button>
                    ${isCreator ? `
                        <button class="action-btn edit-btn" onclick="openEditCollaborativeModal(${todo.id})" title="Edit Task">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="action-btn delete-btn" onclick="deleteCollaborativeTodo(${todo.id})" title="Delete Task">
                            <i class="fas fa-trash"></i>
                        </button>
                    ` : ''}
                </div>
            </div>
            <div class="todo-content">
                <h3 class="todo-title">${todo.title}</h3>
                <p class="todo-description">${todo.description}</p>
                <div class="todo-timestamps">
                    <small>Created: ${formatDate(todo.createdAt)}</small>
                    ${todo.completedAt ? `<small>Completed: ${formatDate(todo.completedAt)}</small>` : ''}
                </div>
                <div class="collaborative-users">
                    <small>Created by: ${todo.creator.username}</small>
                    <div class="collaborators">
                        <h4>Collaborators:</h4>
                        <ul>${collaborators}</ul>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// Function to open edit modal for collaborative task
async function openEditCollaborativeModal(id) {
    try {
        const response = await fetch(`/api/collaborative-todos/${id}`);
        if (!response.ok) {
            throw new Error('Failed to fetch task details');
        }
        
        const task = await response.json();
        
        // Check if current user is the creator
        if (!isTaskCreator(task)) {
            showToast('Only the creator can edit this task', 'error');
            return;
        }

        const editModal = document.getElementById('editCollaborativeTaskModal');
        document.getElementById('editTaskId').value = task.id;
        document.getElementById('editTaskTitle').value = task.title;
        document.getElementById('editTaskDescription').value = task.description;

        // Initialize Select2 for collaborators
        const select = $('#editCollaborators');
        select.empty();

        // Load available users and set selected collaborators
        const usersResponse = await fetch('/api/collaborative-todos/users');
        if (!usersResponse.ok) {
            throw new Error('Failed to load available users');
        }

        const users = await usersResponse.json();
        users.forEach(user => {
            const option = new Option(user.username, user.id, false, task.collaborators.some(c => c.id === user.id));
            select.append(option);
        });

        select.trigger('change');
        editModal.style.display = 'block';
    } catch (error) {
        console.error('Error opening edit modal:', error);
        showToast('Failed to open edit modal', 'error');
    }
}

// Function to update collaborative task
async function updateCollaborativeTask(event) {
    event.preventDefault();
    
    const taskId = document.getElementById('editTaskId').value;
    const title = document.getElementById('editTaskTitle').value.trim();
    const description = document.getElementById('editTaskDescription').value.trim();
    const collaboratorIds = $('#editCollaborators').val().map(Number);

    try {
        // First verify if user is still the creator
        const checkResponse = await fetch(`/api/collaborative-todos/${taskId}`);
        if (!checkResponse.ok) {
            throw new Error('Failed to verify task ownership');
        }
        
        const task = await checkResponse.json();
        if (!isTaskCreator(task)) {
            showToast('Only the creator can edit this task', 'error');
            return;
        }

        const response = await fetch(`/api/collaborative-todos/${taskId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({
                title,
                description,
                collaboratorIds
            })
        });

        if (!response.ok) {
            throw new Error('Failed to update task');
        }

        const updatedTask = await response.json();
        
        // Update the task in the UI
        const taskElement = document.querySelector(`[data-todo-id="${taskId}"]`);
        if (taskElement) {
            taskElement.outerHTML = createCollaborativeTodoElement(updatedTask);
        }

        // Close modal and show success message
        document.getElementById('editCollaborativeTaskModal').style.display = 'none';
        showToast('Task updated successfully', 'success');
    } catch (error) {
        console.error('Error updating task:', error);
        showToast('Failed to update task', 'error');
    }
}

// Function to delete collaborative todo
async function deleteCollaborativeTodo(todoId) {
    if (!confirm('Are you sure you want to delete this collaborative task? All collaborators will be notified.')) {
        return;
    }

    try {
        const response = await fetch(`/api/collaborative-todos/${todoId}`, {
            method: 'DELETE',
            headers: {
                [csrfHeader]: csrfToken
            }
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to delete collaborative task');
        }

        // Remove the todo card from UI
        const todoCard = document.querySelector(`.todo-card[data-todo-id="${todoId}"]`);
        if (todoCard) {
            todoCard.remove();
            showToast('Collaborative task deleted successfully', 'success');
            
            // Check if there are no more tasks
            const container = document.querySelector('.collaborative-todo-grid');
            if (container && !container.querySelector('.todo-card')) {
                container.innerHTML = `
                    <div class="empty-state">
                        <i class="fas fa-users"></i>
                        <p>No collaborative tasks found</p>
                    </div>
                `;
            }
        }
    } catch (error) {
        console.error('Error deleting collaborative task:', error);
        showToast(error.message, 'error');
    }
}

// Function to load collaborative tasks
async function loadCollaborativeTasks() {
    try {
        const response = await fetch('/api/collaborative-todos', {
            headers: {
                [csrfHeader]: csrfToken
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to load collaborative tasks');
        }
        
        const tasks = await response.json();
        const container = document.querySelector('.collaborative-todo-grid');
        if (!container) {
            console.error('Collaborative todo grid container not found');
            return;
        }
        
        container.innerHTML = ''; // Clear existing tasks
        
        if (tasks.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-users"></i>
                    <p>No collaborative tasks found</p>
                </div>
            `;
            return;
        }
        
        tasks.forEach(task => {
            container.insertAdjacentHTML('beforeend', createCollaborativeTodoElement(task));
        });
    } catch (error) {
        console.error('Error loading collaborative tasks:', error);
        showToast('Failed to load collaborative tasks', 'error');
    }
}

// Check for remembered email on page load
document.addEventListener('DOMContentLoaded', function() {
    const rememberedEmail = localStorage.getItem('rememberedEmail');
    if (rememberedEmail) {
        document.getElementById('email').value = rememberedEmail;
        document.getElementById('rememberMe').checked = true;
    }
    
    // Add click event listeners to edit icons
    document.querySelectorAll('.edit-task-btn').forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const taskId = e.target.closest('.task-card').dataset.taskId;
            openEditCollaborativeModal(taskId);
        });
    });
    
    // Add submit handler for edit form
    document.getElementById('editCollaborativeTaskForm').addEventListener('submit', updateCollaborativeTask);
    
    // Add close handler for edit modal
    document.querySelector('#editCollaborativeTaskModal .close').addEventListener('click', () => {
        document.getElementById('editCollaborativeTaskModal').style.display = 'none';
    });
    
    loadCollaborativeTasks(); // Load collaborative tasks on page load
});
