package com.otwtag.janalyze

import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

//const val path: String = "/Github/kotlin/fakefiles/src/main/java"
//const val path: String = "/Projets/kotlin/fakefiles/src/main/java/helpers"
const val path = "fake_source/src/main/java"
const val resultFile = "build/output.n4j"

fun main(args: Array<String>) {
    val charset = Charset.forName("utf8")
    val browser = CodeBrowser(path)

    try {
        browser.parseFilesInPath()
        browser.buildGraphString()
    } catch (e: Exception) {
        println("An error occurred while parsing files at $path")
        e.printStackTrace()
    }

    var outFile = File(resultFile)
    if (outFile.exists()) {
        outFile.delete()
    }
    outFile.createNewFile()

    var outputFile = FileOutputStream(outFile)

    for (s in browser.packages) {
        outputFile.write("CREATE (${s.replace(".", "")}: Package{name:'$s'})".toByteArray(charset))
    }
    for (s in browser.classes) {
        outputFile.write("CREATE (${s.replace(".", "")}: Class{name:'$s'})".toByteArray(charset))
    }
    for (s in browser.relations) {
        outputFile.write(s.toByteArray(charset))
    }
}