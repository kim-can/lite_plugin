package com.boss.android.lite.plugin.file

import com.android.tools.idea.projectsystem.getModuleSystem
import com.android.tools.idea.projectsystem.sourceProviders
import com.boss.android.lite.plugin.utils.LiteConfig.Companion.instance
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys.IDE_VIEW
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.Messages.getInformationIcon
import com.intellij.openapi.ui.Messages.showMessageDialog
import com.intellij.psi.JavaDirectoryService
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import org.jetbrains.android.dom.manifest.Manifest
import org.jetbrains.android.dom.manifest.getPrimaryManifestXml
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.android.facet.AndroidRootUtil
import org.jetbrains.android.facet.ResourceFolderManager
import org.jetbrains.android.util.AndroidUtils
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory


class NewFileAction : AnAction() {

    private fun parentDirectory(mProject: Project, psiDirectory: PsiDirectory?): PsiDirectory? {
        if ((psiDirectory?.name == "main") && psiDirectory.parentDirectory?.name == "src") {
            val directory = psiDirectory.findSubdirectory("res")
            if(directory != null) {
                return directory.findSubdirectory("layout") ?: return directory.createSubdirectory("layout")
            }
        }
        if (psiDirectory?.name == mProject.name) {
            return null
        }

        return parentDirectory(mProject=mProject,psiDirectory = psiDirectory?.parentDirectory)
    }

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

        val project = event.getData(PlatformDataKeys.PROJECT) ?: return

        val isAndroid = AndroidUtils.getInstance().isAndroidProject(project)

        if(!isAndroid){
            showMessageDialog(project, "Lite : is not android project", "Lite", getInformationIcon())
            return
        }

        val module = LangDataKeys.MODULE.getData(event.dataContext) ?: return


        //找到目录
        val ideView = event.getData(IDE_VIEW)
        val psiDirectory = ideView?.orChooseDirectory ?: return
        //找到文件包名
        val psiPackage =  JavaDirectoryService.getInstance().getPackage(psiDirectory)
        val filePackageName = psiPackage?.qualifiedName
        if(filePackageName  == null){
            showMessageDialog(project, "Lite : not find package name.", "Lite", getInformationIcon())
            return
        }

        //找到 manifest 包名
         val facet = AndroidFacet.getInstance(module)
        if(facet  == null){
            showMessageDialog(project, "Lite : not android project.", "Lite", getInformationIcon())
            return
        }
        val manifestFile = AndroidRootUtil.getPrimaryManifestFile(facet)

        if(manifestFile == null){
            showMessageDialog(project, "Lite : not fond manifestFile in project.", "Lite", getInformationIcon())
            return
        }

        val manifest =  facet.getPrimaryManifestXml()

        if(manifest == null){
            showMessageDialog(project, "Lite : not fond manifestFile in project.", "Lite", getInformationIcon())
            return
        }
        val xmlPackageName = manifest.packageName
        if(xmlPackageName == null){
            showMessageDialog(project, "Lite : not fond manifestFile package name.", "Lite", getInformationIcon())
            return
        }

        val layoutDirectory =  parentDirectory(mProject = project,manifest.parent)

        instance.init(project)
        val dialog = LiteFileDialog()
        dialog.setBasicInfo(project,psiDirectory,layoutDirectory,filePackageName,xmlPackageName)
        dialog.pack()
        dialog.setLocationRelativeTo(null)
        dialog.isVisible = true
        dialog.requestFocus()

    }

}