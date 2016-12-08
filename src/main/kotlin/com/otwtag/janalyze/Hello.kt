package com.otwtag.janalyze

 const val path: String = "D:\\Github\\kotlin\\fakefiles\\src\\main\\java"
//const val path: String = "C:\\Projets\\kotlin\\fakefiles\\src\\main\\java\\helpers"

fun main(args: Array<String>) {
    try {
        val browser = CodeBrowser(path)
        browser.parseFilesInPath()
        browser.buildGraphString()

    } catch (e: Exception) {
        println("An error occurred while parsing files at $path")
        e.printStackTrace()
    }
}