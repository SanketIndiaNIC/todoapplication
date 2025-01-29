# Todo Application

## Overview

A sophisticated task management system built with Spring Boot that enables both personal and collaborative task management. The application features a modern, responsive UI and focuses on real-time collaboration, efficient task organization, and timely notifications.

### Key Highlights
- **Dual Task Management**: Manage both personal and collaborative tasks in a unified interface
- **Real-time Updates**: Instant UI updates using modern JavaScript for seamless user experience
- **Professional Email Notifications**: Beautifully designed HTML email notifications for task updates and reminders
- **Intuitive User Interface**: Clean, modern design with loading indicators and toast notifications

## Features

### Personal Task Management
- Create and manage individual tasks with titles and descriptions
- Set task status (pending/completed) with visual indicators
- Track task creation and modification dates
- Organize tasks with priority levels
- Filter and search tasks efficiently

### Collaborative Task System
- Create tasks with multiple collaborators
- Real-time task updates visible to all collaborators
- Add or remove collaborators from existing tasks
- Track collaborator contributions and changes
- Email notifications for task assignments and updates

### Advanced Reminder System
- Set customizable reminders for any task
- Email notifications with professional HTML templates
- Flexible reminder scheduling
- One-click reminder removal
- Reminder modification options

### Enhanced User Interface
- Modern, responsive card-based design
- Real-time loading indicators during API calls
- Interactive toast notifications for all actions
- Drag-and-drop task organization
- Separate views for personal and collaborative tasks
- Mobile-friendly layout

### Security Features
- Secure user authentication and authorization
- CSRF protection for all requests
- Encrypted data transmission
- Protected API endpoints
- Secure email notification system

### Email Notification System
- Professional HTML email templates
- Task update notifications
- Reminder alerts
- Collaboration invites
- Task completion notifications
- Custom email preferences

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security for authentication
- Spring Data JPA for data persistence
- Spring Mail for email services
- MySQL Database (H2 for development)
- Maven for dependency management

### Frontend
- HTML5 with semantic markup
- CSS3 with modern flexbox/grid layouts
- JavaScript (ES6+) for dynamic features
- jQuery for DOM manipulation
- Select2 for enhanced dropdowns
- Font Awesome for icons
- Custom loader animations

## Setup and Installation

1. **Prerequisites**
   - Java 17 or higher
   - Maven 3.6+
   - MySQL 8.0+ (or H2 for development)
   - SMTP server for email notifications

2. **Clone the Repository**
   ```bash
   git clone [repository-url]
   cd Todo_Application
   ```

3. **Database Configuration**
   Update `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/todo_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

4. **Email Configuration**
   Configure email settings in `application.properties`:
   ```properties
   spring.mail.host=your-smtp-server
   spring.mail.port=587
   spring.mail.username=your-email
   spring.mail.password=your-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

5. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

6. **Access Application**
   - Open browser and navigate to `http://localhost:8080`
   - Default credentials:
     - Username: admin
     - Password: admin123

## API Endpoints

### Personal Tasks
- `GET /api/todos` - Retrieve all personal tasks
- `POST /api/todos` - Create new personal task
- `PUT /api/todos/{id}` - Update existing personal task
- `DELETE /api/todos/{id}` - Delete personal task
- `PATCH /api/todos/{id}/status` - Update task status

### Collaborative Tasks
- `GET /api/collaborative-todos` - List collaborative tasks
- `POST /api/collaborative-todos` - Create collaborative task
- `PUT /api/collaborative-todos/{id}` - Update collaborative task
- `DELETE /api/collaborative-todos/{id}` - Delete collaborative task
- `POST /api/collaborative-todos/{id}/collaborators` - Add collaborators
- `DELETE /api/collaborative-todos/{id}/collaborators/{userId}` - Remove collaborator

### Reminders
- `POST /api/reminders` - Create new reminder
- `GET /api/reminders` - List all reminders
- `PUT /api/reminders/{id}` - Update reminder
- `DELETE /api/reminders/{id}` - Remove reminder

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -m 'Add YourFeature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Boot team for the excellent framework
- Contributors who have helped improve the application
- Open source community for tools and libraries
- Users who provided valuable feedback
