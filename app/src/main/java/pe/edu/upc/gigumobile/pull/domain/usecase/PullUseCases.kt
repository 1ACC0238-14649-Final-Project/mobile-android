package pe.edu.upc.gigumobile.pull.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.gigumobile.pull.domain.model.*
import pe.edu.upc.gigumobile.pull.domain.repository.PullRepository

class CreatePullUseCase(private val repo: PullRepository) { suspend operator fun invoke(pull: Pull) = repo.createPull(pull) }
class GetPullByIdUseCase(private val repo: PullRepository) { suspend operator fun invoke(id: String) = repo.getPullById(id) }
class ObserveMessagesUseCase(private val repo: PullRepository) { operator fun invoke(pullId: String): Flow<List<PullMessage>> = repo.observeMessages(pullId) }
class SendMessageUseCase(private val repo: PullRepository) { suspend operator fun invoke(message: PullMessage) = repo.sendMessage(message) }
class UpdatePullStatusUseCase(private val repo: PullRepository) { suspend operator fun invoke(pullId: String, status: PullStatus) = repo.updateStatus(pullId, status) }

data class PullUseCases(
    val create: CreatePullUseCase,
    val getById: GetPullByIdUseCase,
    val observeMessages: ObserveMessagesUseCase,
    val sendMessage: SendMessageUseCase,
    val updateStatus: UpdatePullStatusUseCase
)

object PullUseCasesFactory {
    fun from(repo: PullRepository) = PullUseCases(
        create = CreatePullUseCase(repo),
        getById = GetPullByIdUseCase(repo),
        observeMessages = ObserveMessagesUseCase(repo),
        sendMessage = SendMessageUseCase(repo),
        updateStatus = UpdatePullStatusUseCase(repo)
    )
}
