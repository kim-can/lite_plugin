package com.boss.android.lite.plugin.utils

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

class LiteConfig {
    companion object {
        val instance: LiteConfig by lazy { LiteConfig() }
    }

    var isPosition = 0
    var isXML = false
    var isJava = false

    fun init(mProject: Project){
        isPosition = PropertiesComponent.getInstance(mProject).getInt("lite_select_index", 0)
        isXML = PropertiesComponent.getInstance(mProject).getBoolean("lite_is_create_xml", false)
        isJava = PropertiesComponent.getInstance(mProject).getBoolean("lite_is_java_xml", false)
    }

    fun save(mProject: Project,isXML: Boolean, isJava: Boolean,position: Int) {
        PropertiesComponent.getInstance(mProject).setValue(
            "lite_is_create_xml", isXML.toString()
        )
        PropertiesComponent.getInstance(mProject).setValue(
            "lite_is_java_xml", isJava.toString()
        )
        PropertiesComponent.getInstance(mProject).setValue("lite_select_index", position.toString())
    }
}