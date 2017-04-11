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

import com.google.common.collect.Multimap;
import com.google.common.collect.LinkedHashMultimap;

/**
 * Dictionary of lemmas, implemented as multimap. Lemmas are unique, so we label them as a key,
 * while mapping multiple words to the same lemma.
 *
 * @author chakrabortyr
 */
public class Dictionary {
   private final Multimap<String, String> _dictionary = LinkedHashMultimap.create();
   private String language;

   public Dictionary(String language) {
      this.language = language;
   }

   public String getLang() {
      return this.language;
   }

   public boolean hasConcept(String key) {
      return _dictionary.containsKey(key);
   }

   /**
    * Return key (lemma) associated with a given concept
    *
    * @param concept
    * @return the lemma we want
    */
   public String getLemma(String concept) {

      for (String key : _dictionary.keySet()) {
         if (_dictionary.get(key).contains(concept)) {
            return key;
         }
      }

      return "";
   }

   /**
    * Associates a concept with a given lemma
    * @param lemma The lemma wherewith we associate a concept
    * @param concept The concept we are associating with the lemma
    */
   public void putLemma(String lemma, String concept) {
      _dictionary.put(lemma, concept);
   }
}
