package extractDFS;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import extractDFS.MutualInformation.AbstractFeature;

public abstract class DiscriminatingFeatureSet {

	MutualInformation w;
	String outputFile;
	int topk;
	
	public DiscriminatingFeatureSet(String inputFile, String outputFile, int topk) {
		this.outputFile = outputFile;
		this.topk = topk;
		w = new MutualInformation(inputFile);
	}

	public void run()
	{
		//calculateInfoloss();
		mergedFeatures();
	}
	
	public void mergedFeatures(){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			for(int k = 0; k<topk; k++)
			{	
				double maxMI = 0;
				int maxI = 0;
				int maxJ = 1;
				
				//find the max MI loss and their index
				for(int i = 0; i< w.featureList.size()-1; i++)
				{
					for(int j = i+1; j< w.featureList.size(); j++)
					{
						double MI = informationLoss(w.featureList.get(i),w.featureList.get(j));
						if(MI>maxMI)
						{
							maxMI = MI;
							maxI = i;
							maxJ = j;
						}
					}
				}
				//merge feature I and J
				AbstractFeature mergedFeature = w.new AbstractFeature(w.featureList.get(maxI),w.featureList.get(maxJ));
				w.printFeatureMerge(bw,w.featureList.get(maxI),w.featureList.get(maxJ),maxMI);
				//has to delete J first since J is larger than I.\
				//TODO: for overlapping sets
				w.featureList.remove(maxJ);
				w.featureList.remove(maxI);			
				w.featureList.add(mergedFeature);
			}
			bw.close();
			
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//return if a1 belongs to a2
	public boolean belongs(HashSet<Integer> a1, HashSet<Integer> a2)
	{
		for(int i:a1)
		{
			if(!a2.contains(i))
				return false;
		}
		return true;
	}
	
	public void calculateInfoloss(){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Chong/Desktop/InfoLossResult.csv"));
			
			for(int i = 0; i< w.featureList.size(); i++)
			{
				for(int j = i+1; j< w.featureList.size(); j++)
				{
					double MI = informationLoss(w.featureList.get(i),w.featureList.get(j));
					
					w.printFeatureMerge(bw,w.featureList.get(i),w.featureList.get(j),MI);
				}
			}
			bw.close();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	abstract double informationLoss(AbstractFeature abstractFeature,AbstractFeature abstractFeature2);

}