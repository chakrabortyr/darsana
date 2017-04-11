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

import util.NGram;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

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
      String [] termBits = term.split("\\s+");
      double harmonic = 0.0;
      
      for (String bit : termBits) {
         harmonic += 1 / corpus.get(bit);
      }
      
      return termBits.length / harmonic;
   }
   
   private static int rawFrequency(String term, Map<String, Double> corpus) {
      String [] termBits = term.split("\\s+");
      int raw = 0;
      
      for (String bit : termBits) {
         raw += corpus.get(bit);
      }
      
      return raw / termBits.length;
   }

   private static double scoreTFIDF(int termFreq, int docFreq, int docSize) throws ArithmeticException {
      double logFrequency = 1 + Math.log(termFreq);
      double invDocFrequency = Math.log(docSize / docFreq);

      return logFrequency * invDocFrequency;
   }

   private static Map<String, Double> generateConceptMap(String srcCorp, String dstCorp, int gramSize) {
      NGram srcGrams = new NGram(gramSize, srcCorp);
      NGram dstGrams = new NGram(gramSize, dstCorp);
      String gram;
      
      Map<String, Double> conceptMap = new TreeMap<>();

      while (srcGrams.hasNext()) {
         gram = srcGrams.next();

         if (!conceptMap.containsKey(gram)) {
            conceptMap.put(gram, 1.0);
         } else {
            conceptMap.put(gram, (double) conceptMap.get(gram) + 1.0);
         }
      }

      while (dstGrams.hasNext()) {
         gram = dstGrams.next();

         if (!conceptMap.containsKey(gram)) {
            conceptMap.put(gram, 1.0);
         } else {
            conceptMap.put(gram, (double) conceptMap.get(gram) + 1.0);
         }
      }
      
      return conceptMap;
   }

   public static Map<String, Double> ScoreGram(String srcCorp, String dstCorp, int method, int gramSize) {
      // Generate grams from corpora, store in Maps
      Map<String, Double> conceptMap = generateConceptMap(srcCorp, dstCorp, gramSize);

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
         Map<String, Double> termFrequencyMap = generateConceptMap(srcCorp, dstCorp, 1);
         Map<String, Double> relativeFrequencyMap = new TreeMap<>();
         
         conceptMap.keySet().forEach((key) -> {
            relativeFrequencyMap.put(key, harmonicFrequency(key, termFrequencyMap));
         });
         
         return relativeFrequencyMap;
      } else if (method == Type.STRING_DISTANCE.getValue()) {
         // Return most similar strings across all documents, likely needs trigrams or larger 
         // to be truly useful.
         String [] keys = (String[]) conceptMap.keySet().toArray();
         Map<String, Double> distanceMap = new TreeMap<>();
         
         for (int i = 0; i != keys.length; i++) {
            double score = StringUtils.getJaroWinklerDistance(keys[i], keys[i + 1]);
            
            if (score >= 0.9) {
               distanceMap.put(keys[i] + "," + keys[i +1], score);
            }
         }
         
         return distanceMap;
      } else {
         // Score terms by term frequency over inverse document frequency (tfIdf)
         // REF: https://en.wikipedia.org/wiki/Tf-idf
         Map<String, Double> termFrequencyMap = generateConceptMap(srcCorp, dstCorp, 1);
         Map<String, Double> tfIdfMap = new TreeMap<>();
         
         int documentSize = StringUtils.countMatches(srcCorp, "\\s") + 
                 StringUtils.countMatches(dstCorp, "\\s");
         
         conceptMap.keySet().forEach((key) -> {
            int termFrequency = rawFrequency(key, termFrequencyMap);
            int docFrequency = conceptMap.get(key).intValue();
            double tfIdf = scoreTFIDF(termFrequency, docFrequency, documentSize);
            
            tfIdfMap.put(key, tfIdf);
         });
         
         return tfIdfMap;
      }
   }
}
