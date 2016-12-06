package com.otwtag.janalyze

const val path: String = "D:\\Github\\kotlin\\fakefiles\\src\\main\\java"

fun main(args: Array<String>) {
    try {
        val browser = CodeBrowser(path)
        browser.parseFilesInPath()

        browser.buildPackages()

        browser.buildGraph()
        browser.graph.display()

    } catch (e: Exception) {
        println("An error occurred while parsing files at $path")
        e.printStackTrace()
    }
}

