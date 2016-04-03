package evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

public class TopicCoherence {
	/**
	 * term-index, index-term dictionary
	 */
	public HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
	public HashMap<Integer, String> inverseDictionary = new HashMap<Integer, String>();

	// term-document frequency matrix
	public int[][] termDocMat;

	public int documentSize = 0;

	// sum of matrix
	int matrixSum = 0;
	int[] termSums;
	int[] docSums;
	
	public static void runEval(BufferedWriter bw, String model, String[] domainNames){
		
		try {
			for (String domain:domainNames){
				String dictionaryPath = "./input/"+domain+"/"+domain+".vocab";
				String dataPath = "./input/"+domain+"/"+domain+".docs";
				
				TopicCoherence t = new TopicCoherence(dictionaryPath,dataPath);
				
				String topicWordPath = "./output/"+model+"/DomainModels/"+domain+"/"+domain+".twords";
				double score = t.coherenceScore(topicWordPath);

				System.out.println(domain+" topic Coherence:" + score);	
				
				java.util.Date date= new java.util.Date();
				bw.write(new Timestamp(date.getTime())+"  "+domain+" topic Coherence:" + score);
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TopicCoherence(String dictionaryPath, String dataPath) {
		processDictionary(dictionaryPath);
		calDocSize(dataPath);
		initMat();
		readInData(dataPath);
		calSum();
	}

	public void processDictionary(String dictionaryPath) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(dictionaryPath));

			String line = null;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(":");
				String word = parts[1].trim();
				Integer id = Integer.valueOf(parts[0].trim());
				dictionary.put(word, id);
				inverseDictionary.put(id, word);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void calDocSize(String dataPath) {
		documentSize = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(dataPath));
			String line;
			while ((line = br.readLine()) != null) {
				documentSize++;
			}
			br.close();
			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initMat() {
		termDocMat = new int[dictionary.size()][documentSize];
		termSums = new int[termDocMat.length];
		docSums = new int[termDocMat[0].length];
	}

	public void readInData(String dataPath) {
		int docIndex = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(dataPath));

			String line;
			while ((line = br.readLine()) != null) {
				String[] words = StringUtils.split(StringUtils.strip(line), " ");
				for (String word : words) {
					termDocMat[Integer.valueOf(word)][docIndex] += 1;
				}
				docIndex++;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void calSum() {
		// calculate the rowsum and colsum
		for (int i = 0; i < termDocMat.length; i++) {
			for (int j = 0; j < termDocMat[0].length; j++) {
				matrixSum += termDocMat[i][j];

				termSums[i] += termDocMat[i][j];
				docSums[j] += termDocMat[i][j];
			}
		}
	}

	public double coherenceScore(String filePath){
		BufferedReader br;
		int numTopics = 0;
		double score = 0;
		try {
			br = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = br.readLine()) != null) {
				numTopics++;

				String[] words = line.split(",");
				ArrayList<String> topics = new ArrayList<String>();

				for (String word : words) {
					topics.add(word);
				}

				for (int m = 1; m < topics.size(); m++) {
					for (int l = 0; l < m - 1; l++) {
						score += calCoherence(topics.get(m), topics.get(l));
					}
				}
			}

			br.close();
			return score / numTopics;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private double calCoherence(String m, String l) {

		int mIndex = dictionary.get(m);
		int lIndex = dictionary.get(l);

		int d2 = 1;
		int dl = 0;
		for (int i = 0; i < documentSize; i++) {
			if (termDocMat[mIndex][i] > 0 && termDocMat[lIndex][i] > 0) {
				d2++;
			}
			
			if (termDocMat[lIndex][i]>0){
				dl++;
			}
		}

		return log2((double)d2 / (double)dl);
	}

	public double log2(double a) {
		// if zero
		if (new Double(0.0).equals(a))
			return 0.0;
		return Math.log(a) / Math.log(2);
	}
}
