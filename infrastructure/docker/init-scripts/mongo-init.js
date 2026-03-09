// MongoDB Initialization Script

// Switch to notification_db
db = db.getSiblingDB('notification_db');

// Create collections
db.createCollection('notifications');
db.createCollection('notification_logs');

// Create indexes
db.notifications.createIndex({ "userId": 1 });
db.notifications.createIndex({ "eventType": 1 });
db.notifications.createIndex({ "createdAt": -1 });
db.notifications.createIndex({ "status": 1, "createdAt": -1 });

db.notification_logs.createIndex({ "notificationId": 1 });
db.notification_logs.createIndex({ "createdAt": -1 });

// Insert sample data (optional)
db.notifications.insertOne({
    userId: "user-sample",
    eventType: "SYSTEM_STARTUP",
    channel: "EMAIL",
    recipient: "admin@enterprise.com",
    subject: "System Initialized",
    message: "Enterprise Order Management System databases initialized successfully",
    status: "SENT",
    createdAt: new Date(),
    sentAt: new Date()
});

print("MongoDB initialization completed successfully!");
print("Database: notification_db");
print("Collections: notifications, notification_logs");
print("Indexes created for performance optimization");
