package com.example.finance_tracker.features.settings.data

import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.data.local.room.dao.UserSettingDao
import com.example.finance_tracker.core.data.local.room.mapper.UserSettingMapper
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.apiendpoints.SettingsApi
import com.example.finance_tracker.core.network.model.settings.ImportSummaryDTO
import com.example.finance_tracker.core.network.model.settings.UpdateSettingDTO
import com.example.finance_tracker.core.network.model.settings.UserSettingDTO
import com.example.finance_tracker.features.settings.domain.SettingsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

class SettingsRepoImpl(
    private val api: SettingsApi,
    private val tokenManager: TokenManager,
    private val userSettingDao: UserSettingDao
) : SettingsRepo {

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
        val result = ApiResponseHandler.handleApi { api.logout() }
        // Log out locally even when the server call fails (expired token, offline); otherwise the
        // user is stuck logged in. The server-side blacklist is best effort.
        tokenManager.clearTokens()
        return result
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

    override suspend fun exportData(): NetworkResult<ByteArray> = withContext(Dispatchers.IO) {
        when (val result = ApiResponseHandler.handleApi { api.exportData() }) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.use { it.bytes() })
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    override suspend fun importData(file: ByteArray, filename: String): NetworkResult<ImportSummaryDTO> {
        val requestBody = file.toRequestBody("application/zip".toMediaTypeOrNull())
        val multipartFile = MultipartBody.Part.createFormData("file", filename, requestBody)
        return ApiResponseHandler.handleApi { api.importData(multipartFile) }
    }
}

