package com.github.uglyog.markdowncodecheck

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

interface Validator {
  fun validateBlock(block: CodeBlock): Result
}

sealed class Result {
  data class Ok(val types: List<String> = emptyList()) : Result()
  data class Error(val errors: List<String> = emptyList()) : Result()

  fun merge(result: Result): Result {
    return when (this) {
      is Ok -> when (result) {
        is Ok -> Ok(this.types + result.types)
        is Error -> result
      }
      is Error -> when (result) {
        is Ok -> this
        is Error -> Error(this.errors + result.errors)
      }
    }
  }

  companion object {
    fun tryBlock(type: String, block: () -> Unit): Result {
      return try {
        block()
        Ok(listOf(type))
      } catch (e: Throwable) {
        Error(listOf(e.message ?: "Caught $e"))
      }
    }
  }
}

open class CheckMarkdownTask : DefaultTask() {
  @InputFile
  lateinit var markdownFile: File

  private val validators = mapOf<String, Validator>(
    "json" to JsonValidator,
    "xml" to XmlValidator
  )

  @TaskAction
  fun checkMarkdown() {
    println("Checking code blocks in $markdownFile ...")

    var anyFailed = false
    val parser = MarkdownParser(markdownFile)
    for (block in parser.codeBlocks()) {
      print("    Checking block at line ${block.line} - ")

      if (block.options.isEmpty() || block.options.contains("ignore") || block.options.contains("console") ||
        block.options.all { !validators.containsKey(it) }) {
        println("SKIP")
      } else {
        val validators = block.options.mapNotNull { validators[it] }
        val result = validators.fold(Result.Ok() as Result) { acc, validator ->
          acc.merge(validator.validateBlock(block))
        }
        when (result) {
          is Result.Ok -> println("OK (${result.types.joinToString(", ")})")
          is Result.Error -> {
            println("ERROR (${result.errors.joinToString(", ")})")
            anyFailed = true
          }
        }
      }
    }

    if (anyFailed) {
      throw GradleException("There were code blocks that failed validation")
    }
  }
}

object JsonValidator : Validator {
  override fun validateBlock(block: CodeBlock) = Result.tryBlock("json") {
    val jsonParser = JsonSlurper()
    jsonParser.parseText(block.lines.joinToString("\n"))
  }
}

object XmlValidator : Validator {
  override fun validateBlock(block: CodeBlock) = Result.tryBlock("xml") {
    val dbFactory = DocumentBuilderFactory.newInstance()
    val dBuilder = dbFactory.newDocumentBuilder()
    val xmlInput = InputSource(StringReader(block.lines.joinToString("\n")))
    dBuilder.parse(xmlInput)
  }
}

class MarkdownParser(private val file: File) {
  fun codeBlocks(): List<CodeBlock> {
    val blocks = mutableListOf<CodeBlock>()
    file.useLines { lines ->
      var block: CodeBlock? = null
      lines.forEachIndexed() { index, line ->
        if (line.startsWith("```")) {
          block = if (block == null) {
            val options = line.substring(3).split(',').map { it.trim() }.filterNot { it.isEmpty() }
            CodeBlock(index + 1, LinkedHashSet(options))
          } else {
            blocks.add(block!!)
            null
          }
        } else block?.lines?.add(line)
      }
    }
    return blocks
  }
}

data class CodeBlock(
  val line: Int,
  val options: LinkedHashSet<String> = linkedSetOf(),
  val lines: MutableList<String> = mutableListOf()
)
