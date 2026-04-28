package plugins

import infrastructure.di.AppComponent
import io.ktor.server.application.*
import io.ktor.server.routing.*
import presentation.routes.*

fun Application.configureRouting(appComponent: AppComponent) {
    val authController = appComponent.controllerModule.authController
    val dosenController = appComponent.controllerModule.dosenController
    val statistikController = appComponent.controllerModule.statistikController
    val jadwalController = appComponent.controllerModule.jadwalController
    val fasilitasController = appComponent.controllerModule.fasilitasController
    val triDharmaController = appComponent.controllerModule.triDharmaController
    val penelitianController = appComponent.controllerModule.penelitianController
    val publikasiController = appComponent.controllerModule.publikasiController
    val pengabdianController = appComponent.controllerModule.pengabdianController
    val hkiController = appComponent.controllerModule.hkiController
    val bukuAjarController = appComponent.controllerModule.bukuAjarController
    val sertifikatController = appComponent.controllerModule.sertifikatController
    val keahlianController = appComponent.controllerModule.keahlianController
    val mediaController = appComponent.controllerModule.mediaController

    routing {
        route("/api/v1") {
            authRoutes(authController)
            dosenRoutes(dosenController)
            statistikRoutes(statistikController)
            jadwalRoutes(jadwalController)
            fasilitasRoutes(fasilitasController)
            triDharmaRoutes(triDharmaController) // Sekarang manggil dari presentation.routes.TriDharmaRoutes
            penelitianRoutes(penelitianController)
            publikasiRoutes(publikasiController)
            pengabdianRoutes(pengabdianController)
            hkiRoutes(hkiController)
            bukuAjarRoutes(bukuAjarController)
            sertifikatRoutes(sertifikatController)
            keahlianRoutes(keahlianController)
            mediaRoutes(mediaController)
        }
    }
}
