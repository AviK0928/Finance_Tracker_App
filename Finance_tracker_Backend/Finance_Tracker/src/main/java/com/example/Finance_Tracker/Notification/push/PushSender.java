package com.example.Finance_Tracker.Notification.push;

/** Sends one push to one device. Behind an interface so tests and servers without Firebase need no credentials. */
public interface PushSender {

    PushResult send(String deviceToken);
}
