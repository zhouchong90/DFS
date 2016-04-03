package extractDFS;

import java.util.ArrayList;

import extractDFS.MutualInformation.AbstractFeature;

public class KLMean extends DiscriminatingFeatureSet {

	public KLMean(String inputFile, String outputFile, int topk) {
		super(inputFile,outputFile, topk);
	}

	@Override
	// KL to the mean //the max value of a1 and a2 is the distance of a1, a2
	double informationLoss(AbstractFeature a1, AbstractFeature a2) {

		// P(C|A or B) = P(C|A)+P(C|B)-P(C|AB)
		double KL = 0;

		double pw1 = a1.featureProbability;
		double pw2 = a2.featureProbability;

		for (int i = 0; i < w.documentSize; i++) {
			
			double ptc_w1 = ((double) a1.featureTotalCount[i] / w.matrixSum)/ pw1;
			double ptc_w2 = ((double) a2.featureTotalCount[i] / w.matrixSum)/ pw2;

			if(ptc_w1==0 || ptc_w2==0)
				continue;
			else
			{
				KL += (ptc_w1 * w.log2(ptc_w1/ptc_w2) + ptc_w2 * w.log2(ptc_w2/ptc_w1))/2;
			}
		}

		return KL;

		/*
		 * // calculate informationLoss of the two feature if merged.
		 * 
		 * // p(a) double featureProb = feature1.featureProbability +
		 * feature2.featureProbability;
		 * 
		 * // H(term) ArrayList<Double> term = new ArrayList<Double>(); for (int
		 * i = 0; i < w.documentSize; i++) { double sum = 0; sum += (double)
		 * feature1.featureTotalCount[i] / w.matrixSum; sum += (double)
		 * feature2.featureTotalCount[i] / w.matrixSum; sum *= (double) 1 /
		 * featureProb; term.add(sum); } double entropy = entropy(term);
		 * 
		 * // weight1 and H(term1) double weight1 = feature1.featureProbability
		 * / featureProb; ArrayList<Double> term1 = new ArrayList<Double>(); for
		 * (int i = 0; i < w.documentSize; i++) { double jointProb = (double)
		 * feature1.featureTotalCount[i] / w.matrixSum; term1.add(jointProb /
		 * feature1.featureProbability); } double entropy1 = entropy(term1);
		 * 
		 * // weight2 and H(term2) double weight2 = feature2.featureProbability
		 * / featureProb; ArrayList<Double> term2 = new ArrayList<Double>(); for
		 * (int i = 0; i < w.documentSize; i++) { double jointProb = (double)
		 * feature2.featureTotalCount[i] / w.matrixSum; term2.add(jointProb /
		 * feature2.featureProbability); } double entropy2 = entropy(term2);
		 * 
		 * double infoLoss = (entropy - weight1 * entropy1 - weight2 *
		 * entropy2);
		 * 
		 * return infoLoss;
		 */
	}

	public double entropy(ArrayList<Double> values) {
		double probSum = 0;

		for (double v : values) {
			probSum += v;
		}

		double result = 0;
		for (double v : values) {
			double prob = v / probSum;
			result -= prob * w.log2(prob);
		}

		return result;
	}

}
