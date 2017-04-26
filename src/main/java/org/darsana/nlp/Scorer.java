/*
   Darsana is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Darsana is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Darsana.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.darsana.nlp;

import org.darsana.util.NGram;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

/**
 * Generates n-grams from an (English) lemmatized corpus; scores grams based on specified parameters
 *
 * @author chakrabortyr
 */
public final class Scorer {

   private static enum Type {
      RAW_FREQUENCY(0),
      RELATIVE_FREQUENCY(1),
      STRING_DISTANCE(2),
      TF_IDF(3);

      private final int value;

      private Type(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }
   }

   private static double harmonicFrequency(String term, Map<String, Double> corpus) {
      String[] termBits = term.split("\\s+");
      double harmonic = 0.0;

      for (String bit : termBits) {
         harmonic += 1 / corpus.get(bit);
      }

      return termBits.length / harmonic;
   }

   private static int rawFrequency(String term, Map<String, Double> corpus) {
      String[] termBits = term.split("\\s+");
      int raw = 0;

      for (String bit : termBits) {
         raw += corpus.get(bit);
      }

      return raw / termBits.length;
   }
   
   private static Map<String,Double> generateConceptMap(String corp, int gramSize) {
      NGram srcGrams = new NGram(gramSize, corp);
      Map<String, Double> conceptMap = new TreeMap<>();
      
      while (srcGrams.hasNext()) {
         String gram = srcGrams.next();

         if (!conceptMap.containsKey(gram)) {
            conceptMap.put(gram, 1.0);
         } else {
            conceptMap.put(gram, (double) conceptMap.get(gram) + 1.0);
         }
      }
      
      return conceptMap;
   }

   private static Map<String, Double> generateCommonConceptMap(String srcCorp, String dstCorp, int gramSize) {
      NGram srcGrams = new NGram(gramSize, srcCorp);
      NGram dstGrams = new NGram(gramSize, dstCorp);
      
      Map<String, Double> conceptMap, srcMap, dstMap;
      conceptMap = new TreeMap<>();
      srcMap = generateConceptMap(srcCorp, gramSize);
      dstMap = generateConceptMap(dstCorp, gramSize);

      conceptMap.putAll(srcMap);
      conceptMap.putAll(dstMap);
      
      ArrayList<String> toRemove = new ArrayList();

      // Remove all concepts that occur in one text and not the other
      conceptMap.keySet().stream().filter((key) -> (!srcMap.containsKey(key) || !dstMap.containsKey(key))).forEachOrdered((key) -> {
         toRemove.add(key);
      });

      toRemove.forEach((rem) -> {
         conceptMap.remove(rem);
      });

      return conceptMap;
   }

   public static Map<String, Double> ScoreGram(String srcCorp, String dstCorp, int method, int gramSize) {
      // Generate grams from corpora, store in Maps
      Map<String, Double> conceptMap = generateCommonConceptMap(srcCorp, dstCorp, gramSize);

      if (method == Type.RAW_FREQUENCY.getValue()) {
         ArrayList<String> toRemove = new ArrayList<>();
         // Return raw frequency after nixing any terms that occur only once.
         conceptMap.keySet().stream().filter((key) -> (conceptMap.get(key) == 1)).forEachOrdered((key) -> {
            toRemove.add(key);
         });

         toRemove.forEach((key) -> {
            conceptMap.remove(key);
         });

         return conceptMap;
      } else if (method == Type.RELATIVE_FREQUENCY.getValue()) {
         // Return harmonic frequency of terms as they occur across all documents.
         Map<String, Double> termFrequencyMap = generateCommonConceptMap(srcCorp, dstCorp, 1);
         Map<String, Double> relativeFrequencyMap = new TreeMap<>();

         conceptMap.keySet().forEach((key) -> {
            double freq = harmonicFrequency(key, termFrequencyMap);

            if (freq > 1.0) {
               relativeFrequencyMap.put(key, freq);
            }
         });

         return relativeFrequencyMap;
      } else {
         // Return most similar strings across all documents, likely needs trigrams or larger 
         // to be truly useful.
         Map<String, Double> srcMap = generateConceptMap(srcCorp, gramSize);
         Map<String, Double> dstMap = generateConceptMap(dstCorp, gramSize);
         Map<String, Double> distanceMap = new TreeMap<>();
         
         Object[] srcKeys = srcMap.keySet().toArray();
         Object[] dstKeys = dstMap.keySet().toArray();

         for (int i = 0; i < srcKeys.length- 1; i++) {
            for (int j = 0; j < dstKeys.length - 1; j++) {
               double score = StringUtils.getJaroWinklerDistance(srcKeys[i].toString(), dstKeys[j].toString());

               if (score >= 0.9) {
                  distanceMap.put(srcKeys[i] + "," + dstKeys[j], score);
               }
            }
         }

         return distanceMap;
      }
   }
}
