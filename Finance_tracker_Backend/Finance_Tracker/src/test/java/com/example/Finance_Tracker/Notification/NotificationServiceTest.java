package com.example.Finance_Tracker.Notification;

import com.example.Finance_Tracker.Core.exception.ResourceNotFoundException;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Notification.dto.NotificationDTO;
import com.example.Finance_Tracker.Notification.entity.Notification;
import com.example.Finance_Tracker.Notification.repository.NotificationRepository;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import com.example.Finance_Tracker.Notification.util.NotificationType;
import com.example.Finance_Tracker.Settings.service.UserSettingService;
import com.example.Finance_Tracker.Settings.util.SettingKey;
import com.example.Finance_Tracker.User.mapper.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final long CURRENT_USER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;
    private static final long NOTIFICATION_ID = 10L;

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserSettingService userSettingService;

    @InjectMocks private NotificationService notificationService;

    @BeforeEach
    void loginAsCurrentUser() {
        CustomUserDetails principal = new CustomUserDetails(CURRENT_USER_ID, "me@example.com", "hash", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static Notification notificationOwnedBy(long userId) {
        return Notification.builder()
                .id(NOTIFICATION_ID)
                .userId(userId)
                .title("High Value Expense")
                .message("You made a high-value expense")
                .type(NotificationType.WARNING)
                .read(false)
                .archived(false)
                .build();
    }

    @Test
    void markAsRead_ownNotification_marksReadAndSaves() {
        Notification own = notificationOwnedBy(CURRENT_USER_ID);
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(own));

        notificationService.markAsRead(NOTIFICATION_ID);

        assertThat(own.isRead()).isTrue();
        verify(notificationRepository).save(own);
    }

    @Test
    void markAsRead_otherUsersNotification_throwsAccessDeniedAndDoesNotSave() {
        Notification foreign = notificationOwnedBy(OTHER_USER_ID);
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> notificationService.markAsRead(NOTIFICATION_ID))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(foreign.isRead()).isFalse();
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_missingNotification_throwsNotFound() {
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(NOTIFICATION_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteNotification_otherUsersNotification_throwsAccessDeniedAndDoesNotDelete() {
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notificationOwnedBy(OTHER_USER_ID)));

        assertThatThrownBy(() -> notificationService.deleteNotification(NOTIFICATION_ID))
                .isInstanceOf(AccessDeniedException.class);
        verify(notificationRepository, never()).delete(any());
    }

    @Test
    void archiveNotification_ownNotification_archivesAndSaves() {
        Notification own = notificationOwnedBy(CURRENT_USER_ID);
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(own));

        notificationService.archiveNotification(NOTIFICATION_ID);

        assertThat(own.isArchived()).isTrue();
        verify(notificationRepository).save(own);
    }

    @Test
    void getNotifications_returnsInboxWithoutArchived() {
        when(notificationRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(CURRENT_USER_ID))
                .thenReturn(List.of(notificationOwnedBy(CURRENT_USER_ID)));

        List<NotificationDTO> inbox = notificationService.getNotificationsByUser();

        assertThat(inbox).hasSize(1);
        assertThat(inbox.get(0).getTitle()).isEqualTo("High Value Expense");
    }

    @Test
    void getUnreadCount_excludesArchived() {
        when(notificationRepository.countByUserIdAndReadFalseAndArchivedFalse(CURRENT_USER_ID)).thenReturn(3L);

        assertThat(notificationService.getUnreadCount()).isEqualTo(3L);
    }

    @Test
    void createNotificationForUser_whenEnabled_saves() {
        when(userSettingService.getBooleanForUser(OTHER_USER_ID, SettingKey.NOTIFICATIONS_ENABLED)).thenReturn(true);
        when(userSettingService.getBooleanForUser(OTHER_USER_ID, SettingKey.NOTIFY_SPENDING_ALERTS)).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification saved = notificationService.createNotificationForUser(
                OTHER_USER_ID, dto(SettingKey.NOTIFY_SPENDING_ALERTS));

        assertThat(saved).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(OTHER_USER_ID);
    }

    @Test
    void createNotificationForUser_mutedByPreference_savesNothing() {
        when(userSettingService.getBooleanForUser(OTHER_USER_ID, SettingKey.NOTIFICATIONS_ENABLED)).thenReturn(true);
        when(userSettingService.getBooleanForUser(OTHER_USER_ID, SettingKey.NOTIFY_SPENDING_ALERTS)).thenReturn(false);

        Notification saved = notificationService.createNotificationForUser(
                OTHER_USER_ID, dto(SettingKey.NOTIFY_SPENDING_ALERTS));

        assertThat(saved).isNull();
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createNotificationForUser_masterSwitchOff_mutesEvenWithoutPreference() {
        when(userSettingService.getBooleanForUser(OTHER_USER_ID, SettingKey.NOTIFICATIONS_ENABLED)).thenReturn(false);

        Notification saved = notificationService.createNotificationForUser(OTHER_USER_ID, dto(null));

        assertThat(saved).isNull();
        verify(notificationRepository, never()).save(any());
    }

    private static CreateNotificationDTO dto(SettingKey preference) {
        CreateNotificationDTO dto = new CreateNotificationDTO();
        dto.setTitle("High Value Expense");
        dto.setMessage("You made a high-value expense");
        dto.setType(NotificationType.WARNING);
        dto.setPreference(preference);
        return dto;
    }
}
