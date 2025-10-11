package pe.edu.upc.gigumobile.pull.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.gigumobile.pull.domain.model.*

interface PullRepository {
    suspend fun createPull(pull: Pull): Pull
    suspend fun getPullById(id: String): Pull?
    fun observeMessages(pullId: String): Flow<List<PullMessage>>
    suspend fun sendMessage(message: PullMessage)
    suspend fun updateStatus(pullId: String, status: PullStatus): Pull
}