package com.boss.android.lite.plugin.utils

import com.boss.android.lite.plugin.utils.LiteConstants.ASYNC_FUN_CLASS
import com.intellij.lang.Language
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference
import org.jetbrains.kotlin.psi.KtNamedFunction

object LitePsiUtils {
    fun isKotlin(psiElement: PsiElement): Boolean {
        return psiElement.language.`is`(Language.findLanguageByID("kotlin"))
    }

    fun isJava(psiElement: PsiElement): Boolean {
        return psiElement.language.`is`(Language.findLanguageByID("JAVA"))
    }
    fun isLiteBindAndFind(psiElement: PsiElement): Boolean {

        when (psiElement.language) {
            Language.findLanguageByID("JAVA") -> {

            }
            Language.findLanguageByID("kotlin") -> {

            }
        }

        return false
    }

    fun isLiteListener(psiElement: PsiElement): Boolean {
        when (psiElement.language) {
            Language.findLanguageByID("JAVA") -> {

            }
            Language.findLanguageByID("kotlin") -> {

            }
        }
        return false
    }

    fun isLiteFun(psiElement: PsiElement): Boolean {
        when (psiElement.language) {
            Language.findLanguageByID("JAVA") -> {
                if(psiElement is KtNamedFunction){

                }

                if (psiElement is PsiMethod) {
                    val returnType = psiElement.returnTypeElement ?: return false

                    if(returnType is PsiClass){
                        if(safeEquals(returnType.qualifiedName,ASYNC_FUN_CLASS)){
                            return true
                        }
                    }
                }

            }
            Language.findLanguageByID("kotlin") -> {

            }
        }

        return false
    }

    fun isLiteFunCall(psiElement: PsiElement): Boolean {
        when (psiElement.language) {
            Language.findLanguageByID("JAVA") -> {
                if(psiElement is PsiCallExpression){
                    val method = psiElement.resolveMethod()  ?: return false
                    if(method.lastChild is PsiReferenceExpressionImpl){
                        if(safeEquals(method.lastChild.text, LiteConstants.FUN_NAME2)) {
                            return true
                        }
                    }
                }
            }
            Language.findLanguageByID("kotlin") -> {
                if(psiElement is PsiCallExpression){

                }
            }
        }



        return false
    }

    private fun safeEquals(obj: String?, value: String): Boolean {
        return obj != null && obj == value
    }






}