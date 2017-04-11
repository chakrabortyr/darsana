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
package util;

import java.util.Iterator;

/**
 * N-Gram gen via Iterator
 * @author chakrabortyr
 */
public class NGram implements Iterator<String> {
   String[] grams;
   int pos = 0, n;

   public NGram(int n, String str) {
      this.n = n;
      grams = str.split(" ");
   }

   @Override
   public boolean hasNext() {
      return pos < grams.length - n + 1;
   }

   @Override
   public String next() {
      StringBuilder builder = new StringBuilder();

      for (int i = pos; i < pos + n;) {
         builder.append(i > pos ? " " : "").append(grams[i]);
         i++;
      }
      pos++;
      return builder.toString();
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException();
   }
}
