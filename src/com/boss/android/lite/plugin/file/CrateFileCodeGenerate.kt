package com.boss.android.lite.plugin.file

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.boss.android.lite.plugin.utils.LiteConfig
import java.util.*


class CrateFileCodeGenerate(
    private val mProject: Project,
    private val psiDirectory: PsiDirectory,
    private val packageName: String,
    private val name: String,
    private val fileModel: FileListModel,
    private val isXML: Boolean,
    private val isJava: Boolean,
) {

    fun run(liteFileDialog: LiteFileDialog) {
        val className = name.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1);

        WriteCommandAction.runWriteCommandAction(mProject) {

            val result = if(isJava){
                createJavaFile(className=className)
            }else{
                createKotlinFile(className=className)
            }

            LiteConfig.instance.save(mProject, isXML,isJava, fileModel.type.ordinal)

            if (result) {
                liteFileDialog.onCancel()
            }
        }
    }


    private fun createJavaFile(className:String):Boolean {
        val result = when (fileModel.type) {
            FileType.Lite -> {
                createKotlinLite(className)
            }
            FileType.LiteActivity -> {
                createKotlinLite(className)

                val activityName = if (className.endsWith("activity")) {
                    className.replace("activity", "Activity")
                } else if (!className.endsWith("Activity")) {
                    className + "Activity"
                } else {
                    className
                }
                val xmlModel = if (isXML) {
                    createXML(className, "activity", activityName,fileModel.type)
                } else {
                    null
                }
                createLiteJavaActivity(activityName, className, xmlModel)
            }
            FileType.LiteFragment -> {
                createKotlinLite(className)

                val fragmentName = if (className.endsWith("fragment")) {
                    className.replace("fragment", "Fragment")
                } else if (!className.endsWith("Fragment")) {
                    className + "Fragment"
                } else {
                    className
                }
                val xmlModel = if (isXML) {
                    createXML(className, "fragment", fragmentName,fileModel.type)
                } else {
                    null
                }
                createLiteJavaFragment(fragmentName, className, xmlModel,"Fragment")
            }
            FileType.LiteDialogFragment -> {
                createKotlinLite(className)

                val fragmentName = if (className.endsWith("dialogFragment")) {
                    className.replace("dialogFragment", "DialogFragment")
                } else if (!className.endsWith("DialogFragment")) {
                    className + "DialogFragment"
                } else {
                    className
                }
                val xmlModel = if (isXML) {
                    createXML(className, "dialog_fragment", fragmentName,fileModel.type)
                } else {
                    null
                }
                createLiteJavaFragment(fragmentName, className, xmlModel,"DialogFragment")
            }
        }
        return result
    }

    private fun createLiteJavaActivity(activityName: String, liteName: String, xmlModel: XMLModel?) :Boolean {
        val defaultProperties = FileTemplateManager.getInstance(mProject).defaultProperties

        val viewTemplate = FileTemplateManager.getInstance(mProject).getInternalTemplate("LiteActivityJavaTemplate")
        val viewProperties = Properties(defaultProperties)
        viewProperties.setProperty("PACKAGE_NAME", "$packageName;")
        viewProperties.setProperty("NAME", activityName)
        viewProperties.setProperty("SUPPORT", "AppCompatActivity")

        viewProperties.setProperty("LITE_NAME", "${liteName}Lite")

        val import = "\nimport android.os.Bundle;\n" +
                "import androidx.annotation.Nullable;\n" +
                "import androidx.appcompat.app.AppCompatActivity;\n" +
                "import com.boss.android.lite.LiteJavaListener;\n" +
                "import com.boss.android.lite.java.BindListener;\n"+
                "import com.boss.android.lite.java.LiteJavaComponent;\n"

        viewProperties.setProperty("IMPORT", import)

        if(xmlModel != null){
            viewProperties.setProperty("IMPORT_BINDING", "import $packageName.databinding.${xmlModel.xmlBinding};")
            viewProperties.setProperty("IMPORT_BINDING_METHOD", "${xmlModel.xmlBinding} binding = ${xmlModel.xmlBinding}.inflate(getLayoutInflater());\n" +
                    "        setContentView(binding.getRoot());")
        }else{
            viewProperties.setProperty("IMPORT_BINDING", "")
            viewProperties.setProperty("IMPORT_BINDING_METHOD", "")
        }

        val stateType = FileTypeRegistry.getInstance().getFileTypeByFileName("$activityName.java")
        val statePsiClass = PsiFileFactory.getInstance(mProject)
            .createFileFromText("$activityName.java", stateType, viewTemplate.getText(viewProperties))

        val isFind =
            PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.findFile("$activityName.java")

        if (isFind != null) {
            Messages.showMessageDialog(
                mProject,
                "Lite : activity file existed.",
                "Lite",
                Messages.getInformationIcon()
            )
            return false
        }
        PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.add(statePsiClass)
        return true
    }

    private fun createLiteJavaFragment(fragmentName: String, liteName: String, xmlModel: XMLModel?,supportName: String): Boolean {

        val defaultProperties = FileTemplateManager.getInstance(mProject).defaultProperties

        val viewTemplate = FileTemplateManager.getInstance(mProject).getInternalTemplate("LiteFragmentJavaTemplate")
        val viewProperties = Properties(defaultProperties)
        viewProperties.setProperty("PACKAGE_NAME", "$packageName;")
        viewProperties.setProperty("NAME", fragmentName)
        viewProperties.setProperty("SUPPORT", supportName)

        viewProperties.setProperty("LITE_NAME", "${liteName}Lite")

        val import = "\nimport android.os.Bundle;\n" +
                "import android.view.View;\n" +
                "import android.view.LayoutInflater;\n" +
                "import android.view.ViewGroup;\n" +
                "import androidx.annotation.NonNull;\n" +
                "import androidx.annotation.Nullable;\n" +
                "import androidx.fragment.app.$supportName;\n" +
                "import com.boss.android.lite.LiteJavaListener;\n" +
                "import com.boss.android.lite.java.BindListener;\n" +
                "import com.boss.android.lite.java.LiteJavaComponent;"

        viewProperties.setProperty("IMPORT", import)

        if(xmlModel != null){
            viewProperties.setProperty("IMPORT_BINDING", "import $packageName.databinding.${xmlModel.xmlBinding};")
            viewProperties.setProperty("IMPORT_BINDING_1", "\n    ${xmlModel.xmlBinding} binding;")
            viewProperties.setProperty("IMPORT_BINDING_2",  "\n    @Nullable\n" +
                    "    @Override\n" +
                    "    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {\n" +
                    "        binding = ${xmlModel.xmlBinding}.inflate(inflater);\n" +
                    "        return binding.getRoot();\n" +
                    "    }")
            viewProperties.setProperty("IMPORT_BINDING_3", "\n    @Override\n" +
                    "    public void onDestroyView() {\n" +
                    "        super.onDestroyView();\n" +
                    "        binding = null;\n" +
                    "    }")


            viewProperties.setProperty("XML_BINDING", "\nprivate val binding: ${xmlModel.xmlBinding} by liteFragmentBinding()\n")
        }else{
            viewProperties.setProperty("IMPORT_BINDING", "")
            viewProperties.setProperty("IMPORT_BINDING_1", "")
            viewProperties.setProperty("IMPORT_BINDING_2", "")
            viewProperties.setProperty("IMPORT_BINDING_3", "")
        }


        val stateType = FileTypeRegistry.getInstance().getFileTypeByFileName("$fragmentName.java")
        val statePsiClass = PsiFileFactory.getInstance(mProject)
            .createFileFromText("$fragmentName.java", stateType, viewTemplate.getText(viewProperties))

        val isFind =
            PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.findFile("$fragmentName.java")

        if (isFind != null) {
            Messages.showMessageDialog(
                mProject,
                "Lite : fragment file existed.",
                "Lite",
                Messages.getInformationIcon()
            )
            return false
        }
        PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.add(statePsiClass)
        return true
    }

    private fun createKotlinFile(className:String) :Boolean {
        val result = when (fileModel.type) {
            FileType.Lite -> {
                createKotlinLite(className)
            }
            FileType.LiteActivity -> {
                createKotlinLite(className)

                val activityName = if (className.endsWith("activity")) {
                    className.replace("activity", "Activity")
                } else if (!className.endsWith("Activity")) {
                    className + "Activity"
                } else {
                    className
                }
                val xmlModel = if (isXML) {
                    createXML(className, "activity", activityName,fileModel.type)
                } else {
                    null
                }

                createLiteKotlinActivity(activityName, className, xmlModel)
            }
            FileType.LiteFragment -> {
                createKotlinLite(className)

                val fragmentName = if (className.endsWith("fragment")) {
                    className.replace("fragment", "Fragment")
                } else if (!className.endsWith("Fragment")) {
                    className + "Fragment"
                } else {
                    className
                }
                val xmlModel = if (isXML) {
                    createXML(className, "fragment", fragmentName,fileModel.type)
                } else {
                    null
                }
                createLiteKotlinFragment(fragmentName, className, xmlModel,"Fragment")
            }
            FileType.LiteDialogFragment -> {
                createKotlinLite(className)

                val fragmentName = if (className.endsWith("dialogFragment")) {
                    className.replace("dialogFragment", "DialogFragment")
                } else if (!className.endsWith("DialogFragment")) {
                    className + "DialogFragment"
                } else {
                    className
                }
                val xmlModel = if (isXML) {
                    createXML(className, "dialog_fragment", fragmentName,fileModel.type)
                } else {
                    null
                }
                createLiteKotlinFragment(fragmentName, className, xmlModel,"DialogFragment")
            }
        }
        return result
    }


    private fun createXML(name: String, typeName: String, activityName: String,type: FileType): XMLModel {
        val contentRoot = ProjectFileIndex.getInstance(mProject).getContentRootForFile(psiDirectory.virtualFile)

        val bindingName =  StringBuilder()
        val rootName = contentRoot?.name ?: ""
        if(rootName.isNotBlank()){
            val rootNameSub = rootName.substring(0, 1).uppercase(Locale.getDefault()) + rootName.lowercase().substring(1);
            bindingName.append(rootNameSub)
        }
        val typeNameSub = typeName.substring(0, 1).uppercase(Locale.getDefault()) + typeName.substring(1);

        val bindName = name.substring(0, 1).uppercase(Locale.getDefault()) + name.lowercase().substring(1);

        when(type){
            FileType.LiteDialogFragment->{
                bindingName.append("DialogFragment")
                bindingName.append(bindName)
                bindingName.append("Binding")
            }
            else->{
                bindingName.append(typeNameSub)
                bindingName.append(bindName)
                bindingName.append("Binding")
            }
        }

        val xmlFileName = StringBuilder()
        xmlFileName.append(rootName)
        xmlFileName.append("_")
        xmlFileName.append(typeName)
        xmlFileName.append("_")
        xmlFileName.append(name.lowercase())

        val contentText = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<androidx.constraintlayout.widget.ConstraintLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    tools:context=\"${packageName}.${activityName}\">\n" +
                "    <!-- begin to code -->\n" +
                "    \n" +
                "</androidx.constraintlayout.widget.ConstraintLayout>"
        val directory: PsiDirectory = parentDirectory(psiDirectory) ?: return  XMLModel(xmlName = xmlFileName.toString(),xmlBinding = bindingName.toString())
        val psiClass: PsiFile = PsiFileFactory.getInstance(mProject)
            .createFileFromText("$xmlFileName.xml", XmlFileType.INSTANCE, contentText)
        val psiDirectory = PsiManager.getInstance(mProject).findDirectory(directory.virtualFile) ?: return  XMLModel(xmlName = xmlFileName.toString(),xmlBinding = bindingName.toString())
        val findFile = psiDirectory.findFile(xmlFileName.toString())
        if (findFile != null) {
            return XMLModel(xmlName = xmlFileName.toString(),xmlBinding = bindingName.toString())
        }
        psiDirectory.add(psiClass)
        return XMLModel(xmlName = xmlFileName.toString(),xmlBinding = bindingName.toString())
    }

    private fun parentDirectory(psiDirectory: PsiDirectory?): PsiDirectory? {
        if ((psiDirectory?.name == "main") && psiDirectory.parentDirectory?.name == "src") {
            val directory = psiDirectory.findSubdirectory("res") ?: return null
            return directory.findSubdirectory("layout");
        }
        if (psiDirectory?.name == mProject.name) {
            return null
        }

        return parentDirectory(psiDirectory = psiDirectory?.parentDirectory)
    }

    private fun createLiteKotlinActivity(activityName: String, liteName: String, xmlModel: XMLModel?): Boolean {

        val defaultProperties = FileTemplateManager.getInstance(mProject).defaultProperties

        val viewTemplate = FileTemplateManager.getInstance(mProject).getInternalTemplate("LiteActivityTemplate")
        val viewProperties = Properties(defaultProperties)
        viewProperties.setProperty("PACKAGE_NAME", packageName)
        viewProperties.setProperty("NAME", activityName)
        viewProperties.setProperty("SUPPORT", "AppCompatActivity()")

        viewProperties.setProperty("LITE_NAME", "${liteName}Lite")

        val import = "\nimport android.os.Bundle\n" +
                "import android.os.PersistableBundle\n" +
                "import androidx.appcompat.app.AppCompatActivity\n" +
                "import com.boss.android.lite.LiteListener\n" +
                "import com.boss.android.lite.liteBind\n"

        viewProperties.setProperty("IMPORT", import)

        if(xmlModel != null){
            viewProperties.setProperty("IMPORT_BINDING", "import $packageName.databinding.${xmlModel.xmlBinding}")
            viewProperties.setProperty("IMPORT_BINDING_METHOD", "        val binding = ${xmlModel.xmlBinding}.inflate(layoutInflater)\n" +
                    "        setContentView(binding.root)\n")
        }else{
            viewProperties.setProperty("IMPORT_BINDING", "")
            viewProperties.setProperty("IMPORT_BINDING_METHOD", "")
        }

        val stateType = FileTypeRegistry.getInstance().getFileTypeByFileName("$activityName.kt")
        val statePsiClass = PsiFileFactory.getInstance(mProject)
            .createFileFromText("$activityName.kt", stateType, viewTemplate.getText(viewProperties))

        val isFind =
            PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.findFile("$activityName.kt")

        if (isFind != null) {
            Messages.showMessageDialog(
                mProject,
                "Lite : activity file existed.",
                "Lite",
                Messages.getInformationIcon()
            )
            return false
        }
        PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.add(statePsiClass)
        return true
    }

    private fun createLiteKotlinFragment(fragmentName: String, liteName: String, xmlModel: XMLModel?,supportName: String): Boolean {

        val defaultProperties = FileTemplateManager.getInstance(mProject).defaultProperties

        val viewTemplate = FileTemplateManager.getInstance(mProject).getInternalTemplate("LiteFragmentTemplate")
        val viewProperties = Properties(defaultProperties)
        viewProperties.setProperty("PACKAGE_NAME", packageName)
        viewProperties.setProperty("NAME", fragmentName)
        viewProperties.setProperty("SUPPORT", supportName)

        viewProperties.setProperty("LITE_NAME", "${liteName}Lite")

        val import = "\nimport android.os.Bundle\n" +
                "import android.view.View\n" +
                "import androidx.fragment.app.$supportName\n" +
                "import com.boss.android.lite.LiteListener\n" +
                "import com.boss.android.lite.liteBind"


        viewProperties.setProperty("IMPORT", import)

        if(xmlModel != null){
            viewProperties.setProperty("IMPORT_BINDING", "import com.boss.android.lite.liteFragmentBinding \n import $packageName.databinding.${xmlModel.xmlBinding}")
            viewProperties.setProperty("XML", "R.layout.${xmlModel.xmlName}")
            viewProperties.setProperty("XML_BINDING", "\n    private val binding: ${xmlModel.xmlBinding} by liteFragmentBinding()\n")
        }else{
            viewProperties.setProperty("IMPORT_BINDING", "")
            viewProperties.setProperty("XML", "")
            viewProperties.setProperty("XML_BINDING", "")
        }


        val stateType = FileTypeRegistry.getInstance().getFileTypeByFileName("$fragmentName.kt")
        val statePsiClass = PsiFileFactory.getInstance(mProject)
            .createFileFromText("$fragmentName.kt", stateType, viewTemplate.getText(viewProperties))

        val isFind =
            PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.findFile("$fragmentName.kt")

        if (isFind != null) {
            Messages.showMessageDialog(
                mProject,
                "Lite : fragment file existed.",
                "Lite",
                Messages.getInformationIcon()
            )
            return false
        }
        PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.add(statePsiClass)
        return true
    }

    private fun createKotlinLite(parameterClassName: String): Boolean {

        val className = if (parameterClassName.endsWith("lite")) {
            parameterClassName.replace("lite", "Lite")
        } else if (!parameterClassName.endsWith("Lite")) {
            parameterClassName + "Lite"
        } else {
            parameterClassName
        }

        val defaultProperties = FileTemplateManager.getInstance(mProject).defaultProperties
        val properties = Properties(defaultProperties)
        properties.setProperty("PACKAGE_NAME", packageName)

        properties.setProperty("NAME", className)

        val template = FileTemplateManager.getInstance(mProject).getInternalTemplate("LiteTemplate")

        val type = FileTypeRegistry.getInstance().getFileTypeByFileName("$className.kt")
        val psiClass =
            PsiFileFactory.getInstance(mProject).createFileFromText("$className.kt", type, template.getText(properties))
        val isFind = PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.findFile("$className.kt")
        if (isFind != null) {
            Messages.showMessageDialog(mProject, "Lite : lite file existed.", "Lite", Messages.getInformationIcon())
            return false
        }
        PsiManager.getInstance(mProject).findDirectory(psiDirectory.virtualFile)?.add(psiClass)
        return true
    }
}