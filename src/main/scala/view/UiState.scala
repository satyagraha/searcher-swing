package org.satyagraha.searcher
package view

case class UiState(baseDir: String,
                   includeFiles: String,
                   excludeFiles: String,
                   excludeDirs: String,
                   matchText: String,
                   matchCase: Boolean,
                   isRegex: Boolean,
                   recurse: Boolean)
