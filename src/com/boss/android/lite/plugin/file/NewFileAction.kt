package com.boss.android.lite.plugin.file

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys.IDE_VIEW
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages.getInformationIcon
import com.intellij.openapi.ui.Messages.showMessageDialog
import com.intellij.psi.JavaDirectoryService
import com.boss.android.lite.plugin.utils.LiteConfig.Companion.instance


class NewFileAction : AnAction() {

    override fun update(event: AnActionEvent) {
        super.update(event)
        val project = event.getData(PlatformDataKeys.PROJECT)
        val presentation = event.presentation

        if(project  == null){
            presentation.isEnabledAndVisible = false
            return
        }
        val ideView = event.getData(IDE_VIEW)
        val psiDirectory = ideView?.orChooseDirectory
        if(psiDirectory == null){
            presentation.isEnabledAndVisible = false
            return
        }
        val psiPackage =  JavaDirectoryService.getInstance().getPackage(psiDirectory)
        if (psiPackage == null) {
            presentation.isEnabledAndVisible = false
            return
        }
        presentation.isEnabledAndVisible = true
    }
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)

        if(project  == null){
            showMessageDialog(project, "Lite : not find project.", "Lite", getInformationIcon())
            return
        }

        val ideView = event.getData(IDE_VIEW)

        val psiDirectory = ideView?.orChooseDirectory
        if(psiDirectory  == null){
            showMessageDialog(project, "Lite : not find directory.", "Lite", getInformationIcon())
            return
        }

        val psiPackage =  JavaDirectoryService.getInstance().getPackage(psiDirectory)

        val packageName = psiPackage?.qualifiedName
        if(packageName  == null){
            showMessageDialog(project, "Lite : not find package name.", "Lite", getInformationIcon())
            return
        }
        instance.init(project)

        val dialog = LiteFileDialog()


        dialog.setBasicInfo(project,psiDirectory,packageName)
        dialog.pack()
        dialog.setLocationRelativeTo(null)
        dialog.isVisible = true
        dialog.requestFocus()

    }

}