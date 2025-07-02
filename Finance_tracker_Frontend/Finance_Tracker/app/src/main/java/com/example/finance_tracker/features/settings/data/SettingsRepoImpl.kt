package com.example.finance_tracker.features.settings.data

import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.data.local.room.dao.UserSettingDao
import com.example.finance_tracker.core.data.local.room.mapper.UserSettingMapper
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.RetrofitInstance
import com.example.finance_tracker.core.network.apiendpoints.SettingsApi
import com.example.finance_tracker.core.network.model.settings.UpdateSettingDTO
import com.example.finance_tracker.core.network.model.settings.UserSettingDTO
import com.example.finance_tracker.features.settings.domain.SettingsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

class SettingsRepoImpl(
    private val tokenManager: TokenManager,
    private val userSettingDao: UserSettingDao
) : SettingsRepo {

    private val api: SettingsApi = RetrofitInstance.provideSettingsApi(tokenManager)

    override suspend fun getSettings(): NetworkResult<List<UserSettingDTO>> {
        return withContext(Dispatchers.IO) {
            val response = ApiResponseHandler.handleApi { api.getSettings() }

            if (response is NetworkResult.Success) {
                val entities = UserSettingMapper.fromDTOList(response.data)
                userSettingDao.insertOrReplaceAll(entities)
                response
            } else {
                val fallbackData = userSettingDao.getAllSettings().map {
                    UserSettingDTO(key = it.key, value = it.value)
                }

                if (fallbackData.isNotEmpty()) {
                    NetworkResult.Success(fallbackData)
                } else {
                    response
                }
            }
        }
    }

    override suspend fun updateSettings(settings: List<UpdateSettingDTO>): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.updateSettings(settings) }
    }

    override suspend fun resetToDefaults(): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.resetToDefaults() }
    }

    override suspend fun logout(): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi {
            val response = api.logout()
            if (response.isSuccessful) {
                tokenManager.clearTokens()
            }
            response
        }
    }

    override suspend fun deleteAccount(): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi {
            val response = api.deleteAccount()
            if (response.isSuccessful) {
                tokenManager.clearTokens()
            }
            response
        }
    }

    override suspend fun exportData(): NetworkResult<ByteArray> {
        return ApiResponseHandler.handleApi { api.exportData() }
    }

    override suspend fun importData(file: ByteArray, filename: String): NetworkResult<Unit> {
        val requestBody = file.toRequestBody("application/zip".toMediaTypeOrNull())
        val multipartFile = MultipartBody.Part.createFormData("file", filename, requestBody)
        return ApiResponseHandler.handleApi { api.importData(multipartFile) }
    }
}

