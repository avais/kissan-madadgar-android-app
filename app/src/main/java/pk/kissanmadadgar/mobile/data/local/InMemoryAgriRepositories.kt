package pk.kissanmadadgar.mobile.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pk.kissanmadadgar.mobile.domain.model.*
import pk.kissanmadadgar.mobile.domain.repository.*

class InMemoryMachineryRepository : MachineryRepository {
    private val machineryListState = MutableStateFlow<List<Machinery>>(emptyList())

    override fun getCategories(): Flow<List<Category>> = MutableStateFlow(emptyList())

    override fun getAvailableMachinery(): Flow<List<Machinery>> {
        return machineryListState.map { list ->
            list.filter { it.isAvailable && it.status == MachineryStatus.APPROVED }
        }
    }

    override fun getMachineryByProvider(providerId: String): Flow<List<Machinery>> {
        return machineryListState.map { list ->
            list.filter { it.providerId == providerId }
        }
    }

    override suspend fun getMachineryById(id: String): Machinery? {
        return machineryListState.value.find { it.id == id }
    }

    override suspend fun addMachinery(machinery: Machinery): Result<Unit> {
        val current = machineryListState.value.toMutableList()
        current.add(machinery)
        machineryListState.value = current
        return Result.success(Unit)
    }

    override suspend fun updateMachineryStatus(id: String, status: MachineryStatus): Result<Unit> {
        val current = machineryListState.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(status = status)
            machineryListState.value = current
            return Result.success(Unit)
        }
        return Result.failure(Exception("مشینری نہیں ملی"))
    }

    override fun getAllMachineryAdmin(): Flow<List<Machinery>> {
        return machineryListState
    }
}

