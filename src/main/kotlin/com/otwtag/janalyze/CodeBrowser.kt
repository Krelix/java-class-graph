package com.otwtag.janalyze

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.graph.Node
import org.graphstream.graph.Edge
import java.io.File
import java.io.FileInputStream

/**
 * Returns a list of Files in the given directory that have the java extension
 * @return a list of files with the java extension
 */
private fun getJavaSourceInDir(dir: File): Array<File> {
    assert(dir.isDirectory)
    return dir.listFiles { file -> file.extension == "java" }
}

/**
 * Returns a list of Files that are directories in the passed directory
 * @return a list of files containing sub directories
 */
private fun getDirsInDir(dir: File): Array<File> {
    assert(dir.isDirectory)
    return dir.listFiles { file -> file.isDirectory }
}

private fun getJavaSourceInDirRecurse(dir: File): Array<File> {
    assert(dir.isDirectory)
    var result = emptyArray<File>()
    for (subDir in getDirsInDir(dir)) {
        result += getJavaSourceInDirRecurse(subDir)
    }
    result += getJavaSourceInDir(dir)
    return result
}

/**
 * Created by Adrien on 02/12/2016.
 */
class CodeBrowser(val filePath: String) {
    var parsedFiles = mutableListOf<CompilationUnit>()
    var edges = mutableMapOf<String, MutableList<String>>()
    var graph = SingleGraph("test", false, true)

    fun parseFilesInPath() {
        val baseDir = File(filePath)
        val filesToParse = getJavaSourceInDirRecurse(baseDir)
        println("Found ${filesToParse.size} files to parse")

        for (file in filesToParse) {
            try {
                parsedFiles.add(JavaParser.parse(FileInputStream(file)))
            } catch(e: Exception) {
                println("=====================================================================================")
                e.printStackTrace()
                error("Error occurred when reading file ${file.name}")
            }
        }
    }

    fun buildPackages() {
        assert(!parsedFiles.isEmpty())
        for (cu in parsedFiles) {
            val packageName = cu.`package`.name.toString().trim()
            cu.types.map { type ->
                var currNode = "$packageName.${type.name.toString().trim()}"
                var imports = mutableListOf<String>()
                cu.imports.map { import -> if(!imports.contains(import.name.toString().trim())) imports.add(import.name.toString().trim())}
                edges.put(currNode, imports)
            }
        }
    }

    fun buildGraph() {
        for((key, value) in edges) {
            var currNode = graph.addNode<Node>(key)
            currNode.setAttribute("ui.label", key)
            value.map{ s ->
                println("creating edge for $key and $s")
                graph.addEdge<Edge>("$key$s", key, s) }
        }
    }
}