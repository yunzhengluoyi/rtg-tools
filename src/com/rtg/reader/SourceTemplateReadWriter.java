/*
 * Copyright (c) 2014. Real Time Genomics Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rtg.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.rtg.util.StringUtils;

/**
 * A class to read and write the source template files
 * generated by the read simulator.
 */
public final class SourceTemplateReadWriter {

  private SourceTemplateReadWriter() { }

  /** Name of the file containing the mapping from template set ID to template SDF */
  static final String TEMPLATE_MAP_FILE = "simTemplates";

  /** Name of the file containing the mapping from mutated genome to original reference SDF */
  static final String MUTATION_MAP_FILE = "mutTemplate";

  /**
   * Reads in the list of SDF IDs of the templates from which simulated reads were generated.
   * @param baseDir the directory of the preread.
   * @return the list of template IDs, or null if no template map file is present.
   * @throws IOException if an I/O error occurs during reading.
   */
  public static SdfId[] readTemplateMap(File baseDir) throws IOException {
    return readMappingFile(new File(baseDir, TEMPLATE_MAP_FILE));
  }

  /**
   * Reads in the SDF ID of the template from which the mutated genome was generated.
   * @param baseDir the directory of the preread.
   * @return the template ID, or null if no template map file is present.
   * @throws IOException if an I/O error occurs during reading.
   */
  public static SdfId readMutationMap(File baseDir) throws IOException {
    final SdfId[] mappings = readMappingFile(new File(baseDir, MUTATION_MAP_FILE));
    return mappings == null ? null : mappings[0];
  }

  private static SdfId[] readMappingFile(File mappingFile) throws IOException {
    if (mappingFile.exists()) {
      try (BufferedReader br = new BufferedReader(new FileReader(mappingFile))) {
        final ArrayList<SdfId> ids = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
          try {
            ids.add(new SdfId(line));
          } catch (final NumberFormatException e) {
            throw new CorruptSdfException("Malformed simulator template map line " + line);
          }
        }
        final SdfId[] result = new SdfId[ids.size()];
        int i = 0;
        for (SdfId l : ids) {
          result[i++] = l;
        }
        return result;
      }
    }
    return null;
  }

  /**
   * Writes out the list of SDF IDs of the templates from which simulated reads were generated.
   * @param baseDir the directory of the preread.
   * @param templateIds the template ID, or null if no template map is to be written.
   * @throws IOException if an I/O error occurs during writing.
   */
  public static void writeTemplateMappingFile(File baseDir, SdfId[] templateIds) throws IOException {
    if (templateIds != null) {
      writeMappingFile(new File(baseDir, TEMPLATE_MAP_FILE), templateIds);
    }
  }

  /**
   * Writes out the SDF ID of the template from which the mutated genome was generated.
   * @param baseDir the directory of the preread.
   * @param referenceId the template ID, or null if no template map is to be written.
   * @throws IOException if an I/O error occurs during writing.
   */
  public static void writeMutationMappingFile(File baseDir, SdfId referenceId) throws IOException {
    if (referenceId != null) {
      writeMappingFile(new File(baseDir, MUTATION_MAP_FILE), referenceId);
    }
  }

  private static void writeMappingFile(File mappingFile, SdfId... ids) throws IOException {
    final StringBuilder sb = new StringBuilder();
    for (SdfId id : ids) {
      sb.append(id).append(StringUtils.LS);
    }
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(mappingFile))) {
      bw.write(sb.toString());
    }
  }

  /**
   * Reads in the mutated genomes reference ID file and then writes it to the new preread directory.
   * @param originDir the directory of the input preread.
   * @param destinationDir the directory of the output preread.
   * @throws IOException if an I/O error occurs during reading or writing.
   */
  public static void copyMutationMappingFile(File originDir, File destinationDir) throws IOException {
    final File originFile = new File(originDir, MUTATION_MAP_FILE);
    if (originFile.exists()) {
      final SdfId id = readMutationMap(originDir);
      writeMutationMappingFile(destinationDir, id);
    }
  }
}
