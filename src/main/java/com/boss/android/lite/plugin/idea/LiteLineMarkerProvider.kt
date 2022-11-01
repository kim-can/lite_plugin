package com.boss.android.lite.plugin.idea

import com.android.tools.lint.detector.api.isKotlin
import com.boss.android.lite.plugin.utils.Icons
import com.boss.android.lite.plugin.utils.LiteConstants.LITE_BIND_JAVA_CLASS
import com.boss.android.lite.plugin.utils.LiteConstants.LITE_CLASS
import com.boss.android.lite.plugin.utils.LiteConstants.LITE_LIFECYCLE_CLASS
import com.boss.android.lite.plugin.utils.LiteConstants.LITE_LISTENER_JAVA_CLASS
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.usageView.UsageInfo
import com.intellij.usages.UsageInfo2UsageAdapter
import org.jetbrains.plugins.groovy.intentions.style.inference.resolve
import org.jetbrains.uast.*
import java.util.function.Supplier


class LiteLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {

        val uElement = element.toUElement() ?: return null

        if (element !is PsiExpressionStatement ) {
            uElement.getCallExpression()?.apply {
                if(methodName == "liteBind" || methodName == "liteFind"){
                    if(returnType?.canonicalText?.startsWith("kotlin.Lazy") == true){
                        sourcePsi?.apply {
                            return LiteBindLineMarkerInfo(this)
                        }
                    }
                }
            }

            if (uElement !is UQualifiedReferenceExpression) {
                return null
            }

            val uCallExpression =  uElement.selector  as? UCallExpression
            if (uCallExpression != null && uCallExpression.isLite() ) {
                val psiIdentifier = uCallExpression.methodIdentifier?.sourcePsi ?: return null
                return LiteLineMarkerInfo(psiIdentifier)
            }
        }
        return null
    }

}
private fun UCallExpression.isLite(): Boolean {

    if(methodName == "liteBind" || methodName == "listener"){
        val psiType = receiver?.getExpressionType() ?: return false

        if(isKotlin(language = this.lang)){
            val lite = psiType.resolve()?.extendsListTypes?.filter {
                it.canonicalText.startsWith(LITE_CLASS) || it.canonicalText.startsWith(LITE_LIFECYCLE_CLASS)
            }
            if(lite == null || lite.isEmpty()){
                return false
            }
        }else{
            if(psiType.canonicalText.startsWith(LITE_LISTENER_JAVA_CLASS)|| psiType.canonicalText.startsWith(LITE_BIND_JAVA_CLASS)){
                return true
            }
        }

        return true
    }
    val receiver =  receiverType.resolve()

    val list = receiver?.extendsListTypes?.filter {
        it.canonicalText.startsWith(LITE_CLASS)
    }
    if(list == null || list.isEmpty()){
        return false
    }
    return  true
}


private class LiteBindLineMarkerInfo(
    psiElement: PsiElement
) : LineMarkerInfo<PsiElement>(
    psiElement,
    psiElement.textRange,
    Icons.create_file,
    null,
    { _, element ->
        element.apply {
            val  callExpression = element.toUElement()?.getCallExpression() ?: return@apply

            val psiClassReferenceType = callExpression.returnType as PsiClassReferenceType
            val psiReferenceParameterList = psiClassReferenceType.reference.parameterList as PsiReferenceParameterList
            val psiTypeElement = psiReferenceParameterList.typeParameterElements.firstOrNull() as PsiTypeElement
            val liteClass =  psiTypeElement.firstChild.toUElement()
            val resolveUElement = liteClass?.sourcePsi?.reference?.resolve().toUElement()

            val sourcePsi = resolveUElement?.sourcePsi ?: return@apply
            ApplicationManager.getApplication().invokeLater {
                UsageInfo2UsageAdapter(UsageInfo(sourcePsi)).navigate(true)
            }
        }
    },
    GutterIconRenderer.Alignment.LEFT,
    Supplier<String> { "Lite" },
)

private class LiteLineMarkerInfo(
    psiElement: PsiElement
) : LineMarkerInfo<PsiElement>(
    psiElement,
    psiElement.textRange,
    Icons.create_file,
    null,
    { _, element ->
        element.apply {
            val callExpression = element.toUElement()?.getLiteParentOfType<UCallExpression>() ?: return@apply

            when(callExpression.methodName){
                "liteBind"->{
                    if(isKotlin(language = callExpression.lang)){
                        ApplicationManager.getApplication().invokeLater {
                            callExpression.valueArguments.firstOrNull()?.getExpressionType()?.resolve()?.navigate(true)
                        }
                    }else{
                        ApplicationManager.getApplication().invokeLater {
                            callExpression.returnType.resolve()?.navigate(true)
                        }
                    }
                }
                "listener"->{
                    if(isKotlin(language = callExpression.lang)){
                        val expressionType = callExpression.receiver?.getExpressionType().resolve() ?: return@apply
                        val stateElement = expressionType.innerClasses.firstOrNull() ?: return@apply
                        ApplicationManager.getApplication().invokeLater {
                            UsageInfo2UsageAdapter(UsageInfo(stateElement)).navigate(true)
                        }
                    }else{
                        val  expressionType = callExpression.valueArguments.firstOrNull()?.getExpressionType().resolve() ?: return@apply

                        val stateElement = expressionType.innerClasses.firstOrNull() ?: return@apply
                        ApplicationManager.getApplication().invokeLater {
                            UsageInfo2UsageAdapter(UsageInfo(stateElement)).navigate(true)
                        }

                    }
                }
                else ->{
                    ApplicationManager.getApplication().invokeLater {
                        callExpression.receiverType.resolve()?.navigate(true)
                    }
                }
            }
        }
    },
    GutterIconRenderer.Alignment.LEFT,
    Supplier<String> { "Lite" },
)
