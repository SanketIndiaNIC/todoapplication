package com.example.todoapp.service;

import com.example.todoapp.model.CollaborativeTodo;
import com.example.todoapp.model.Todo;
import com.example.todoapp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public void sendCollaborativeTaskUpdate(CollaborativeTodo todo, String updateType, String message) {
        String emailContent = buildEmailContent(
            updateType,
            todo.getTitle(),
            message,
            todo.getReminderDateTime() != null ? todo.getReminderDateTime().format(formatter) : null,
            todo.getReminderMessage()
        );

        // Send to creator
        sendHtmlEmail(todo.getCreator().getEmail(), 
                     updateType + ": " + todo.getTitle(), 
                     emailContent);
        
        // Send to collaborators
        for (User collaborator : todo.getCollaborators()) {
            sendHtmlEmail(collaborator.getEmail(), 
                         updateType + ": " + todo.getTitle(), 
                         emailContent);
        }
    }

    public void sendCollaborationNotification(CollaborativeTodo todo, User collaborator) {
        String message = String.format("Hello %s,\n\nYou have been added as a collaborator to this task by %s.", 
            collaborator.getUsername(), todo.getCreator().getUsername());
        
        String emailContent = buildEmailContent(
            "New Collaboration Invitation",
            todo.getTitle(),
            message,
            todo.getReminderDateTime() != null ? todo.getReminderDateTime().format(formatter) : null,
            todo.getReminderMessage()
        );

        sendHtmlEmail(collaborator.getEmail(), 
                     "New Collaborative Task: " + todo.getTitle(), 
                     emailContent);
    }

    public void sendReminderEmail(Todo todo) {
        String message = String.format("This is a reminder for your task due at %s", 
            todo.getReminderDateTime().format(formatter));
            
        if (todo.getReminderFrequency() != null && todo.getReminderFrequency() != Todo.ReminderFrequency.ONCE) {
            message += String.format("\nThis is a %s reminder.", 
                todo.getReminderFrequency().toString().toLowerCase());
        }

        String emailContent = buildEmailContent(
            "Task Reminder",
            todo.getTitle(),
            message,
            todo.getReminderDateTime().format(formatter),
            null
        );

        sendHtmlEmail(todo.getUserEmail(), 
                     "Task Reminder: " + todo.getTitle(), 
                     emailContent);
    }

    private String buildEmailContent(String title, String taskTitle, String message, String reminderDate, String reminderMessage) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<meta charset='UTF-8'>")
            .append("<style>")
            .append("body { margin: 0; padding: 0; background-color: #f4f4f4; font-family: Arial, sans-serif; }")
            .append(".email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }")
            .append(".header { background-color: #0066cc; color: #ffffff; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }")
            .append(".header h1 { margin: 0; font-size: 24px; }")
            .append(".content { padding: 30px; color: #333333; }")
            .append(".task-title { font-size: 20px; color: #0066cc; margin: 0 0 20px 0; }")
            .append(".message { line-height: 1.6; margin-bottom: 20px; }")
            .append(".reminder-box { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }")
            .append(".reminder-box p { margin: 5px 0; }")
            .append(".button { display: inline-block; background-color: #0066cc; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 5px; margin: 20px 0; }")
            .append(".button:hover { background-color: #0052a3; }")
            .append(".footer { text-align: center; padding: 20px; color: #666666; font-size: 12px; border-top: 1px solid #eeeeee; }")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class='email-container'>")
            .append("<div class='header'>")
            .append("<h1>").append(title).append("</h1>")
            .append("</div>")
            .append("<div class='content'>")
            .append("<h2 class='task-title'>").append(taskTitle).append("</h2>")
            .append("<div class='message'>").append(message.replace("\n", "<br>")).append("</div>");

        if (reminderDate != null) {
            html.append("<div class='reminder-box'>")
                .append("<p><strong>Reminder Date:</strong> ").append(reminderDate).append("</p>");
            if (reminderMessage != null && !reminderMessage.isEmpty()) {
                html.append("<p><strong>Note:</strong> ").append(reminderMessage).append("</p>");
            }
            html.append("</div>");
        }

//        html.append("<a href='http://localhost:8080' class='button'>View Task</a>")
        html.append("</div>")
            .append("<div class='footer'>")
            .append("<p>This is an automated message from Todo Application. Please do not reply to this email.</p>")
            .append("<p> 2025 Todo Application. All rights reserved.</p>")
            .append("</div>")
            .append("</div>")
            .append("</body>")
            .append("</html>");

        return html.toString();
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("remainder.todoapp@gmail.com");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
