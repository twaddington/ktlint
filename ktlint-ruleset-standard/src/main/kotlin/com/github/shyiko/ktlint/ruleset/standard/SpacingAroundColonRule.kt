package com.github.shyiko.ktlint.ruleset.standard

import com.github.shyiko.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotation
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtTypeParameterList
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class SpacingAroundColonRule : Rule("colon-spacing") {

    override fun visit(node: ASTNode, autoCorrect: Boolean,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (node is LeafPsiElement && node.textMatches(":") && !node.isPartOfString()) {
            if (node.isPartOf(KtAnnotation::class) || node.isPartOf(KtAnnotationEntry::class)) {
                // todo: enfore "no spacing"
                return
            }
            if (node.prevSibling is PsiWhiteSpace &&
                node.parent !is KtClassOrObject && node.parent.parent !is KtTypeParameterList) {
                emit(node.startOffset, "Unexpected spacing before \":\"", true)
                if (autoCorrect) {
                    var prevNode = node
                    while (prevNode
                        .treePrev
                        .elementType
                        .let { it != KtStubElementTypes.VALUE_PARAMETER_LIST && it != KtTokens.IDENTIFIER }) {
                        prevNode = prevNode.treePrev
                    }
                    node.treeParent.removeRange(prevNode, node)
                }
            }
            val missingSpacingBefore = node.prevSibling !is PsiWhiteSpace &&
                (node.parent is KtClassOrObject || node.parent.parent is KtTypeParameterList)
            val missingSpacingAfter = node.nextSibling !is PsiWhiteSpace
            when {
                missingSpacingBefore && missingSpacingAfter -> {
                    emit(node.startOffset, "Missing spacing around \":\"", true)
                    if (autoCorrect) {
                        node.rawInsertBeforeMe(PsiWhiteSpaceImpl(" "))
                        node.rawInsertAfterMe(PsiWhiteSpaceImpl(" "))
                    }
                }
                missingSpacingBefore -> {
                    emit(node.startOffset, "Missing spacing before \":\"", true)
                    if (autoCorrect) {
                        node.rawInsertBeforeMe(PsiWhiteSpaceImpl(" "))
                    }
                }
                missingSpacingAfter -> {
                    emit(node.startOffset + 1, "Missing spacing after \":\"", true)
                    if (autoCorrect) {
                        node.rawInsertAfterMe(PsiWhiteSpaceImpl(" "))
                    }
                }
            }
        }
    }
}
