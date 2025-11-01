package com.alexandr44.telegramprinterbot.service

import com.alexandr44.telegramprinterbot.PageLayout
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.deleteIfExists


@Service
class PrintServiceImpl : PrintService {

    @Value("\${telegrambots.print_script_path}")
    private lateinit var printScriptPath: String

    override fun printDocument(fileUrl: String, fileName: String, pageLayout: PageLayout) {
        val printerName = System.getenv("PRINTER_NAME")
        val dest: Path = Paths.get("/tmp", fileName)
        URI(fileUrl).toURL().openStream().use { `in` ->
            Files.copy(`in`, dest, StandardCopyOption.REPLACE_EXISTING)
        }

        // Запуск скрипта печати
        val pb = ProcessBuilder(printScriptPath, dest.toString(), printerName, pageLayout.pages.toString())
        pb.inheritIO()
        val p = pb.start()
        p.waitFor()

        dest.deleteIfExists()
    }

}