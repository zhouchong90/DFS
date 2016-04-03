package extractDFS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MutualInformation {

	public class AbstractFeature {
		private ArrayList<Integer> featureIndexList;

		public double featureMIWithTC;

		public double featureProbability;
		public int featureTotalCount[];

		public AbstractFeature(AbstractFeature a1, AbstractFeature a2) {
			featureIndexList = new ArrayList<Integer>();

			HashSet<Integer> set = new HashSet<Integer>();
			set.addAll(a1.featureIndexList);
			set.addAll(a2.featureIndexList);

			featureIndexList.addAll(set);

			featureTotalCount = new int[documentSize];
			for (int i = 0; i < documentSize; i++) {
				featureTotalCount[i] = featureTotalCount(i);
			}
			featureProbability = featureProbability();
			featureMIWithTC = featureMIWithTC();
		}

		public AbstractFeature(int index) {
			featureIndexList = new ArrayList<Integer>();
			featureIndexList.add(index);

			featureTotalCount = new int[documentSize];
			for (int i = 0; i < documentSize; i++) {
				featureTotalCount[i] = featureTotalCount(i);
			}
			featureProbability = featureProbability();
			featureMIWithTC = featureMIWithTC();
		}

		private double featureMIWithTC() {
			double MI = 0;

			double probTerm = this.featureProbability();
			for (int i = 0; i < documentSize; i++) {
				double probJoint = (double) featureTotalCount[i] / matrixSum;
				double probTC = (double) docSums[i] / matrixSum;
				MI += probJoint * log2(probJoint / (probTerm * probTC));
			}
			return MI;
		}

		private double featureProbability() {
			int totalCount = 0;
			for (int index : featureIndexList)
				totalCount += termSums[index];
			return (double) totalCount / matrixSum;
		}

		// count all occurrence in this abstract feature
		private int featureTotalCount(int tcIndex) {
			int count = 0;
			for (int index : featureIndexList)
				count += termDocMat[index][tcIndex];
			return count;
		}

		public ArrayList<Integer> getFeatureIndexList() {
			return featureIndexList;
		}

	}

	/**
	 * term-index, index-term dictionary
	 */
	public HashMap<String, Integer> dictionary = new HashMap<String, Integer>();

	int[] docSums;
	public int documentSize = 0;

	public ArrayList<AbstractFeature> featureList;

	public HashMap<Integer, String> inverseDictionary = new HashMap<Integer, String>();

	// sum of matrix
	int matrixSum = 0;

	final int minWordLength = 2;

	// term-document frequency matrix
	public int[][] termDocMat;

	int[] termSums;

	public MutualInformation(String inputFile) {
		processDictionary(inputFile);
		init();
		readInData(inputFile);
		calSum();
		constructFeatureList();
	}

	public void calSum() {
		for (int i = 0; i < termDocMat.length; i++)
			for (int j = 0; j < termDocMat[0].length; j++)
				matrixSum += termDocMat[i][j];

		// calculate the rowsum and colsum
		for (int i = 0; i < termDocMat.length; i++) {
			for (int j = 0; j < termDocMat[0].length; j++) {
				termSums[i] += termDocMat[i][j];
				docSums[j] += termDocMat[i][j];
			}
		}
	}

	public void constructFeatureList() {
		for (int index = 0; index < dictionary.size(); index++) {
			AbstractFeature f = new AbstractFeature(index);
			featureList.add(f);
		}
	}

	public void init() {
		termDocMat = new int[dictionary.size()][documentSize];
		// PMIMat = new double[dictionary.size()][documentSize];

		termSums = new int[termDocMat.length];
		docSums = new int[termDocMat[0].length];

		// manage all abstract features
		featureList = new ArrayList<AbstractFeature>();
	}

	public double log2(double a) {
		// if zero
		if (new Double(0.0).equals(a))
			return 0.0;
		return Math.log(a) / Math.log(2);
	}

	public void printFeatureMerge(BufferedWriter bw, AbstractFeature af1, AbstractFeature af2, double MI)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int wordIndex : af1.featureIndexList) {
			sb.append(inverseDictionary.get(wordIndex));
			if (af1.featureIndexList.size() > 1)
				sb.append(",");
		}

		sb.append(",");

		for (int wordIndex : af2.featureIndexList) {
			sb.append(inverseDictionary.get(wordIndex));
			if (af2.featureIndexList.size() > 1)
				sb.append(",");
		}
		if (sb.charAt(sb.length()-1) == ',')
			sb.setLength(sb.length()-1);
		bw.write(sb.toString());
		bw.newLine();
	}

	public void printFeatureMerge(BufferedWriter bw, AbstractFeature af1, double MI) throws IOException {
		for (int wordIndex : af1.featureIndexList) {
			bw.append(inverseDictionary.get(wordIndex));
			if (af1.featureIndexList.size() > 1)
				bw.append(",");
		}

		bw.append(",");

		bw.append(String.valueOf(MI));
		bw.newLine();
	}

	public void processDictionary(String inputFile) {
		try {
			File doc = new File(inputFile);

			int index = 0;

			BufferedReader br = new BufferedReader(new FileReader(doc));

			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split(" ");
				for (String word : words)
					if (word.length() >= minWordLength)
						if (!dictionary.containsKey(word)) {
							dictionary.put(word, index);
							inverseDictionary.put(index, word);
							index++;
						}
				documentSize++;
			}
			br.close();

			System.out.println("dictionary size:" + dictionary.size());
			System.out.println("document size:" + documentSize);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readInData(String inputFile) {
		try {
			File doc = new File(inputFile);

			int docIndex = 0;
			BufferedReader br = new BufferedReader(new FileReader(doc));

			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split(" ");
				for (String word : words) {
					termDocMat[dictionary.get(word)][docIndex]++;
				}
				docIndex++;
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
