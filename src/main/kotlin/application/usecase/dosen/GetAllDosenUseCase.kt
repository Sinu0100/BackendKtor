package application.usecase.dosen

import domain.model.Dosen
import domain.repository.DosenRepository

class GetAllDosenUseCase(private val repository: DosenRepository) {
    suspend fun execute(): List<Dosen> {
        return repository.getAll()
    }
}
