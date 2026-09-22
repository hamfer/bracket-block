package com.github.hamfer.bracketblock

import com.github.hamfer.bracketblock.highlighter.BracketBlockHighlighter
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

    fun testXMLFile() {
        val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
        val xmlFile = assertInstanceOf(psiFile, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))

        assertNotNull(xmlFile.rootTag)

        xmlFile.rootTag?.let {
            assertEquals("foo", it.name)
            assertEquals("bar", it.value.text)
        }
    }

    fun testXMLScopeViaHighlighter() {
        myFixture.configureByText(XmlFileType.INSTANCE, "<root><child>he<caret>llo</child></root>")
        val editor = myFixture.editor
        val offset = editor.caretModel.offset

        val highlighter = BracketBlockHighlighter(editor)
        val range = highlighter.findClosetBracePairOrScope(offset)

        assertNotNull(range)

        // Verify the detected scope corresponds to the enclosing XmlTag
        val elementAtCaret: PsiElement? = myFixture.file.findElementAt(offset)
        val tag = PsiTreeUtil.getParentOfType(elementAtCaret, XmlTag::class.java, false)
        assertNotNull(tag)
        if (tag != null && range != null) {
            assertEquals(tag.textRange.startOffset, range.startOffset)
            assertEquals(tag.textRange.endOffset, range.endOffset)
        }
    }

    fun testHTMLScope() {
        myFixture.configureByText(HtmlFileType.INSTANCE, "<div><span>ab<caret>cd</span></div>")
        val editor = myFixture.editor
        val offset = editor.caretModel.offset

        val highlighter = BracketBlockHighlighter(editor)
        val range = highlighter.findClosetBracePairOrScope(offset)

        assertNotNull(range)

        // Verify scope equals the enclosing span tag
        val elementAtCaret: PsiElement? = myFixture.file.findElementAt(offset)
        val tag = PsiTreeUtil.getParentOfType(elementAtCaret, XmlTag::class.java, false)
        assertNotNull(tag)
        if (tag != null && range != null) {
            assertEquals(tag.textRange.startOffset, range.startOffset)
            assertEquals(tag.textRange.endOffset, range.endOffset)
        }
    }

    fun testJavaBracketScope() {
        val code = """
            class A {
              void m() {
                int x = (1 + 2<caret>);
              }
            }
        """.trimIndent()
        myFixture.configureByText("A.java", code)

        val editor = myFixture.editor
        val highlighter = BracketBlockHighlighter(editor)
        val range = highlighter.findClosetBracePairOrScope(editor.caretModel.offset)

        // We should at least detect some bracket or scope around the caret
        assertNotNull(range)
        if (range != null) {
            assertTrue("Invalid range returned", range.startOffset < range.endOffset)
        }
    }

    // Note: rename tests from the template were removed because the corresponding
    // test data files are no longer present after repository cleanup.
}
