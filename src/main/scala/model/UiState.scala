package org.satyagraha.searcher
package model

case class UiState(baseDir: String,
                   includeFiles: String,
                   excludeFiles: String,
                   excludeDirs: String,
                   matchText: String,
                   matchCase: Boolean,
                   isRegex: Boolean,
                   recurse: Boolean)
