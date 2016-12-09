package com.otwtag.janalyze

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import org.apache.commons.lang.StringUtils
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
    var packages = mutableListOf<String>()
    var classes = mutableListOf<String>()
    val relations = mutableListOf<String>()

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

    /**
     * Builds a Neo4j graph data creation
     */
    fun buildGraphString() {
        for (file in parsedFiles) {
            // add package
            val currentPackage = file.`package`.name.toStringWithoutComments()
            if(!packages.contains(currentPackage)) {

                packages.add(currentPackage)
            }

            // add imports
            for (imp in file.imports) {
                var addRelationship = false
                val importName = imp.name.toStringWithoutComments()
                val importPackage = if (importName.contains('.')) {
                    importName.substring(0..(importName.lastIndexOf('.') - 1))
                } else {
                    importName
                }
                val importClass = if (importName.contains('.')) {
                    importName.substring(importName.lastIndexOf('.') + 1)
                } else {
                    // case of package Name = class name...
                    // differentiate with capitalization
                    StringUtils.capitalize(importName)
                }
                if(!packages.contains(importPackage)) {

                    packages.add(importPackage)
                }
                if(!classes.contains(importClass)) {

                    classes.add(importClass)
                    addRelationship = true
                }
                if(addRelationship) {
                    relations.add("CREATE (${importPackage.replace(".","")})-[:CONTAINS]->($importClass)")
                }
            }

            for (clazz in file.types) {
                val className = clazz.nameExpr.toStringWithoutComments()
                if(!classes.contains(className)) {
                    classes.add(className)
                    relations.add("CREATE (${currentPackage.replace(".", "")})-[:CONTAINS]->($className)")
                }
            }
        }
    }
}