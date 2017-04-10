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

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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

   public static Map<String, Integer> Score(String srcCorp, String dstCorp, int method, int gramSize) {
      //Generate grams from corpora, store in Maps
      Map<String, Integer> conceptMap = new HashMap<>();

      NGram srcGrams = new NGram(gramSize, srcCorp);
      NGram dstGrams = new NGram(gramSize, dstCorp);
      String gram;

      while (srcGrams.hasNext()) {
         gram = srcGrams.next();

         if (!conceptMap.containsKey(gram)) {
            conceptMap.put(gram, 1);
         } else {
            conceptMap.put(gram, (int) conceptMap.get(gram) + 1);
         }
      }

      while (dstGrams.hasNext()) {
         gram = dstGrams.next();

         if (!conceptMap.containsKey(gram)) {
            conceptMap.put(gram, 1);
         } else {
            conceptMap.put(gram, (int) conceptMap.get(gram) + 1);

         }
      }
      
      ArrayList<String> toRemove = new ArrayList<>();

      // Remove all grams that only occur once
      conceptMap.keySet().stream().filter((key) -> (conceptMap.get(key) == 1)).forEachOrdered((key) -> {
         toRemove.add(key);
      });
      
      toRemove.forEach((key) -> {
         conceptMap.remove(key);
      });
      
      // if all we care about is raw frequency, we're done
      if (method == Type.RAW_FREQUENCY.getValue()) {
         return conceptMap;
      }
      
      //TODO: Implement all scoring methods

      return conceptMap;
   }
}
