/*******************************************************************************
 * Copyright 2012-2019 Phil Glover and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package org.pitest.pitclipse.ui.swtbot;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pitest.pitclipse.runner.PitResults;
import org.pitest.pitclipse.runner.results.Mutations.Mutation;

import meteor.eclipse.plugin.core.tuples.Tuple2;

public class ResultsParser {

    public static final class Summary {

        private final int classes;
        private final int codeCoverage;
        private final int mutationCoverage;
        private final int generatedMutants;
        private final int killedMutants;
		private final int linesTotal;
        private final int linesCovered;

        private Summary(int classes, int codeCovered, int linesCoverage, int linesTotal, int mutationCoverage, int generatedMutants, int killedMutants) {
            this.classes = classes;
            this.codeCoverage = codeCovered;
            this.mutationCoverage = mutationCoverage;
            this.generatedMutants = generatedMutants;
            this.killedMutants = killedMutants;
            this.linesTotal = linesTotal;
            this.linesCovered = linesCoverage;
        }

        public int getClasses() {
            return classes;
        }

        public int getCodeCoverage() {
            return codeCoverage;
        }

        public int getMutationCoverage() {
            return mutationCoverage;
        }
        
        public double getMutationScore() {
        	return killedMutants / (double)generatedMutants;
        }

        public int getGeneratedMutants() {
            return generatedMutants;
        }

        public int getKilledMutants() {
            return killedMutants;
        }
        
        public int getLinesTotal() {
			return linesTotal;
		}

		public int getLinesCovered() {
			return linesCovered;
		}
    }

    private static final String SUMMARY_START = "<h3>Project Summary</h3>";
    private static final String SUMMARY_END = "</table>";

    private final String html;
    private final int generatedMutants;
    private final int killedMutants;

    public ResultsParser(PitResults result) throws IOException {
        this.html = new String(Files.readAllBytes(Paths.get(result.getHtmlResultFile().getAbsolutePath())));
        List<Mutation> mutations = result.getMutations().getMutation();
        generatedMutants = mutations.size();
        killedMutants = getKilledMutants(mutations);
    }

    private int getKilledMutants(List<Mutation> mutants) {
        int tmp = 0;
        for (Mutation mutation : mutants) {
            if (mutation.isDetected()) {
                tmp++;
            }
        }
        return tmp;
    }
    
    public static Tuple2<Integer, Integer> getLineCoverage(String html) {
        // Parse HTML usando Jsoup
        Document doc = Jsoup.parse(html);

        // Encontre a primeira linha dentro do tbody
        Element tbody = doc.select("tbody").first();
        Element firstRow = tbody.selectFirst("tr");

        // Pegue o texto da segunda coluna dessa linha
        Elements columns = firstRow.select("td");
        if (columns.size() >= 2 && columns.get(2).getAllElements().size() > 3) {
            String secondColumnText = columns.get(1).getAllElements().get(3).text();
            String[] values = secondColumnText.split("/");

            if (values.length == 2) {
                int value1 = Integer.parseInt(values[0]);
                int value2 = Integer.parseInt(values[1]);
                return new Tuple2<>(value1, value2);
            } else {
                throw new IllegalArgumentException("Não foi possível extrair os valores.");
            }
        } else {
            throw new IllegalArgumentException("Não foi possível encontrar a segunda coluna.");
        }
    }


    private String getProjectSummary() {
        String summary = "";
        int startPos = caseInsensitveIndexOf(html, SUMMARY_START);
        if (startPos != -1) {
            int endPos = caseInsensitveIndexOf(html, SUMMARY_END, startPos);
            if (endPos != -1) {
                return html.substring(startPos, endPos + SUMMARY_END.length());
            }
        }
        return summary;
    }

    public Summary getSummary() {
        String summary = getProjectSummary();
        int classes = 0;
        int codeCoverage = 100;
        int mutationCoverage = 100;
        if (!summary.isEmpty()) {
            HtmlTable table = new HtmlTable(summary);
            List<Map<String, String>> results = table.getResults();
            if (results.size() == 1) {
                Map<String, String> mapResults = results.get(0);
                classes = parseInt(mapResults.get("Number of Classes"));
                codeCoverage = parseInt(mapResults.get("Line Coverage")
                        .replace("%", ""));
                mutationCoverage = parseInt(mapResults.get(
                        "Mutation Coverage").replace("%", ""));
            }
        }
        Tuple2<Integer, Integer> lineCoverage = getLineCoverage(html);
        return new Summary(classes, 
        				   codeCoverage, 
        				   lineCoverage.first, 
        				   lineCoverage.second, 
        				   mutationCoverage, 
        				   generatedMutants, 
        				   killedMutants);
    }

    static int caseInsensitveIndexOf(String s, String searchString) {
        return caseInsensitveIndexOf(s, searchString, 0);
    }

    static int caseInsensitveIndexOf(String s, String searchString, int offset) {
        return s.toLowerCase().indexOf(searchString.toLowerCase(), offset);
    }
}
