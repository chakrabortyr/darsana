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
package org.darsana.client;

import java.util.Map;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.darsana.nlp.Scorer;

/**
 * REST Controller for Darsana Service
 *
 * @author chakrabortyr
 */
@RestController
public class DarsanaController {

   @RequestMapping(method = RequestMethod.GET, value = "/test")
   public String test() {
      return "Oh hi";
   }
   
   @RequestMapping(method = RequestMethod.GET, value= "/score/grams") 
   public Map<String,Double> scoreGrams(@RequestParam Map<String, String> request) {
      
      String src = request.get("src").replaceAll("[?,!.;]+", "").trim().toLowerCase();
      String dst = request.get("dst").replaceAll("[?,!.;]+", "").trim().toLowerCase();
      
      //TODO: Normalize, Lemmatize, etc etc
      return Scorer.ScoreGram(src, dst, 
              Integer.parseInt(request.get("scoreBy")), 
              Integer.parseInt(request.get("size")));
   }
   
   @RequestMapping(method = RequestMethod.POST, value= "/score/grams", consumes= MediaType.APPLICATION_JSON_VALUE) 
   public @ResponseBody Map<String,Double> scoreGramsBig(@RequestBody Map<String, String> body) {
      
      String src = body.get("src").replaceAll("[?,!.;]+", " ").trim().toLowerCase();
      String dst = body.get("dst").replaceAll("[?,!.;]+", " ").trim().toLowerCase();
      
      //TODO: Normalize, Lemmatize, etc etc
      return Scorer.ScoreGram(src, dst, 
              Integer.parseInt(body.get("scoreBy")), 
              Integer.parseInt(body.get("size")));
   }
}
