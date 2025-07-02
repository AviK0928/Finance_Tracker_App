package com.example.finance_tracker.core.data.local.room.mapper

import com.example.finance_tracker.core.data.local.room.entity.UserSettingEntity
import com.example.finance_tracker.core.network.model.settings.UserSettingDTO

object UserSettingMapper {
    fun fromDTO(dto: UserSettingDTO): UserSettingEntity {
        return UserSettingEntity(
            key = dto.key,
            value = dto.value
        )
    }

    fun fromDTOList(dtos: List<UserSettingDTO>): List<UserSettingEntity> {
        return dtos.map { fromDTO(it) }
    }
}
