package infrastructure.di

import io.ktor.server.application.*
import infrastructure.security.JwtService
import infrastructure.storage.StorageService

class AppComponent(environment: ApplicationEnvironment) {
    private val jwtService = JwtService(environment)
    private val storageService = StorageService()
    
    val repositoryModule = RepositoryModule()
    val useCaseModule = UseCaseModule(repositoryModule, jwtService, storageService)
    val controllerModule = ControllerModule(useCaseModule)
}
