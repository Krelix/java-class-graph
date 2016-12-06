package com.otwtag.janalyze

/**
 * Created by Adrien on 03/12/2016.
 */

class PackageDependencies(val name: String) {
    private var dependsUpon = mutableListOf<String>()

    /**
     * Returns a non-mutable list of dependencies
     */
    fun getDependsUpon() : List<String> {
        return this.dependsUpon.toList()
    }

    fun addDependency(dep : String) {
        if(!dependsUpon.contains(dep)) {
            this.dependsUpon.add(dep)
        }
    }

    fun addAll(deps: List<String>) {
        for(s in deps) {
            addDependency(s)
        }
    }
}