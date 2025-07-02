package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.notification.NotificationDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {

    @GET("/api/notifications")
    suspend fun getNotifications(): Response<List<NotificationDTO>>

    @POST("/api/notifications/{id}/mark-as-read")
    suspend fun markAsRead(@Path("id") id: Long): Response<Unit>

    @POST("/api/notifications/mark-all-as-read")
    suspend fun markAllAsRead(): Response<Unit>

    @DELETE("/api/notifications/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>

    @POST("/api/notifications/delete")
    suspend fun deleteBulk(@Body ids: List<Long>): Response<Unit>

    @POST("/api/notifications/{id}/archive")
    suspend fun archive(@Path("id") id: Long): Response<Unit>

    @POST("/api/notifications/archive")
    suspend fun archiveBulk(@Body ids: List<Long>): Response<Unit>

    @GET("/api/notifications/unread/count")
    suspend fun getUnreadCount(): Response<Long>
}